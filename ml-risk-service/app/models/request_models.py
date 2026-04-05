from pydantic import BaseModel
from typing import Literal


class ComputeRiskRequest(BaseModel):
    userId: str
    healthId: str | None = None
    trigger: Literal["HEALTH_ID_CREATED", "HEALTH_ID_UPDATED", "HEALTH_RECORD_CREATED", "HEALTH_RECORD_UPDATED", "EMERGENCY_RECORD_CREATED", "MANUAL"]
    requestedBy: Literal["system", "doctor", "migrant", "admin"] = "system"
    requestId: str | None = None
