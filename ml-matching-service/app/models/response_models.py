from pydantic import BaseModel
from typing import List, Optional

from app.models.request_models import RecipientRequest, DonationData


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
    distanceKm: float
    matchReason: str
    priorityRank: int
    mlConfidence: float
    donorLocationId: Optional[str] = None
    recipientLocationId: Optional[str] = None
    distance: Optional[float] = None
    status: Optional[str] = None
    isConfirmed: Optional[bool] = None
    donorConfirmed: Optional[bool] = None
    recipientConfirmed: Optional[bool] = None
    donorConfirmedAt: Optional[str] = None
    recipientConfirmedAt: Optional[str] = None
    matchedAt: Optional[str] = None
    expiredAt: Optional[str] = None
    expiryReason: Optional[str] = None
    firstConfirmer: Optional[str] = None
    firstConfirmedAt: Optional[str] = None
    confirmationExpiresAt: Optional[str] = None
    withdrawalReason: Optional[str] = None
    withdrawnAt: Optional[str] = None
    withdrawnBy: Optional[str] = None
    completedAt: Optional[str] = None
    completionConfirmedBy: Optional[str] = None
    completionNotes: Optional[str] = None
    receivedDate: Optional[str] = None
    recipientRating: Optional[int] = None
    hospitalName: Optional[str] = None


class BatchMatchRequest(BaseModel):
    requests: List[RecipientRequest]
    donations: List[DonationData]
    topN: int = 10
    threshold: float = 0.5


class BatchMatchResponse(BaseModel):
    success: bool
    matchesFound: int
    matches: List[Match]
    modelVersion: str
    processingTimeMs: int
