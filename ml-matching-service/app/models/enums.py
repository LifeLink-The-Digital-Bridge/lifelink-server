from enum import Enum


class BloodType(Enum):
    A_POSITIVE = "A_POSITIVE"
    A_NEGATIVE = "A_NEGATIVE"
    B_POSITIVE = "B_POSITIVE"
    B_NEGATIVE = "B_NEGATIVE"
    O_POSITIVE = "O_POSITIVE"
    O_NEGATIVE = "O_NEGATIVE"
    AB_POSITIVE = "AB_POSITIVE"
    AB_NEGATIVE = "AB_NEGATIVE"


class DonationType(Enum):
    BLOOD = "BLOOD"
    ORGAN = "ORGAN"
    TISSUE = "TISSUE"
    STEM_CELL = "STEM_CELL"


class RequestType(Enum):
    BLOOD = "BLOOD"
    ORGAN = "ORGAN"
    TISSUE = "TISSUE"
    STEM_CELL = "STEM_CELL"


class UrgencyLevel(Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


class SmokingStatus(Enum):
    NEVER_SMOKED = "NEVER_SMOKED"
    FORMER_SMOKER = "FORMER_SMOKER"
    CURRENT_SMOKER = "CURRENT_SMOKER"
    OCCASIONAL_SMOKER = "OCCASIONAL_SMOKER"


class AlcoholStatus(Enum):
    NO_ALCOHOL_USE = "NO_ALCOHOL_USE"
    MODERATE_USE = "MODERATE_USE"
    HEAVY_USE = "HEAVY_USE"
    FORMER_USER = "FORMER_USER"


class OverallHealthStatus(Enum):
    EXCELLENT = "EXCELLENT"
    GOOD = "GOOD"
    FAIR = "FAIR"
    POOR = "POOR"

class BodySize(Enum):
    SMALL = "SMALL"
    MEDIUM = "MEDIUM"
    LARGE = "LARGE"
