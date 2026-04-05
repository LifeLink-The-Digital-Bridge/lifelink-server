from pydantic import BaseModel
from typing import List


class ComputeRiskResponse(BaseModel):
    jobId: str
    status: str
    etaSeconds: int


class RiskScorePayload(BaseModel):
    userId: str
    healthId: str
    riskScore: float
    riskLevel: str
    topFactors: List[str]
    recommendedActions: List[str]
    modelVersion: str
    computedAt: str
