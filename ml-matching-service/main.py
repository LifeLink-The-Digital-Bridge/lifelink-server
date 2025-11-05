from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager
import uvicorn

from models import BatchMatchRequest, BatchMatchResponse, Match
from config import SERVICE_NAME, SERVICE_PORT, SERVICE_HOST, LOCAL_IP, EUREKA_SERVER
from logger_config import logger
from eureka import register_with_eureka, deregister_from_eureka
from data_printer import print_batch_match_request


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info(f"\n{'=' * 120}")
    logger.info(f"STARTING ML MATCHING SERVICE")
    logger.info(f"{'=' * 120}")
    logger.info(f"   • Service Name: {SERVICE_NAME}")
    logger.info(f"   • IP Address: {LOCAL_IP}")
    logger.info(f"   • Port: {SERVICE_PORT}")
    logger.info(f"   • Host: {SERVICE_HOST}")
    logger.info(f"   • Eureka Server: {EUREKA_SERVER}")
    logger.info(f"{'=' * 120}\n")

    register_with_eureka()

    yield

    logger.info(f"\n{'=' * 120}")
    logger.info(f"SHUTTING DOWN ML MATCHING SERVICE")
    logger.info(f"{'=' * 120}\n")
    deregister_from_eureka()


app = FastAPI(
    title="ML Matching Service",
    description="AI/ML service for organ donation matching",
    version="1.0.0",
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

        print_batch_match_request(ml_requests, ml_donations, top_n, threshold)

        mock_matches = []
        for i in range(min(3, max(1, len(ml_requests) * len(ml_donations)))):
            match = Match(
                donationId=ml_donations[i % len(ml_donations)].donationId if ml_donations else "D001",
                receiveRequestId=ml_requests[i % len(ml_requests)].receiveRequestId if ml_requests else "R001",
                donorUserId=ml_donations[i % len(ml_donations)].userId if ml_donations else "DONOR001",
                recipientUserId=ml_requests[i % len(ml_requests)].userId if ml_requests else "RECIP001",
                compatibilityScore=0.85 - (i * 0.1),
                bloodCompatibilityScore=0.9,
                locationCompatibilityScore=0.8,
                medicalCompatibilityScore=0.85,
                urgencyPriorityScore=0.7,
                distanceKm=50 + (i * 10),
                matchReason="Good match",
                priorityRank=i + 1,
                mlConfidence=0.88
            )
            mock_matches.append(match)

        logger.info(f"Returning {len(mock_matches)} mock matches\n")

        return BatchMatchResponse(
            success=True,
            matchesFound=len(mock_matches),
            matches=mock_matches,
            modelVersion="v1.0.0",
            processingTimeMs=245
        )

    except Exception as e:
        logger.error(f"❌ Error: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == '__main__':
    uvicorn.run(app, host='0.0.0.0', port=SERVICE_PORT, log_level="info")
