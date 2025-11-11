import pandas as pd
import numpy as np
import random
import os
from math import radians, sin, cos, sqrt, atan2

BLOOD_TYPE_DISTRIBUTION = {
    'O_NEGATIVE': 0.08,
    'O_POSITIVE': 0.37,
    'A_NEGATIVE': 0.066,
    'A_POSITIVE': 0.344,
    'B_NEGATIVE': 0.019,
    'B_POSITIVE': 0.081,
    'AB_NEGATIVE': 0.008,
    'AB_POSITIVE': 0.032
}

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

BLOOD_TYPES = list(BLOOD_COMPATIBILITY.keys())
URGENCY_LEVELS = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']
SMOKING_STATUS = ['NEVER_SMOKED', 'FORMER_SMOKER', 'CURRENT_SMOKER', 'OCCASIONAL_SMOKER']
ALCOHOL_STATUS = ['NO_ALCOHOL_USE', 'MODERATE_USE', 'HEAVY_USE', 'FORMER_USER']

random.seed(42)
np.random.seed(42)


def calculate_distance(lat1, lon1, lat2, lon2):
    R = 6371
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c


def calculate_bmi_and_size(weight, height):
    bmi = round(weight / ((height / 100) ** 2), 1)
    body_size = 'SMALL' if bmi < 18.5 else ('MEDIUM' if bmi < 25 else 'LARGE')
    return bmi, body_size


def apply_distance_offset(donor_lat, donor_lon, distance_km):
    lat_offset = (distance_km / 111.0) * random.choice([-1, 1])
    lon_offset = (distance_km / (111.0 * np.cos(np.radians(donor_lat)))) * random.choice([-1, 1])
    return donor_lat + lat_offset, donor_lon + lon_offset


def generate_donor():
    blood_type = np.random.choice(list(BLOOD_TYPE_DISTRIBUTION.keys()),
                                  p=list(BLOOD_TYPE_DISTRIBUTION.values()))

    age = random.randint(18, 65)
    weight = round(random.uniform(50, 100), 1)
    height = round(random.uniform(150, 190), 1)
    bmi, body_size = calculate_bmi_and_size(weight, height)

    hemoglobin = round(random.uniform(12.0, 17.0), 1)
    blood_glucose = round(random.uniform(70, 120), 1)
    has_diabetes = random.random() < 0.1
    has_diseases = random.random() < 0.05
    has_infectious = random.random() < 0.02

    medical_clearance = not (has_diseases or has_infectious)
    recent_tattoo = random.random() < 0.1
    recent_surgery = random.random() < 0.05

    days_since_last = random.choice([None, random.randint(90, 365)])

    smoking = random.choice(SMOKING_STATUS)
    pack_years = random.randint(0, 20) if smoking != "NEVER_SMOKED" else 0
    alcohol = random.choice(ALCOHOL_STATUS)
    drinks_per_week = random.randint(0, 14) if alcohol not in ["NO_ALCOHOL_USE", "FORMER_USER"] else 0

    lat = random.uniform(17.3, 17.5)
    lon = random.uniform(78.3, 78.6)
    quantity = random.choice([350, 450, 500])

    return {
        'donor_blood_type': blood_type,
        'donor_age': age,
        'donor_weight': weight,
        'donor_height': height,
        'donor_bmi': bmi,
        'donor_body_size': body_size,
        'donor_hemoglobin': hemoglobin,
        'donor_blood_glucose': blood_glucose,
        'donor_has_diabetes': int(has_diabetes),
        'donor_has_diseases': int(has_diseases),
        'donor_has_infectious': int(has_infectious),
        'donor_medical_clearance': int(medical_clearance),
        'donor_recent_tattoo': int(recent_tattoo),
        'donor_recent_surgery': int(recent_surgery),
        'donor_days_since_last_donation': days_since_last if days_since_last else 999,
        'donor_smoking': smoking,
        'donor_pack_years': pack_years,
        'donor_alcohol': alcohol,
        'donor_drinks_per_week': drinks_per_week,
        'donor_latitude': round(lat, 4),
        'donor_longitude': round(lon, 4),
        'donation_quantity': quantity
    }


