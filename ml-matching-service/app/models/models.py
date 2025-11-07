from pydantic import BaseModel
from typing import List

class Match(BaseModel):
    donationId: str
    receiveRequestId: str
    donorUserId: str
    recipientUserId: str
    compatibilityScore: float
    bloodCompatibilityScore: float
    locationCompatibilityScore: float
    medicalCompatibilityScore: float
    urgencyPriorityScore: float
    hlaCompatibilityScore: float = 0.0
    hlaMismatchCount: int = 0
    distanceKm: float
    matchReason: str
    priorityRank: int
    mlConfidence: float


class BatchMatchResponse(BaseModel):
    success: bool
    matchesFound: int
    matches: List[Match]
    modelVersion: str = "v3.0.0"
    processingTimeMs: int


class HealthCheckResponse(BaseModel):
    status: str
    service: str
    version: str
    modelAccuracy: float
    modelAuc: float
