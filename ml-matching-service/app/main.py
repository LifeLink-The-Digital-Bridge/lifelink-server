from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager
import uvicorn
import time
import asyncio

from app.models.request_models import RecipientRequest, DonationData
from app.models.response_models import BatchMatchRequest, BatchMatchResponse, Match
from app.config import SERVICE_NAME, SERVICE_PORT, SERVICE_HOST, LOCAL_IP, EUREKA_SERVER
from app.logger_config import logger
from app.integrations.eureka import register_with_eureka, deregister_from_eureka, send_heartbeat
from app.utils.data_printer import print_batch_match_request
from app.services.donation_router import DonationRouter


# Background task for heartbeat
async def heartbeat_loop():
    """Send periodic heartbeats to Eureka every 30 seconds"""
    await asyncio.sleep(10)  # Wait for initial registration to complete

    while True:
        try:
            success = send_heartbeat()
            if not success:
                logger.warning("Heartbeat failed, attempting re-registration...")
                register_with_eureka()
        except Exception as e:
            logger.error(f"Error in heartbeat loop: {e}")

        await asyncio.sleep(30)  # Send heartbeat every 30 seconds


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("=" * 120)
    logger.info("STARTING ML MATCHING SERVICE")
    logger.info("=" * 120)
    logger.info(f"Service: {SERVICE_NAME}")
    logger.info(f"IP: {LOCAL_IP}")
    logger.info(f"Port: {SERVICE_PORT}")
    logger.info(f"Eureka: {EUREKA_SERVER}")
    logger.info("=" * 120)

    # Register with Eureka
    register_with_eureka()

    # Start heartbeat background task
    heartbeat_task = asyncio.create_task(heartbeat_loop())
    logger.info("💓 Heartbeat task started")

    yield

    # Cleanup on shutdown
    logger.info("=" * 120)
    logger.info("SHUTTING DOWN ML MATCHING SERVICE")
    logger.info("=" * 120)

    # Cancel heartbeat task
    heartbeat_task.cancel()
    try:
        await heartbeat_task
    except asyncio.CancelledError:
        logger.info("💓 Heartbeat task stopped")

    # Deregister from Eureka
    deregister_from_eureka()


app = FastAPI(
    title="ML Matching Service",
    description="Multi-type donation matching with ML",
    version="3.0.0",
    lifespan=lifespan
)


@app.get("/health")
async def health_check():
    return {"status": "UP", "service": SERVICE_NAME}


@app.post("/api/ml/batch-match", response_model=BatchMatchResponse)
async def batch_match(request: BatchMatchRequest):
    try:
        ml_requests = request.requests
        ml_donations = request.donations
        top_n = request.topN
        threshold = request.threshold

        logger.info("=" * 120)
        logger.info("RECEIVED BATCH MATCH REQUEST")
        logger.info("=" * 120)

        print_batch_match_request(ml_requests, ml_donations, top_n, threshold)

        logger.info("=" * 120)
        logger.info("CATEGORIZING BATCH BY DONATION TYPE")
        logger.info("=" * 120)

        categorized_batch = DonationRouter.categorize_batch(ml_donations, ml_requests)

        if not categorized_batch:
            logger.warning("No valid donation-request type combinations found")
            return BatchMatchResponse(
                success=False,
                matchesFound=0,
                matches=[],
                modelVersion="v3.0.0",
                processingTimeMs=0
            )

        logger.info("=" * 120)
        logger.info("ROUTING TO SPECIALIZED SERVICES")
        logger.info("=" * 120)

        start_time = time.time()
        matches = DonationRouter.route_to_service(categorized_batch, top_n, threshold)
        processing_time_ms = int((time.time() - start_time) * 1000)

        match_objects = []
        for idx, match in enumerate(matches, 1):
            match_obj = Match(
                donationId=match['donationId'],
                receiveRequestId=match['receiveRequestId'],
                donorUserId=match['donorUserId'],
                recipientUserId=match['recipientUserId'],
                compatibilityScore=match['compatibilityScore'],
                bloodCompatibilityScore=match['bloodCompatibilityScore'],
                locationCompatibilityScore=match['locationCompatibilityScore'],
                medicalCompatibilityScore=match['medicalCompatibilityScore'],
                urgencyPriorityScore=match['urgencyPriorityScore'],
                distanceKm=match['distanceKm'],
                matchReason=match['matchReason'],
                priorityRank=idx,
                mlConfidence=match['mlConfidence']
            )
            match_objects.append(match_obj)

        logger.info("=" * 120)
        logger.info("BATCH MATCHING COMPLETE")
        logger.info("=" * 120)
        logger.info(f"Total matches: {len(match_objects)}")
        logger.info(f"Processing time: {processing_time_ms}ms")

        return BatchMatchResponse(
            success=True,
            matchesFound=len(match_objects),
            matches=match_objects,
            modelVersion="v3.0.0",
            processingTimeMs=processing_time_ms
        )

    except Exception as e:
        logger.error(f"Error: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == '__main__':
    uvicorn.run(app, host='0.0.0.0', port=SERVICE_PORT, log_level="info")