def generate_recipient():
    blood_type = np.random.choice(list(BLOOD_TYPE_DISTRIBUTION.keys()),
                                  p=list(BLOOD_TYPE_DISTRIBUTION.values()))

    age = random.randint(18, 80)
    weight = round(random.uniform(45, 110), 1)
    height = round(random.uniform(145, 195), 1)
    bmi, body_size = calculate_bmi_and_size(weight, height)

    hemoglobin = round(random.uniform(8.0, 14.0), 1)
    blood_glucose = round(random.uniform(80, 180), 1)
    has_diabetes = random.random() < 0.25
    has_infectious = random.random() < 0.05

    urgency = random.choice(URGENCY_LEVELS)
    days_waiting = random.randint(1, 180)

    smoking = random.choice(SMOKING_STATUS)
    pack_years = random.randint(0, 30) if smoking != "NEVER_SMOKED" else 0
    alcohol = random.choice(ALCOHOL_STATUS)
    drinks_per_week = random.randint(0, 14) if alcohol not in ["NO_ALCOHOL_USE", "FORMER_USER"] else 0

    lat = random.uniform(17.3, 17.5)
    lon = random.uniform(78.3, 78.6)
    quantity_needed = random.choice([350, 450, 500])

    return {
        'recipient_blood_type': blood_type,
        'recipient_age': age,
        'recipient_weight': weight,
        'recipient_height': height,
        'recipient_bmi': bmi,
        'recipient_body_size': body_size,
        'recipient_hemoglobin': hemoglobin,
        'recipient_blood_glucose': blood_glucose,
        'recipient_has_diabetes': int(has_diabetes),
        'recipient_has_infectious': int(has_infectious),
        'recipient_urgency': urgency,
        'recipient_days_waiting': days_waiting,
        'recipient_smoking': smoking,
        'recipient_pack_years': pack_years,
        'recipient_alcohol': alcohol,
        'recipient_drinks_per_week': drinks_per_week,
        'recipient_latitude': round(lat, 4),
        'recipient_longitude': round(lon, 4),
        'request_quantity': quantity_needed
    }


def generate_compatible_pair():
    donor = generate_donor()
    donor_blood = donor['donor_blood_type']

    compatible_types = BLOOD_COMPATIBILITY[donor_blood]

    compatible_weights = [BLOOD_TYPE_DISTRIBUTION[bt] for bt in compatible_types]
    total_weight = sum(compatible_weights)
    normalized_weights = [w / total_weight for w in compatible_weights]

    recipient_blood = np.random.choice(compatible_types, p=normalized_weights)

    recipient = generate_recipient()
    recipient['recipient_blood_type'] = recipient_blood

    distance_km = np.random.choice(
        [0.5, 2, 5, 10, 15, 20, 25],
        p=[0.2, 0.25, 0.2, 0.15, 0.1, 0.07, 0.03]
    )

    recipient['recipient_latitude'], recipient['recipient_longitude'] = apply_distance_offset(
        donor['donor_latitude'], donor['donor_longitude'], distance_km
    )

    if random.random() < 0.7:
        recipient['request_quantity'] = donor['donation_quantity']

    return donor, recipient


def generate_incompatible_pair():
    donor = generate_donor()
    donor_blood = donor['donor_blood_type']

    compatible_types = BLOOD_COMPATIBILITY[donor_blood]
    all_types = set(BLOOD_TYPES)
    incompatible_types = list(all_types - set(compatible_types))

    if not incompatible_types:
        return generate_incompatible_pair()

    recipient_blood = random.choice(incompatible_types)

    recipient = generate_recipient()
    recipient['recipient_blood_type'] = recipient_blood

    distance_km = np.random.choice(
        [0.5, 2, 5, 10, 15, 20, 25],
        p=[0.2, 0.25, 0.2, 0.15, 0.1, 0.07, 0.03]
    )

    recipient['recipient_latitude'], recipient['recipient_longitude'] = apply_distance_offset(
        donor['donor_latitude'], donor['donor_longitude'], distance_km
    )

    return donor, recipient


def calculate_match_quality(donor, recipient):
    score = 0.0

    if donor['donor_blood_type'] not in BLOOD_COMPATIBILITY.get(recipient['recipient_blood_type'], []):
        return 0.0
    score += 0.30

    distance = calculate_distance(
        donor['donor_latitude'], donor['donor_longitude'],
        recipient['recipient_latitude'], recipient['recipient_longitude']
    )
    if distance < 5:
        score += 0.20
    elif distance < 15:
        score += 0.12
    elif distance < 30:
        score += 0.05
    else:
        return 0.0

    if donor['donation_quantity'] >= recipient['request_quantity']:
        score += 0.15

    urgency_bonus = {
        'CRITICAL': 0.05,
        'HIGH': 0.03,
        'MEDIUM': 0.02,
        'LOW': 0.00
    }
    score += urgency_bonus.get(recipient['recipient_urgency'], 0.0)

    if donor['donor_hemoglobin'] >= 14.0:
        score += 0.08
    if donor['donor_has_diseases'] == 0:
        score += 0.04
    if donor['donor_has_infectious'] == 0:
        score += 0.03

    age_diff = abs(donor['donor_age'] - recipient['recipient_age'])
    if age_diff < 15:
        score += 0.08
    elif age_diff < 30:
        score += 0.04
    elif age_diff < 50:
        score += 0.02

    if donor['donor_medical_clearance']:
        score += 0.07

    return min(score, 1.0)


