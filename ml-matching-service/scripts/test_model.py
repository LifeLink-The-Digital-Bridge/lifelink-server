import pickle
import joblib
import pandas as pd
import json
import os


class SimpleDonation:
    def __init__(self, **kwargs):
        for key, value in kwargs.items():
            setattr(self, key, value)


class SimpleRequest:
    def __init__(self, **kwargs):
        for key, value in kwargs.items():
            setattr(self, key, value)


BLOOD_COMPATIBILITY = {
    'O_NEGATIVE': ['O_NEGATIVE', 'O_POSITIVE', 'A_NEGATIVE', 'A_POSITIVE', 'B_NEGATIVE', 'B_POSITIVE', 'AB_NEGATIVE',
                   'AB_POSITIVE'],
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
    def apply_all_filters(donation, request):
        donor_blood = str(donation.bloodType).replace('BloodType.', '').strip()
        recipient_blood = str(request.requestedBloodType).replace('BloodType.', '').strip()
        is_compatible = recipient_blood in BLOOD_COMPATIBILITY.get(donor_blood, [])

        if not is_compatible:
            return False, f"{donor_blood} incompatible with {recipient_blood}", 0.0
        if donation.hasInfectiousDiseases:
            return False, "Has infectious diseases", 0.0
        if donation.quantity < request.quantity:
            return False, f"Low qty: {donation.quantity}ml < {request.quantity}ml", 0.0
        if donation.hemoglobinLevel < 12.5:
            return False, f"Low hemoglobin: {donation.hemoglobinLevel}", 0.0

        hard_score = BloodTypeFilters.calculate_hard_score(donation, request)
        return True, f"{donor_blood} -> {recipient_blood}", hard_score

    @staticmethod
    def calculate_hard_score(donation, request):
        score = 0.40
        if donation.hemoglobinLevel >= 14.0:
            score += 0.15
        if not (donation.recentTattoo or donation.recentSurgery):
            score += 0.10
        if donation.medicalClearance and not donation.hasDiseases:
            score += 0.15
        if donation.quantity >= request.quantity:
            score += 0.10
        if not donation.hasInfectiousDiseases:
            score += 0.10
        return min(max(score, 0.0), 1.0)


def load_ml_model():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    ml_artifacts_dir = os.path.join(base_dir, 'ml_artifacts')

    model = joblib.load(os.path.join(ml_artifacts_dir, 'blood_matching_model.pkl'))
    scaler = joblib.load(os.path.join(ml_artifacts_dir, 'scaler.pkl'))
    label_encoders = joblib.load(os.path.join(ml_artifacts_dir, 'label_encoders.pkl'))

    with open(os.path.join(ml_artifacts_dir, 'feature_columns.pkl'), 'rb') as f:
        feature_cols = pickle.load(f)

    with open(os.path.join(ml_artifacts_dir, 'model_metadata.json'), 'r') as f:
        metadata = json.load(f)

    return model, scaler, label_encoders, feature_cols, metadata


def get_ml_score(donation, request, model, scaler, label_encoders, feature_cols, metadata):
    try:
        donor_blood = str(donation.bloodType).replace('BloodType.', '').strip()
        recipient_blood = str(request.requestedBloodType).replace('BloodType.', '').strip()

        is_compatible = recipient_blood in BLOOD_COMPATIBILITY.get(donor_blood, [])
        blood_compatible = 1 if is_compatible else 0

        features = {
            'donor_age': donation.age or 35,
            'donor_weight': donation.weight or 70.0,
            'donor_height': donation.height or 175.0,
            'donor_bmi': donation.bmi or 23.0,
            'donor_hemoglobin': donation.hemoglobinLevel or 14.0,
            'donor_blood_glucose': donation.bloodGlucoseLevel or 100.0,
            'donor_has_diabetes': int(donation.hasDiabetes or False),
            'donor_has_diseases': int(donation.hasDiseases or False),
            'donor_has_infectious': int(donation.hasInfectiousDiseases or False),
            'donor_medical_clearance': int(donation.medicalClearance or True),
            'donor_recent_tattoo': int(donation.recentTattoo or False),
            'donor_recent_vaccination': int(donation.recentVaccination or False),
            'donor_recent_surgery': int(donation.recentSurgery or False),
            'donor_days_since_last_donation': donation.daysSinceLastDonation or 180,
            'donor_smoking': str(donation.smokingStatus or 'NEVER_SMOKED').replace('SmokingStatus.', '').strip(),
            'donor_pack_years': donation.packYears or 0,
            'donor_alcohol': str(donation.alcoholStatus or 'NO_ALCOHOL_USE').replace('AlcoholStatus.', '').strip(),
            'donor_drinks_per_week': donation.drinksPerWeek or 0,
            'donor_latitude': donation.latitude or 17.4399,
            'donor_longitude': donation.longitude or 78.3489,
            'donor_body_size': str(donation.bodySize or 'MEDIUM').replace('BodySize.', '').strip(),
            'recipient_age': request.age or 45,
            'recipient_weight': request.weight or 65.0,
            'recipient_height': request.height or 170.0,
            'recipient_bmi': request.bmi or 22.5,
            'recipient_hemoglobin': request.hemoglobinLevel or 11.0,
            'recipient_blood_glucose': request.bloodGlucoseLevel or 110.0,
            'recipient_has_diabetes': int(request.hasDiabetes or False),
            'recipient_has_infectious': int(request.hasInfectiousDiseases or False),
            'recipient_urgency': str(request.urgencyLevel or 'MEDIUM').replace('UrgencyLevel.', '').strip(),
            'recipient_days_waiting': request.daysWaiting or 30,
            'recipient_smoking': str(request.smokingStatus or 'NEVER_SMOKED').replace('SmokingStatus.', '').strip(),
            'recipient_pack_years': request.packYears or 0,
            'recipient_alcohol': str(request.alcoholStatus or 'MODERATE_USE').replace('AlcoholStatus.', '').strip(),
            'recipient_drinks_per_week': request.drinksPerWeek or 2,
            'recipient_latitude': request.latitude or 17.4450,
            'recipient_longitude': request.longitude or 78.3550,
            'recipient_body_size': str(request.bodySize or 'MEDIUM').replace('BodySize.', '').strip(),
            'donation_quantity': donation.quantity or 450.0,
            'request_quantity': request.quantity or 450.0,
            'blood_compatible': blood_compatible
        }

        df = pd.DataFrame([features])

        for col in metadata['categorical_features']:
            if col in df.columns and col in label_encoders:
                df[col] = label_encoders[col].transform(df[col].astype(str))

        df_final = df[feature_cols].copy()
        df_scaled = scaler.transform(df_final)

        prediction_proba = model.predict_proba(df_scaled)[0]
        return float(prediction_proba[1])

    except Exception as e:
        print(f"   ML Error: {str(e)}")
        return 0.0


def test_matching(name, donation_dict, request_dict, model, scaler, label_encoders, feature_cols, metadata):
    donation = SimpleDonation(**donation_dict)
    request = SimpleRequest(**request_dict)

    passed, reason, hard_score = BloodTypeFilters.apply_all_filters(donation, request)

    if not passed:
        print(f"   BLOCKED: {reason}")
        return None

    ml_score = get_ml_score(donation, request, model, scaler, label_encoders, feature_cols, metadata)
    combined_score = (hard_score * 0.3) + (ml_score * 0.7)

    result = " MATCH" if combined_score >= 0.5 else "NO MATCH"
    print(f"   {result} | Hard: {hard_score:.3f} (30%) | ML: {ml_score:.3f} (70%) | Combined: {combined_score:.3f}")

    return combined_score


def main():
    print("\n" + "=" * 120)
    print("COMPREHENSIVE HYBRID MATCHING TEST (30% Rule + 70% ML)")
    print("=" * 120)

    model, scaler, label_encoders, feature_cols, metadata = load_ml_model()
    print(f"\nModel: {metadata['model_type']}")
    print(f"   Accuracy: {metadata['accuracy']:.4f} | AUC-ROC: {metadata['auc_score']:.4f}")
    print(f"   Precision: {metadata['precision']:.4f} | Recall: {metadata['recall']:.4f}\n")

    tests = {
        "BLOOD TYPE COMPATIBILITY TESTS": [
            ("Perfect Match - O_NEGATIVE (Universal) to AB_POSITIVE (Universal)",
             {'bloodType': 'O_NEGATIVE', 'hemoglobinLevel': 16.0, 'age': 28, 'weight': 70.0, 'height': 175.0,
              'bmi': 22.9,
              'bloodGlucoseLevel': 90.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 150, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 500},
             {'requestedBloodType': 'AB_POSITIVE', 'age': 50, 'weight': 80.0, 'height': 178.0, 'bmi': 25.2,
              'hemoglobinLevel': 9.5,
              'bloodGlucoseLevel': 125.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False,
              'urgencyLevel': 'CRITICAL',
              'daysWaiting': 1, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 3, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'LARGE', 'quantity': 450}),

            ("Same Type Match - A_POSITIVE to A_POSITIVE",
             {'bloodType': 'A_POSITIVE', 'hemoglobinLevel': 15.0, 'age': 30, 'weight': 75.0, 'height': 180.0,
              'bmi': 23.1,
              'bloodGlucoseLevel': 95.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 120, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 450},
             {'requestedBloodType': 'A_POSITIVE', 'age': 45, 'weight': 65.0, 'height': 170.0, 'bmi': 22.5,
              'hemoglobinLevel': 10.5,
              'bloodGlucoseLevel': 110.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'HIGH',
              'daysWaiting': 5, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 2, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 450}),

            ("BLOCKED - Incompatible A_POSITIVE to B_POSITIVE",
             {'bloodType': 'A_POSITIVE', 'hemoglobinLevel': 14.5, 'age': 35, 'weight': 68.0, 'height': 172.0,
              'bmi': 23.0,
              'bloodGlucoseLevel': 100.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 100, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 450},
             {'requestedBloodType': 'B_POSITIVE', 'age': 40, 'weight': 70.0, 'height': 175.0, 'bmi': 22.9,
              'hemoglobinLevel': 11.0,
              'bloodGlucoseLevel': 105.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False,
              'urgencyLevel': 'MEDIUM',
              'daysWaiting': 20, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 1, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 450}),

            ("Rh Negative Donor to Rh Positive - B_NEGATIVE to B_POSITIVE",
             {'bloodType': 'B_NEGATIVE', 'hemoglobinLevel': 14.0, 'age': 33, 'weight': 72.0, 'height': 176.0,
              'bmi': 23.3,
              'bloodGlucoseLevel': 98.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 110, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 450},
             {'requestedBloodType': 'B_POSITIVE', 'age': 42, 'weight': 71.0, 'height': 174.0, 'bmi': 23.4,
              'hemoglobinLevel': 11.2,
              'bloodGlucoseLevel': 108.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False,
              'urgencyLevel': 'MEDIUM',
              'daysWaiting': 15, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 2, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 450}),
        ],

        "MEDICAL CONDITION TESTS": [
            ("BLOCKED - Low Hemoglobin",
             {'bloodType': 'B_POSITIVE', 'hemoglobinLevel': 11.5, 'age': 32, 'weight': 60.0, 'height': 165.0,
              'bmi': 22.0,
              'bloodGlucoseLevel': 95.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 90, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'SMALL', 'quantity': 350},
             {'requestedBloodType': 'B_POSITIVE', 'age': 38, 'weight': 72.0, 'height': 176.0, 'bmi': 23.2,
              'hemoglobinLevel': 10.0,
              'bloodGlucoseLevel': 110.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'HIGH',
              'daysWaiting': 7, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 2, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 350}),

            ("BLOCKED - Infectious Diseases",
             {'bloodType': 'O_POSITIVE', 'hemoglobinLevel': 15.0, 'age': 30, 'weight': 75.0, 'height': 180.0,
              'bmi': 23.1,
              'bloodGlucoseLevel': 95.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': True,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 120, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 450},
             {'requestedBloodType': 'O_POSITIVE', 'age': 45, 'weight': 65.0, 'height': 170.0, 'bmi': 22.5,
              'hemoglobinLevel': 10.5,
              'bloodGlucoseLevel': 110.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'HIGH',
              'daysWaiting': 5, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 2, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 450}),

            ("Healthy High Hemoglobin - A_NEGATIVE to A_NEGATIVE",
             {'bloodType': 'A_NEGATIVE', 'hemoglobinLevel': 16.5, 'age': 25, 'weight': 65.0, 'height': 172.0,
              'bmi': 21.9,
              'bloodGlucoseLevel': 85.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 95, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 450},
             {'requestedBloodType': 'A_NEGATIVE', 'age': 60, 'weight': 58.0, 'height': 162.0, 'bmi': 22.1,
              'hemoglobinLevel': 10.2,
              'bloodGlucoseLevel': 115.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'LOW',
              'daysWaiting': 60, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'NO_ALCOHOL_USE',
              'drinksPerWeek': 0, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'SMALL', 'quantity': 350}),

            ("Diabetic Donor - O_POSITIVE to O_POSITIVE",
             {'bloodType': 'O_POSITIVE', 'hemoglobinLevel': 14.5, 'age': 42, 'weight': 85.0, 'height': 176.0,
              'bmi': 27.4,
              'bloodGlucoseLevel': 145.0, 'hasDiabetes': True, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 130, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'MODERATE_USE', 'drinksPerWeek': 3, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'LARGE', 'quantity': 450},
             {'requestedBloodType': 'O_POSITIVE', 'age': 38, 'weight': 68.0, 'height': 170.0, 'bmi': 23.4,
              'hemoglobinLevel': 9.8,
              'bloodGlucoseLevel': 140.0, 'hasDiabetes': True, 'hasInfectiousDiseases': False, 'urgencyLevel': 'MEDIUM',
              'daysWaiting': 30, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 2, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 450}),
        ],

        "LIFESTYLE RISK TESTS": [
            ("Smoker Donor - A_POSITIVE to A_POSITIVE",
             {'bloodType': 'A_POSITIVE', 'hemoglobinLevel': 14.0, 'age': 45, 'weight': 80.0, 'height': 178.0,
              'bmi': 25.2,
              'bloodGlucoseLevel': 105.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 120, 'smokingStatus': 'CURRENT_SMOKER', 'packYears': 15,
              'alcoholStatus': 'MODERATE_USE', 'drinksPerWeek': 7, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'LARGE', 'quantity': 450},
             {'requestedBloodType': 'A_POSITIVE', 'age': 35, 'weight': 65.0, 'height': 168.0, 'bmi': 23.0,
              'hemoglobinLevel': 11.5,
              'bloodGlucoseLevel': 100.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False,
              'urgencyLevel': 'MEDIUM',
              'daysWaiting': 15, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'NO_ALCOHOL_USE',
              'drinksPerWeek': 0, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 450}),

            ("Heavy Alcohol Use - B_POSITIVE to B_POSITIVE",
             {'bloodType': 'B_POSITIVE', 'hemoglobinLevel': 13.5, 'age': 38, 'weight': 78.0, 'height': 174.0,
              'bmi': 25.7,
              'bloodGlucoseLevel': 115.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 110, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'HEAVY_USE', 'drinksPerWeek': 12, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 450},
             {'requestedBloodType': 'B_POSITIVE', 'age': 44, 'weight': 75.0, 'height': 171.0, 'bmi': 25.6,
              'hemoglobinLevel': 10.8,
              'bloodGlucoseLevel': 120.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'HIGH',
              'daysWaiting': 10, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 4, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 450}),

            ("Recent Tattoo - AB_NEGATIVE to AB_NEGATIVE",
             {'bloodType': 'AB_NEGATIVE', 'hemoglobinLevel': 14.5, 'age': 29, 'weight': 68.0, 'height': 170.0,
              'bmi': 23.5,
              'bloodGlucoseLevel': 92.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': False, 'recentTattoo': True, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 200, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'MODERATE_USE', 'drinksPerWeek': 3, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 400},
             {'requestedBloodType': 'AB_NEGATIVE', 'age': 55, 'weight': 62.0, 'height': 164.0, 'bmi': 23.0,
              'hemoglobinLevel': 9.8,
              'bloodGlucoseLevel': 105.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'LOW',
              'daysWaiting': 50, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'NO_ALCOHOL_USE',
              'drinksPerWeek': 0, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'SMALL', 'quantity': 350}),
        ],

        "URGENCY PRIORITY TESTS": [
            ("Critical Urgency - O_POSITIVE to A_POSITIVE",
             {'bloodType': 'O_POSITIVE', 'hemoglobinLevel': 16.5, 'age': 26, 'weight': 72.0, 'height': 177.0,
              'bmi': 23.0,
              'bloodGlucoseLevel': 88.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 200, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 500},
             {'requestedBloodType': 'A_POSITIVE', 'age': 18, 'weight': 55.0, 'height': 165.0, 'bmi': 20.2,
              'hemoglobinLevel': 8.5,
              'bloodGlucoseLevel': 90.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False,
              'urgencyLevel': 'CRITICAL',
              'daysWaiting': 1, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'NO_ALCOHOL_USE',
              'drinksPerWeek': 0, 'latitude': 17.442, 'longitude': 78.351, 'bodySize': 'SMALL', 'quantity': 350}),

            ("Low Urgency - B_NEGATIVE to B_NEGATIVE",
             {'bloodType': 'B_NEGATIVE', 'hemoglobinLevel': 13.8, 'age': 40, 'weight': 70.0, 'height': 172.0,
              'bmi': 23.6,
              'bloodGlucoseLevel': 100.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 125, 'smokingStatus': 'FORMER_SMOKER', 'packYears': 5,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 450},
             {'requestedBloodType': 'B_NEGATIVE', 'age': 62, 'weight': 60.0, 'height': 165.0, 'bmi': 22.0,
              'hemoglobinLevel': 10.5,
              'bloodGlucoseLevel': 118.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'LOW',
              'daysWaiting': 120, 'smokingStatus': 'FORMER_SMOKER', 'packYears': 10, 'alcoholStatus': 'NO_ALCOHOL_USE',
              'drinksPerWeek': 0, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'SMALL', 'quantity': 350}),
        ],

        "QUANTITY MISMATCH TESTS": [
            ("BLOCKED - Low Donation Quantity",
             {'bloodType': 'O_POSITIVE', 'hemoglobinLevel': 15.0, 'age': 30, 'weight': 75.0, 'height': 180.0,
              'bmi': 23.1,
              'bloodGlucoseLevel': 95.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 120, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 300},
             {'requestedBloodType': 'O_POSITIVE', 'age': 45, 'weight': 65.0, 'height': 170.0, 'bmi': 22.5,
              'hemoglobinLevel': 10.5,
              'bloodGlucoseLevel': 110.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'HIGH',
              'daysWaiting': 5, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 2, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 450}),

            ("Sufficient Quantity - O_NEGATIVE to B_POSITIVE",
             {'bloodType': 'O_NEGATIVE', 'hemoglobinLevel': 15.2, 'age': 31, 'weight': 73.0, 'height': 176.0,
              'bmi': 23.5,
              'bloodGlucoseLevel': 92.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 140, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 500},
             {'requestedBloodType': 'B_POSITIVE', 'age': 41, 'weight': 70.0, 'height': 175.0, 'bmi': 22.9,
              'hemoglobinLevel': 10.8,
              'bloodGlucoseLevel': 108.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False,
              'urgencyLevel': 'MEDIUM',
              'daysWaiting': 18, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 2, 'latitude': 17.445, 'longitude': 78.355, 'bodySize': 'MEDIUM', 'quantity': 450}),
        ],

        "CROSS-TYPE COMPATIBILITY TESTS": [
            ("O_NEG to O_POS - Same base different Rh",
             {'bloodType': 'O_NEGATIVE', 'hemoglobinLevel': 15.8, 'age': 27, 'weight': 68.0, 'height': 172.0,
              'bmi': 23.0,
              'bloodGlucoseLevel': 88.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 150, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 450},
             {'requestedBloodType': 'O_POSITIVE', 'age': 35, 'weight': 72.0, 'height': 175.0, 'bmi': 23.5,
              'hemoglobinLevel': 10.2,
              'bloodGlucoseLevel': 102.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'HIGH',
              'daysWaiting': 8, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 3, 'latitude': 17.442, 'longitude': 78.351, 'bodySize': 'MEDIUM', 'quantity': 450}),

            ("O_NEG to A_NEG - Cross-type compatible",
             {'bloodType': 'O_NEGATIVE', 'hemoglobinLevel': 16.2, 'age': 29, 'weight': 70.0, 'height': 174.0,
              'bmi': 23.1,
              'bloodGlucoseLevel': 90.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 160, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 450},
             {'requestedBloodType': 'A_NEGATIVE', 'age': 52, 'weight': 65.0, 'height': 168.0, 'bmi': 23.0,
              'hemoglobinLevel': 9.8,
              'bloodGlucoseLevel': 115.0, 'hasDiabetes': True, 'hasInfectiousDiseases': False,
              'urgencyLevel': 'CRITICAL',
              'daysWaiting': 2, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'NO_ALCOHOL_USE',
              'drinksPerWeek': 0, 'latitude': 17.443, 'longitude': 78.352, 'bodySize': 'MEDIUM', 'quantity': 450}),

            ("O_POS to AB_POS - Positive universal",
             {'bloodType': 'O_POSITIVE', 'hemoglobinLevel': 15.5, 'age': 32, 'weight': 75.0, 'height': 178.0,
              'bmi': 23.7,
              'bloodGlucoseLevel': 95.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 120, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 500},
             {'requestedBloodType': 'AB_POSITIVE', 'age': 48, 'weight': 78.0, 'height': 176.0, 'bmi': 25.2,
              'hemoglobinLevel': 10.5,
              'bloodGlucoseLevel': 118.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False, 'urgencyLevel': 'HIGH',
              'daysWaiting': 6, 'smokingStatus': 'FORMER_SMOKER', 'packYears': 8, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 4, 'latitude': 17.444, 'longitude': 78.353, 'bodySize': 'LARGE', 'quantity': 450}),

            ("A_NEG to AB_NEG - Negative cross-type",
             {'bloodType': 'A_NEGATIVE', 'hemoglobinLevel': 14.8, 'age': 36, 'weight': 67.0, 'height': 170.0,
              'bmi': 23.2,
              'bloodGlucoseLevel': 93.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 135, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'NO_ALCOHOL_USE', 'drinksPerWeek': 0, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'MEDIUM', 'quantity': 400},
             {'requestedBloodType': 'AB_NEGATIVE', 'age': 61, 'weight': 60.0, 'height': 163.0, 'bmi': 22.6,
              'hemoglobinLevel': 10.0,
              'bloodGlucoseLevel': 122.0, 'hasDiabetes': False, 'hasInfectiousDiseases': False,
              'urgencyLevel': 'MEDIUM',
              'daysWaiting': 45, 'smokingStatus': 'FORMER_SMOKER', 'packYears': 12, 'alcoholStatus': 'NO_ALCOHOL_USE',
              'drinksPerWeek': 0, 'latitude': 17.446, 'longitude': 78.354, 'bodySize': 'SMALL', 'quantity': 350}),

            ("B_POS to AB_POS - Positive cross-type",
             {'bloodType': 'B_POSITIVE', 'hemoglobinLevel': 14.2, 'age': 40, 'weight': 82.0, 'height': 180.0,
              'bmi': 25.3,
              'bloodGlucoseLevel': 102.0, 'hasDiabetes': False, 'hasDiseases': False, 'hasInfectiousDiseases': False,
              'medicalClearance': True, 'recentTattoo': False, 'recentVaccination': False, 'recentSurgery': False,
              'daysSinceLastDonation': 110, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0,
              'alcoholStatus': 'MODERATE_USE', 'drinksPerWeek': 5, 'latitude': 17.4399, 'longitude': 78.3489,
              'bodySize': 'LARGE', 'quantity': 450},
             {'requestedBloodType': 'AB_POSITIVE', 'age': 55, 'weight': 68.0, 'height': 168.0, 'bmi': 24.1,
              'hemoglobinLevel': 9.5,
              'bloodGlucoseLevel': 128.0, 'hasDiabetes': True, 'hasInfectiousDiseases': False, 'urgencyLevel': 'HIGH',
              'daysWaiting': 12, 'smokingStatus': 'NEVER_SMOKED', 'packYears': 0, 'alcoholStatus': 'MODERATE_USE',
              'drinksPerWeek': 3, 'latitude': 17.441, 'longitude': 78.350, 'bodySize': 'MEDIUM', 'quantity': 450}),
        ],
    }

    total_passed = 0
    total_blocked = 0
    category_results = {}

    for category, test_list in tests.items():
        print(f"\n{category}")
        print("-" * 120)
        category_passed = 0
        category_blocked = 0

        for name, donor, recipient in test_list:
            print(f"\n   {name}")
            result = test_matching(name, donor, recipient, model, scaler, label_encoders, feature_cols, metadata)
            if result is not None:
                category_passed += 1
                total_passed += 1
            else:
                category_blocked += 1
                total_blocked += 1

        category_results[category] = (category_passed, category_blocked)

    print("\n" + "=" * 120)
    print("TEST SUMMARY")
    print("=" * 120)
    for category, (passed, blocked) in category_results.items():
        total = passed + blocked
        print(f"{category}: {passed}/{total} matches | {blocked} blocked")

    print(f"\n{'=' * 120}")
    print(f"TOTAL: {total_passed + total_blocked} tests | {total_passed} matches | {total_blocked} blocked")
    print(f"{'=' * 120}\n")


if __name__ == '__main__':
    main()
