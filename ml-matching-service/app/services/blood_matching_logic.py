from app.logger_config import logger
from app.services.blood_filters import BloodTypeFilters
from app.services.matching_service import get_blood_matching_service
from math import radians, sin, cos, sqrt, atan2

class BloodMatchingLogic:

    def __init__(self):
        self.filters = BloodTypeFilters()
        self.ml_service = get_blood_matching_service()

    def calculate_distance(self, lat1, lon1, lat2, lon2):
        R = 6371
        lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
        dlat = lat2 - lat1
        dlon = lon2 - lon1
        a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
        c = 2 * atan2(sqrt(a), sqrt(1-a))
        return R * c

    def calculate_match(self, donation, request, threshold=0.5):
        logger.info(f"D:{donation.donationId} -> R:{request.receiveRequestId}")

        passed, reason, hard_score = self.filters.apply_all_filters(donation, request)

        if not passed:
            logger.info(f"Hard filter failed: {reason}")
            return None

        logger.info(f"Hard filters passed: {hard_score:.4f}")

        try:
            ml_result = self.ml_service.predict_match(donation, request)
            ml_score = ml_result['match_score']
            logger.info(f"ML score: {ml_score:.4f}")
        except Exception as e:
            logger.warning(f"ML service error: {str(e)}, using hard score only")
            ml_score = hard_score

        combined_score = (hard_score * 0.3) + (ml_score * 0.7)

        if combined_score < threshold:
            logger.info(f"Below threshold: {combined_score:.4f} < {threshold}")
            return None

        logger.info(f"Combined score: {combined_score:.4f}")

        distance = self.calculate_distance(
            donation.latitude or 17.4399,
            donation.longitude or 78.3489,
            request.latitude or 17.4450,
            request.longitude or 78.3550
        )

        location_score = max(0.0, min(1.0, 1.0 - (distance / 50.0)))
        urgency_map = {'CRITICAL': 1.0, 'HIGH': 0.8, 'MEDIUM': 0.6, 'LOW': 0.4}
        urgency_score = urgency_map.get(str(request.urgencyLevel), 0.5)

        match = {
            'donationId': str(donation.donationId),
            'receiveRequestId': str(request.receiveRequestId),
            'donorUserId': str(donation.userId),
            'recipientUserId': str(request.userId),
            'compatibilityScore': combined_score,
            'bloodCompatibilityScore': hard_score,
            'locationCompatibilityScore': location_score,
            'medicalCompatibilityScore': ml_score,
            'urgencyPriorityScore': urgency_score,
            'distanceKm': distance,
            'matchReason': reason,
            'priorityRank': 1,
            'mlConfidence': ml_score
        }

        return match

    def batch_match(self, donations, requests, top_n=10, threshold=0.5):
        logger.info(f"Blood matching: {len(donations)} donations x {len(requests)} requests")

        all_matches = []

        for request in requests:
            logger.info(f"Request: {request.receiveRequestId}")

            request_matches = []

            for donation in donations:
                match = self.calculate_match(donation, request, threshold)
                if match:
                    request_matches.append(match)

            request_matches.sort(key=lambda x: x['compatibilityScore'], reverse=True)
            all_matches.extend(request_matches[:top_n])

            logger.info(f"Found {len(request_matches[:top_n])} matches")

        all_matches.sort(key=lambda x: x['compatibilityScore'], reverse=True)
        logger.info(f"Total matches: {len(all_matches)}")

        return all_matches
