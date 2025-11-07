import joblib
import pickle
import pandas as pd
from app.logger_config import logger
import json
import os
from math import radians, sin, cos, sqrt, atan2

class BloodMatchingService:

    def __init__(self):
        logger.info("Initializing Blood Matching Service...")

        try:
            base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            ml_artifacts_dir = os.path.join(base_dir, 'ml_artifacts')

            self.model = joblib.load(os.path.join(ml_artifacts_dir, 'blood_matching_model.pkl'))
            self.scaler = joblib.load(os.path.join(ml_artifacts_dir, 'scaler.pkl'))
            self.label_encoders = joblib.load(os.path.join(ml_artifacts_dir, 'label_encoders.pkl'))

            with open(os.path.join(ml_artifacts_dir, 'feature_columns.pkl'), 'rb') as f:
                self.feature_cols = pickle.load(f)

            with open(os.path.join(ml_artifacts_dir, 'model_metadata.json'), 'r') as f:
                self.metadata = json.load(f)

            logger.info(f"Model: {self.metadata['model_type']}")
            logger.info(f"Accuracy: {self.metadata['accuracy']:.4f}")
            logger.info(f"Features: {len(self.feature_cols)}\n")

        except Exception as e:
            logger.error(f"Failed to load model: {str(e)}")
            raise

    def calculate_distance(self, lat1, lon1, lat2, lon2):
        R = 6371
        lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
        dlat = lat2 - lat1
        dlon = lon2 - lon1
        a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
        c = 2 * atan2(sqrt(a), sqrt(1-a))
        return R * c

    def prepare_features(self, donation, recipient):
        features = {}

        features['donor_age'] = donation.age or 35
        features['donor_weight'] = donation.weight or 70.0
        features['donor_height'] = donation.height or 175.0
        features['donor_bmi'] = donation.bmi or 23.0
        features['donor_blood_type'] = str(donation.bloodType).replace('BloodType.', '').strip()
        features['donor_hemoglobin'] = donation.hemoglobinLevel or 14.0
        features['donor_blood_glucose'] = donation.bloodGlucoseLevel or 100.0
        features['donor_has_diabetes'] = int(donation.hasDiabetes or False)
        features['donor_has_diseases'] = int(donation.hasDiseases or False)
        features['donor_has_infectious'] = int(donation.hasInfectiousDiseases or False)
        features['donor_medical_clearance'] = int(donation.medicalClearance or True)
        features['donor_recent_tattoo'] = int(donation.recentTattoo or False)
        features['donor_recent_vaccination'] = int(donation.recentVaccination or False)
        features['donor_recent_surgery'] = int(donation.recentSurgery or False)
        features['donor_days_since_last_donation'] = donation.daysSinceLastDonation or 180
        features['donor_smoking'] = str(donation.smokingStatus or 'NEVER_SMOKED').replace('SmokingStatus.','').strip()
        features['donor_pack_years'] = donation.packYears or 0
        features['donor_alcohol'] = str(donation.alcoholStatus or 'NO_ALCOHOL_USE').replace('AlcoholStatus.','').strip()
        features['donor_drinks_per_week'] = donation.drinksPerWeek or 0
        features['donor_latitude'] = donation.latitude or 17.4399
        features['donor_longitude'] = donation.longitude or 78.3489
        features['donor_body_size'] = str(donation.bodySize or 'MEDIUM').replace('BodySize.', '').strip()

        features['recipient_age'] = recipient.age or 45
        features['recipient_weight'] = recipient.weight or 65.0
        features['recipient_height'] = recipient.height or 170.0
        features['recipient_bmi'] = recipient.bmi or 22.5
        features['recipient_blood_type'] = str(recipient.requestedBloodType).replace('BloodType.', '').strip()
        features['recipient_hemoglobin'] = recipient.hemoglobinLevel or 11.0
        features['recipient_blood_glucose'] = recipient.bloodGlucoseLevel or 110.0
        features['recipient_has_diabetes'] = int(recipient.hasDiabetes or False)
        features['recipient_has_infectious'] = int(recipient.hasInfectiousDiseases or False)
        features['recipient_urgency'] = str(recipient.urgencyLevel or 'MEDIUM').replace('UrgencyLevel.', '').strip()
        features['recipient_days_waiting'] = recipient.daysWaiting or 30
        features['recipient_smoking'] = str(recipient.smokingStatus or 'NEVER_SMOKED').replace('SmokingStatus.','').strip()
        features['recipient_pack_years'] = recipient.packYears or 0
        features['recipient_alcohol'] = str(recipient.alcoholStatus or 'MODERATE_USE').replace('AlcoholStatus.','').strip()
        features['recipient_drinks_per_week'] = recipient.drinksPerWeek or 2
        features['recipient_latitude'] = recipient.latitude or 17.4450
        features['recipient_longitude'] = recipient.longitude or 78.3550
        features['recipient_body_size'] = str(recipient.bodySize or 'MEDIUM').replace('BodySize.', '').strip()

        distance = self.calculate_distance(
            features['donor_latitude'], features['donor_longitude'],
            features['recipient_latitude'], features['recipient_longitude']
        )
        features['distance_km'] = distance
        features['same_area'] = 0
        features['donation_quantity'] = donation.quantity or 450.0
        features['request_quantity'] = recipient.quantity or 450.0
        features['quantity_match'] = features['donation_quantity'] / features['request_quantity'] if features['request_quantity'] > 0 else 0.0
        features['blood_compatible'] = 1

        return features

    def predict_match(self, donation, recipient):
        try:
            features = self.prepare_features(donation, recipient)

            df = pd.DataFrame([features])

            for col in self.metadata['categorical_features']:
                if col in df.columns and col in self.label_encoders:
                    df[col] = self.label_encoders[col].transform(df[col].astype(str))

            df_final = df[self.feature_cols].copy()
            df_scaled = self.scaler.transform(df_final)

            prediction = self.model.predict(df_scaled)[0]
            prediction_proba = self.model.predict_proba(df_scaled)[0]

            return {
                'match_score': float(prediction_proba[1]),
                'is_good_match': bool(prediction == 1),
                'confidence': float(prediction_proba[1])
            }

        except Exception as e:
            logger.error(f"Prediction error: {str(e)}")
            return {
                'match_score': 0.0,
                'is_good_match': False,
                'confidence': 0.0
            }

_blood_matching_service = None

def get_blood_matching_service():
    global _blood_matching_service
    if _blood_matching_service is None:
        _blood_matching_service = BloodMatchingService()
    return _blood_matching_service
