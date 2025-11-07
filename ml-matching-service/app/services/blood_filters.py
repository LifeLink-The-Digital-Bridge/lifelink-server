from app.logger_config import logger

BLOOD_COMPATIBILITY = {
    'O_NEGATIVE': ['O_NEGATIVE', 'O_POSITIVE', 'A_NEGATIVE', 'A_POSITIVE', 'B_NEGATIVE', 'B_POSITIVE', 'AB_NEGATIVE', 'AB_POSITIVE'],
    'O_POSITIVE': ['O_POSITIVE', 'A_POSITIVE', 'B_POSITIVE', 'AB_POSITIVE'],
    'A_NEGATIVE': ['A_NEGATIVE', 'A_POSITIVE', 'AB_NEGATIVE', 'AB_POSITIVE'],
    'A_POSITIVE': ['A_POSITIVE', 'AB_POSITIVE'],
    'B_NEGATIVE': ['B_NEGATIVE', 'B_POSITIVE', 'AB_NEGATIVE', 'AB_POSITIVE'],
    'B_POSITIVE': ['B_POSITIVE', 'AB_POSITIVE'],
    'AB_NEGATIVE': ['AB_NEGATIVE', 'AB_POSITIVE'],
    'AB_POSITIVE': ['AB_POSITIVE']
}


class BloodTypeFilters:

    @staticmethod
    def check_blood_compatibility(donor_blood, recipient_blood):
        if not donor_blood or not recipient_blood:
            return False, "Missing blood type"

        donor_blood = str(donor_blood).replace('BloodType.', '').strip()
        recipient_blood = str(recipient_blood).replace('BloodType.', '').strip()

        is_compatible = recipient_blood in BLOOD_COMPATIBILITY.get(donor_blood, [])

        if is_compatible:
            return True, f"{donor_blood} -> {recipient_blood}"
        else:
            return False, f"{donor_blood} incompatible with {recipient_blood}"

    @staticmethod
    def check_infectious_diseases(donation):
        if donation.hasInfectiousDiseases:
            return False, "Has infectious diseases"
        return True, "No infectious diseases"

    @staticmethod
    def check_hemoglobin_level(donor_hemoglobin):
        MIN_HEMOGLOBIN = 12.5

        if not donor_hemoglobin:
            return False, "Hemoglobin missing"

        if donor_hemoglobin < MIN_HEMOGLOBIN:
            return False, f"Low hemoglobin: {donor_hemoglobin} < {MIN_HEMOGLOBIN}"

        return True, f"Hemoglobin ok: {donor_hemoglobin}"

    @staticmethod
    def check_quantity(donation, request):
        """Check if donation quantity is sufficient"""
        if not donation.quantity or not request.quantity:
            return True, "Quantity check skipped"

        if donation.quantity < request.quantity:
            return False, f"Low qty: {donation.quantity}ml < {request.quantity}ml"

        return True, f"Quantity ok: {donation.quantity}ml >= {request.quantity}ml"

    @staticmethod
    def apply_all_filters(donation, request):
        filters = [
            (BloodTypeFilters.check_blood_compatibility, (donation.bloodType, request.requestedBloodType)),
            (BloodTypeFilters.check_infectious_diseases, (donation,)),
            (BloodTypeFilters.check_hemoglobin_level, (donation.hemoglobinLevel,)),
            (BloodTypeFilters.check_quantity, (donation, request))
        ]

        last_reason = ""
        for filter_func, args in filters:
            passed, reason = filter_func(*args)
            last_reason = reason
            if not passed:
                return False, reason, 0.0

        hard_score = BloodTypeFilters.calculate_hard_score(donation, request)
        return True, last_reason, hard_score

    @staticmethod
    def calculate_hard_score(donation, request):
        score = 0.0

        score += 0.40

        if donation.hemoglobinLevel and donation.hemoglobinLevel >= 14.0:
            score += 0.15

        if not (donation.recentTattoo or donation.recentSurgery):
            score += 0.10

        if donation.medicalClearance and not donation.hasDiseases:
            score += 0.15

        if donation.quantity and request.quantity and donation.quantity >= request.quantity:
            score += 0.10

        if not donation.hasInfectiousDiseases:
            score += 0.10

        return min(max(score, 0.0), 1.0)