def generate_training_dataset(num_samples=50000):
    print(f"\n{'=' * 100}")
    print(f"GENERATING {num_samples:,} SAMPLES - 25% SAME / 35% CROSS / 40% INCOMPATIBLE")
    print(f"{'=' * 100}\n")

    data = []
    good_matches = 0
    incompatible = 0
    total_distance = 0

    same_type_ratio = 0.25
    cross_type_ratio = 0.35
    incompatible_ratio = 0.40

    same_type_samples = int(num_samples * same_type_ratio)
    cross_type_samples = int(num_samples * cross_type_ratio)
    incompatible_samples = int(num_samples * incompatible_ratio)

    print(f"Generating {same_type_samples:,} SAME-TYPE pairs...")
    for i in range(same_type_samples):
        if (i + 1) % 5000 == 0:
            print(f"  Same-type: {i + 1:,}/{same_type_samples:,}")

        donor = generate_donor()
        recipient = generate_recipient()
        recipient['recipient_blood_type'] = donor['donor_blood_type']

        distance_km = np.random.choice([0.5, 2, 5, 10, 15, 20, 25], p=[0.2, 0.25, 0.2, 0.15, 0.1, 0.07, 0.03])
        recipient['recipient_latitude'], recipient['recipient_longitude'] = apply_distance_offset(
            donor['donor_latitude'], donor['donor_longitude'], distance_km
        )
        if random.random() < 0.7:
            recipient['request_quantity'] = donor['donation_quantity']

        distance = calculate_distance(
            donor['donor_latitude'], donor['donor_longitude'],
            recipient['recipient_latitude'], recipient['recipient_longitude']
        )
        total_distance += distance
        match_quality = calculate_match_quality(donor, recipient)
        is_good_match = int(match_quality >= 0.6)
        if is_good_match:
            good_matches += 1

        sample = {
            **donor,
            **recipient,
            'distance_km': round(distance, 2),
            'blood_compatible': 1,
            'match_score': round(match_quality, 3),
            'is_good_match': is_good_match
        }
        data.append(sample)

    print(f"\nGenerating {cross_type_samples:,} CROSS-TYPE COMPATIBLE pairs...")
    for i in range(cross_type_samples):
        if (i + 1) % 5000 == 0:
            print(f"  Cross-type: {i + 1:,}/{cross_type_samples:,}")

        donor, recipient = generate_compatible_pair()
        distance = calculate_distance(
            donor['donor_latitude'], donor['donor_longitude'],
            recipient['recipient_latitude'], recipient['recipient_longitude']
        )
        total_distance += distance
        match_quality = calculate_match_quality(donor, recipient)
        is_good_match = int(match_quality >= 0.6)
        if is_good_match:
            good_matches += 1

        sample = {
            **donor,
            **recipient,
            'distance_km': round(distance, 2),
            'blood_compatible': 1,
            'match_score': round(match_quality, 3),
            'is_good_match': is_good_match
        }
        data.append(sample)

    print(f"\nGenerating {incompatible_samples:,} INCOMPATIBLE pairs...")
    for i in range(incompatible_samples):
        if (i + 1) % 5000 == 0:
            print(f"  Incompatible: {i + 1:,}/{incompatible_samples:,}")

        donor, recipient = generate_incompatible_pair()
        distance = calculate_distance(
            donor['donor_latitude'], donor['donor_longitude'],
            recipient['recipient_latitude'], recipient['recipient_longitude']
        )
        total_distance += distance
        incompatible += 1

        sample = {
            **donor,
            **recipient,
            'distance_km': round(distance, 2),
            'blood_compatible': 0,
            'match_score': 0.0,
            'is_good_match': 0
        }
        data.append(sample)

    df = pd.DataFrame(data)
    print(f"\n{'=' * 100}")
    print("DATASET COMPLETE")
    print(f"{'=' * 100}")
    print(
        f"Total: {len(df):,} | Good: {good_matches:,} ({good_matches / len(df):.1%}) | Incompatible: {incompatible:,}")
    print(f"Blood compatible: {df['blood_compatible'].sum():,} | Avg distance: {total_distance / num_samples:.2f} km\n")
    return df


def main():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    data_dir = os.path.join(base_dir, 'data')
    os.makedirs(data_dir, exist_ok=True)

    df = generate_training_dataset(num_samples=50000)

    output_file = os.path.join(data_dir, 'blood_matching_training_data.csv')
    df.to_csv(output_file, index=False)
    print(f"Training data saved to: {output_file}\n")


if __name__ == '__main__':
    main()
