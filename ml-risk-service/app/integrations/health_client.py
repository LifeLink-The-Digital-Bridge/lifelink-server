import httpx
from app.config import HEALTH_SERVICE_BASE_URL, INTERNAL_TOKEN


class HealthServiceClient:
    def __init__(self):
        self.base_url = HEALTH_SERVICE_BASE_URL.rstrip("/")

    async def fetch_risk_input(self, user_id: str) -> dict:
        url = f"{self.base_url}/api/health/internal/risk-data/{user_id}"
        headers = {"x-internal-token": INTERNAL_TOKEN}
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.get(url, headers=headers)
            response.raise_for_status()
            return response.json()

    async def push_risk_snapshot(self, payload: dict) -> None:
        url = f"{self.base_url}/api/health/risk/callback"
        headers = {"x-internal-token": INTERNAL_TOKEN, "Content-Type": "application/json"}
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(url, headers=headers, json=payload)
            response.raise_for_status()
