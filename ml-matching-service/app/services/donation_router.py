from app.logger_config import logger
from app.services.blood_matching_logic import BloodMatchingLogic

class DonationRouter:

    @staticmethod
    def get_donation_types(donations):
        types = set()
        for donation in donations:
            donation_type = donation.donationType if isinstance(donation.donationType, str) else donation.donationType.value
            types.add(donation_type)
        return types

    @staticmethod
    def get_request_types(requests):
        types = set()
        for request in requests:
            request_type = request.requestType if isinstance(request.requestType, str) else request.requestType.value
            types.add(request_type)
        return types

    @staticmethod
    def validate_type_match(donation_type, request_type):
        valid_pairs = {
            'BLOOD': ['BLOOD'],
            'ORGAN': ['ORGAN'],
            'TISSUE': ['TISSUE'],
            'STEM_CELL': ['STEM_CELL'],
        }
        return request_type in valid_pairs.get(donation_type, [])

    @staticmethod
    def categorize_batch(donations, requests):
        donation_types = DonationRouter.get_donation_types(donations)
        request_types = DonationRouter.get_request_types(requests)

        logger.info(f"Categorizing batch...")
        logger.info(f"Donation types: {list(donation_types)}")
        logger.info(f"Request types: {list(request_types)}")

        categorized = {}

        for donation_type in donation_types:
            for request_type in request_types:
                if DonationRouter.validate_type_match(donation_type, request_type):
                    key = f"{donation_type}_{request_type}"

                    batch_donations = [d for d in donations if (d.donationType if isinstance(d.donationType, str) else d.donationType.value) == donation_type]
                    batch_requests = [r for r in requests if (r.requestType if isinstance(r.requestType, str) else r.requestType.value) == request_type]

                    if batch_donations and batch_requests:
                        categorized[key] = {
                            'donation_type': donation_type,
                            'request_type': request_type,
                            'donations': batch_donations,
                            'requests': batch_requests
                        }
                        logger.info(f"{key}: {len(batch_donations)} x {len(batch_requests)}")

        return categorized

    @staticmethod
    def route_to_service(categorized_batch, top_n, threshold):
        all_matches = []

        for batch_key, batch_data in categorized_batch.items():
            logger.info(f"Routing to {batch_key}...")

            if batch_key == 'BLOOD_BLOOD':
                logic = BloodMatchingLogic()
                matches = logic.batch_match(
                    batch_data['donations'],
                    batch_data['requests'],
                    top_n=top_n,
                    threshold=threshold
                )
                all_matches.extend(matches)
                logger.info(f"{batch_key}: {len(matches)} matches")
            else:
                logger.warning(f"No service: {batch_key}")

        return all_matches
