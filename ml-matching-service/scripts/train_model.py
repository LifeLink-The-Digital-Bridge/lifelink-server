import pandas as pd
import numpy as np
import pickle
import joblib
import json
import os
import warnings
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.utils.class_weight import compute_class_weight
import xgboost as xgb
from sklearn.metrics import (
    classification_report, roc_auc_score,
    accuracy_score, precision_score, recall_score, f1_score
)

warnings.filterwarnings('ignore')


def train_model():
    print("\n" + "=" * 100)
    print("TRAINING ML MODEL - BLOOD TYPES EXCLUDED, USING BLOOD_COMPATIBLE ONLY")
    print("=" * 100 + "\n")

    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    data_dir = os.path.join(base_dir, 'data')
    ml_artifacts_dir = os.path.join(base_dir, 'ml_artifacts')

    os.makedirs(ml_artifacts_dir, exist_ok=True)

    print("Loading training data...")
    data_file = os.path.join(data_dir, 'blood_matching_training_data.csv')

    if not os.path.exists(data_file):
        print(f"Error: Training data not found at {data_file}")
        print("   Run: python scripts/generate_training_data.py")
        return

    df = pd.read_csv(data_file)
    print(f"Loaded {len(df):,} samples with {df.shape[1]} features")
    print(f"   Match distribution: {df['is_good_match'].value_counts().to_dict()}\n")

    exclude_cols = [
        'distance_km', 'match_score', 'is_good_match',
        'donor_blood_type', 'recipient_blood_type'
    ]

    feature_cols = [col for col in df.columns if col not in exclude_cols]

    X = df[feature_cols].copy()
    y = df['is_good_match'].copy()

    print(f"Total features: {len(feature_cols)}")
    print(f"Target distribution: {y.value_counts().to_dict()}\n")

    categorical_features = [
        'donor_smoking', 'donor_alcohol',
        'recipient_urgency', 'recipient_smoking', 'recipient_alcohol',
        'donor_body_size', 'recipient_body_size'
    ]

    label_encoders = {}
    print("Encoding categorical features...")
    for col in categorical_features:
        if col in X.columns:
            le = LabelEncoder()
            X[col] = le.fit_transform(X[col].astype(str))
            label_encoders[col] = le
            print(f"  {col}")

    print("\nSplitting data (80-20)...")
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    print(f"  Train: {len(X_train):,} samples")
    print(f"  Test: {len(X_test):,} samples")

    print("\nScaling features...")
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)
    print("  Features scaled")

    class_weights = compute_class_weight('balanced', classes=np.unique(y_train), y=y_train)
    class_weight_dict = {i: class_weights[i] for i in range(len(class_weights))}
    print(f"\nClass weights:")
    print(f"  No Match (0): {class_weight_dict[0]:.4f}")
    print(f"  Good Match (1): {class_weight_dict[1]:.4f}")

    print("\nTraining XGBoost...")
    xgb_model = xgb.XGBClassifier(
        n_estimators=200,
        max_depth=8,
        learning_rate=0.05,
        subsample=0.8,
        colsample_bytree=0.8,
        scale_pos_weight=class_weight_dict[1] / class_weight_dict[0],
        objective='binary:logistic',
        random_state=42,
        n_jobs=-1,
        eval_metric='auc',
        verbosity=0
    )

    xgb_model.fit(X_train_scaled, y_train)
    print("Model training complete")

    print("\nEvaluating model...")
    y_pred = xgb_model.predict(X_test_scaled)
    y_pred_proba = xgb_model.predict_proba(X_test_scaled)[:, 1]

    accuracy = accuracy_score(y_test, y_pred)
    precision = precision_score(y_test, y_pred)
    recall = recall_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)
    auc = roc_auc_score(y_test, y_pred_proba)

    print(f"  Accuracy:  {accuracy:.4f}")
    print(f"  Precision: {precision:.4f}")
    print(f"  Recall:    {recall:.4f}")
    print(f"  F1-Score:  {f1:.4f}")
    print(f"  AUC-ROC:   {auc:.4f}")

    print(f"\nClassification Report:")
    print(classification_report(y_test, y_pred, target_names=['No Match', 'Good Match']))

    print("Performing 5-Fold Cross-Validation...")
    cv_scores = cross_val_score(xgb_model, X_train_scaled, y_train, cv=5, scoring='roc_auc')
    print(f"  CV AUC: {cv_scores.mean():.4f} (+/- {cv_scores.std():.4f})\n")

    print("Saving model artifacts...")
    joblib.dump(xgb_model, os.path.join(ml_artifacts_dir, 'blood_matching_model.pkl'))
    joblib.dump(scaler, os.path.join(ml_artifacts_dir, 'scaler.pkl'))
    joblib.dump(label_encoders, os.path.join(ml_artifacts_dir, 'label_encoders.pkl'))

    with open(os.path.join(ml_artifacts_dir, 'feature_columns.pkl'), 'wb') as f:
        pickle.dump(feature_cols, f)

    print("  Model saved")
    print("  Scaler saved")
    print("  Label encoders saved")
    print("  Feature columns saved")

    metadata = {
        'model_type': 'XGBoost (Blood Types Excluded)',
        'accuracy': float(accuracy),
        'precision': float(precision),
        'recall': float(recall),
        'f1_score': float(f1),
        'auc_score': float(auc),
        'cv_auc_mean': float(cv_scores.mean()),
        'cv_auc_std': float(cv_scores.std()),
        'n_features': len(feature_cols),
        'feature_names': feature_cols,
        'categorical_features': categorical_features,
        'training_samples': len(X_train),
        'test_samples': len(X_test),
        'class_weights': {str(k): float(v) for k, v in class_weight_dict.items()}
    }

    with open(os.path.join(ml_artifacts_dir, 'model_metadata.json'), 'w') as f:
        json.dump(metadata, f, indent=4)

    print("  Metadata saved")

    print("\n" + "=" * 100)
    print("MODEL TRAINING COMPLETE")
    print("=" * 100 + "\n")

    print("Summary:")
    print(json.dumps({
        'Model Type': metadata['model_type'],
        'Accuracy': f"{accuracy:.4f}",
        'Precision': f"{precision:.4f}",
        'Recall': f"{recall:.4f}",
        'F1-Score': f"{f1:.4f}",
        'AUC-ROC': f"{auc:.4f}",
        'CV AUC': f"{cv_scores.mean():.4f} ± {cv_scores.std():.4f}",
        'Features': len(feature_cols),
        'Training Samples': f"{len(X_train):,}",
        'Test Samples': f"{len(X_test):,}"
    }, indent=2))

    print(f"\nAll artifacts saved to: {ml_artifacts_dir}\n")


if __name__ == '__main__':
    train_model()
