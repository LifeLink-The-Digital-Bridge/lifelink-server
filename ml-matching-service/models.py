from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime, date

class RecipientRequest(BaseModel):
    receiveRequestId: str
    recipientId: str
    userId: str
    locationId: str
    requestType: str
    requestedBloodType: Optional[str]
    requestedOrgan: Optional[str]
    requestedTissue: Optional[str]
    requestedStemCellType: Optional[str]
    urgencyLevel: str
    quantity: Optional[float]
    requestDate: Optional[str]
    daysWaiting: int
    age: Optional[int]
    dob: Optional[str]
    weight: Optional[float]
    height: Optional[float]
    bmi: Optional[float]
    bodySize: Optional[str]
    hemoglobinLevel: Optional[float]
    bloodGlucoseLevel: Optional[float]
    hasDiabetes: Optional[bool]
    bloodPressure: Optional[str]
    hasInfectiousDiseases: Optional[bool]
    infectiousDiseaseDetails: Optional[str]
    creatinineLevel: Optional[float]
    liverFunctionTests: Optional[str]
    cardiacStatus: Optional[str]
    pulmonaryFunction: Optional[float]
    overallHealthStatus: Optional[str]
    diagnosis: Optional[str]
    allergies: Optional[str]
    smokingStatus: Optional[str]
    packYears: Optional[int]
    alcoholStatus: Optional[str]
    drinksPerWeek: Optional[int]
    latitude: Optional[float]
    longitude: Optional[float]
    city: Optional[str]
    district: Optional[str]
    state: Optional[str]
    country: Optional[str]
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
    donationType: str
    bloodType: str
    donationDate: str
    quantity: Optional[float]
    age: Optional[int]
    dob: Optional[str]
    weight: Optional[float]
    height: Optional[float]
    bmi: Optional[float]
    bodySize: Optional[str]
    isLivingDonor: Optional[bool]
    hemoglobinLevel: Optional[float]
    bloodGlucoseLevel: Optional[float]
    hasDiabetes: Optional[bool]
    bloodPressure: Optional[str]
    hasDiseases: Optional[bool]
    hasInfectiousDiseases: Optional[bool]
    infectiousDiseaseDetails: Optional[str]
    creatinineLevel: Optional[float]
    liverFunctionTests: Optional[str]
    cardiacStatus: Optional[str]
    pulmonaryFunction: Optional[float]
    overallHealthStatus: Optional[str]
    medicalClearance: Optional[bool]
    recentTattoo: Optional[bool]
    recentVaccination: Optional[bool]
    recentSurgery: Optional[bool]
    chronicDiseases: Optional[str]
    allergies: Optional[str]
    lastDonationDate: Optional[str]
    daysSinceLastDonation: Optional[int]
    smokingStatus: Optional[str]
    packYears: Optional[int]
    quitSmokingDate: Optional[str]
    alcoholStatus: Optional[str]
    drinksPerWeek: Optional[int]
    quitAlcoholDate: Optional[str]
    alcoholAbstinenceMonths: Optional[int]
    latitude: Optional[float]
    longitude: Optional[float]
    city: Optional[str]
    district: Optional[str]
    state: Optional[str]
    country: Optional[str]
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

class BatchMatchResponse(BaseModel):
    success: bool
    matchesFound: int
    matches: List[Match]
    modelVersion: str
    processingTimeMs: int
