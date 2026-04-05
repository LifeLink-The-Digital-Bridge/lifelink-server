from datetime import datetime, timezone


def _parse_bp(bp: str | None) -> tuple[int | None, int | None]:
    if not bp or "/" not in bp:
        return None, None
    try:
        s, d = bp.split("/", 1)
        return int(s.strip()), int(d.strip())
    except Exception:
        return None, None


def compute_risk_score(risk_input: dict) -> dict:
    health_id = risk_input.get("healthId") or {}
    records = risk_input.get("recentRecords") or []

    score = 15.0
    factors: list[str] = []

    hemoglobin = health_id.get("hemoglobinLevel")
    if isinstance(hemoglobin, (int, float)):
        if hemoglobin < 11.0:
            score += 20
            factors.append("Hemoglobin is critically low")
        elif hemoglobin < 12.5:
            score += 10
            factors.append("Hemoglobin is below healthy range")

    if health_id.get("hasDiabetes") is True:
        score += 15
        factors.append("Diabetes flag present")

    if health_id.get("hasChronicDiseases") is True:
        score += 15
        factors.append("Chronic disease flag present")

    systolic, diastolic = _parse_bp(health_id.get("bloodPressure"))
    if systolic is not None and diastolic is not None:
        if systolic >= 140 or diastolic >= 90:
            score += 15
            factors.append("Blood pressure indicates hypertension")
        elif systolic >= 130 or diastolic >= 85:
            score += 8
            factors.append("Blood pressure is elevated")

    emergency_count = sum(1 for r in records if bool(r.get("isEmergency")))
    if emergency_count > 0:
        emergency_weight = min(20, emergency_count * 5)
        score += emergency_weight
        factors.append(f"Recent emergency records: {emergency_count}")

    medical_history = str(health_id.get("medicalHistory") or "").strip()
    if len(medical_history) >= 40:
        score += 5
        factors.append("Medical history indicates ongoing complexity")

    chronic_text = str(health_id.get("chronicConditions") or "").strip()
    if chronic_text:
        score += 5
        factors.append("Chronic conditions reported")

    score = max(0.0, min(100.0, round(score, 2)))

    if score >= 65:
        risk_level = "HIGH"
        actions = [
            "Schedule doctor follow-up within 48 hours",
            "Review medication adherence and vitals",
            "Prioritize emergency preparedness"
        ]
    elif score >= 35:
        risk_level = "MEDIUM"
        actions = [
            "Schedule checkup within 7 days",
            "Track blood pressure and symptoms daily",
            "Repeat key tests as advised"
        ]
    else:
        risk_level = "LOW"
        actions = [
            "Continue regular monitoring",
            "Maintain preventive care schedule",
            "Update health records after each consultation"
        ]

    if not factors:
        factors = ["No major risk amplifiers detected in current profile"]

    return {
        "riskScore": score,
        "riskLevel": risk_level,
        "topFactors": factors[:5],
        "recommendedActions": actions,
        "modelVersion": "risk-v1.0.0",
        "computedAt": datetime.now(timezone.utc).isoformat()
    }
