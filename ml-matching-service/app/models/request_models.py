from pydantic import BaseModel
from typing import List, Optional
from app.models.enums import (
    BloodType, DonationType, RequestType, UrgencyLevel,
    SmokingStatus, AlcoholStatus, BodySize, OverallHealthStatus
)

class RecipientRequest(BaseModel):
    receiveRequestId: str
    recipientId: str
    userId: str
    locationId: str
    requestType: RequestType
    urgencyLevel: UrgencyLevel
    daysWaiting: int
    requestedBloodType: Optional[BloodType] = None
    requestedOrgan: Optional[str] = None
    requestedTissue: Optional[str] = None
    requestedStemCellType: Optional[str] = None
    quantity: Optional[float] = None
    requestDate: Optional[str] = None
    age: Optional[int] = None
    dob: Optional[str] = None
    weight: Optional[float] = None
    height: Optional[float] = None
    bmi: Optional[float] = None
    bodySize: Optional[BodySize] = None
    hemoglobinLevel: Optional[float] = None
    bloodGlucoseLevel: Optional[float] = None
    hasDiabetes: Optional[bool] = None
    bloodPressure: Optional[str] = None
    hasInfectiousDiseases: Optional[bool] = None
    infectiousDiseaseDetails: Optional[str] = None
    creatinineLevel: Optional[float] = None
    liverFunctionTests: Optional[str] = None
    cardiacStatus: Optional[str] = None
    pulmonaryFunction: Optional[float] = None
    overallHealthStatus: Optional[OverallHealthStatus] = None
    diagnosis: Optional[str] = None
    allergies: Optional[str] = None
    smokingStatus: Optional[SmokingStatus] = None
    packYears: Optional[int] = None
    alcoholStatus: Optional[AlcoholStatus] = None
    drinksPerWeek: Optional[int] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    city: Optional[str] = None
    district: Optional[str] = None
    state: Optional[str] = None
    country: Optional[str] = None
    hlaA1: Optional[str] = None
    hlaA2: Optional[str] = None
    hlaB1: Optional[str] = None
    hlaB2: Optional[str] = None
    hlaC1: Optional[str] = None
    hlaC2: Optional[str] = None
    hlaDR1: Optional[str] = None
    hlaDR2: Optional[str] = None
    hlaDQ1: Optional[str] = None
    hlaDQ2: Optional[str] = None
    hlaDP1: Optional[str] = None
    hlaDP2: Optional[str] = None
    hlaHighResolution: Optional[bool] = None
    hlaString: Optional[str] = None


class DonationData(BaseModel):
    donationId: str
    donorId: str
    userId: str
    locationId: str
    donationType: DonationType
    bloodType: BloodType
    donationDate: str
    quantity: Optional[float] = None
    age: Optional[int] = None
    dob: Optional[str] = None
    weight: Optional[float] = None
    height: Optional[float] = None
    bmi: Optional[float] = None
    bodySize: Optional[BodySize] = None
    isLivingDonor: Optional[bool] = None
    hemoglobinLevel: Optional[float] = None
    bloodGlucoseLevel: Optional[float] = None
    hasDiabetes: Optional[bool] = None
    bloodPressure: Optional[str] = None
    hasDiseases: Optional[bool] = None
    hasInfectiousDiseases: Optional[bool] = None
    infectiousDiseaseDetails: Optional[str] = None
    creatinineLevel: Optional[float] = None
    liverFunctionTests: Optional[str] = None
    cardiacStatus: Optional[str] = None
    pulmonaryFunction: Optional[float] = None
    overallHealthStatus: Optional[OverallHealthStatus] = None
    medicalClearance: Optional[bool] = None
    recentTattoo: Optional[bool] = None
    recentVaccination: Optional[bool] = False
    recentSurgery: Optional[bool] = None
    chronicDiseases: Optional[str] = None
    allergies: Optional[str] = None
    lastDonationDate: Optional[str] = None
    daysSinceLastDonation: Optional[int] = None
    smokingStatus: Optional[SmokingStatus] = None
    packYears: Optional[int] = None
    quitSmokingDate: Optional[str] = None
    alcoholStatus: Optional[AlcoholStatus] = None
    drinksPerWeek: Optional[int] = None
    quitAlcoholDate: Optional[str] = None
    alcoholAbstinenceMonths: Optional[int] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    city: Optional[str] = None
    district: Optional[str] = None
    state: Optional[str] = None
    country: Optional[str] = None
    organType: Optional[str] = None
    organQuality: Optional[str] = None
    organViabilityExpiry: Optional[str] = None
    organViabilityHours: Optional[float] = None
    coldIschemiaTime: Optional[int] = None
    organPerfused: Optional[bool] = None
    organWeight: Optional[float] = None
    organSize: Optional[str] = None
    hasAbnormalities: Optional[bool] = None
    tissueType: Optional[str] = None
    stemCellType: Optional[str] = None
    hlaA1: Optional[str] = None
    hlaA2: Optional[str] = None
    hlaB1: Optional[str] = None
    hlaB2: Optional[str] = None
    hlaC1: Optional[str] = None
    hlaC2: Optional[str] = None
    hlaDR1: Optional[str] = None
    hlaDR2: Optional[str] = None
    hlaDQ1: Optional[str] = None
    hlaDQ2: Optional[str] = None
    hlaDP1: Optional[str] = None
    hlaDP2: Optional[str] = None
    hlaHighResolution: Optional[bool] = None
    hlaString: Optional[str] = None


class BatchMatchRequest(BaseModel):
    requests: List[RecipientRequest]
    donations: List[DonationData]
    topN: int = 10
    threshold: float = 0.5
