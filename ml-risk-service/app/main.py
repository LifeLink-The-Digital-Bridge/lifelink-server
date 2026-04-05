from fastapi import FastAPI, HTTPException
from uuid import uuid4

from app.models.request_models import ComputeRiskRequest
from app.models.response_models import ComputeRiskResponse
from app.integrations.health_client import HealthServiceClient
from app.services.risk_engine import compute_risk_score
from app.config import SERVICE_NAME

app = FastAPI(title="ML Risk Service", version="1.0.0")
health_client = HealthServiceClient()


@app.get("/health")
async def health_check():
    return {"status": "UP", "service": SERVICE_NAME}


@app.post("/api/ml-risk/v1/scores/compute", response_model=ComputeRiskResponse)
async def compute_score(request: ComputeRiskRequest):
    job_id = str(uuid4())
    try:
        risk_input = await health_client.fetch_risk_input(request.userId)
        result = compute_risk_score(risk_input)
        payload = {
            "userId": request.userId,
            "healthId": risk_input.get("healthId", {}).get("healthId") or request.healthId,
            "riskScore": result["riskScore"],
            "riskLevel": result["riskLevel"],
            "topFactors": result["topFactors"],
            "recommendedActions": result["recommendedActions"],
            "modelVersion": result["modelVersion"],
            "computedAt": result["computedAt"]
        }
        await health_client.push_risk_snapshot(payload)
        return ComputeRiskResponse(jobId=job_id, status="COMPLETED", etaSeconds=0)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
