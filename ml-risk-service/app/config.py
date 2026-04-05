import os
from dotenv import load_dotenv

load_dotenv()

SERVICE_NAME = os.getenv("SERVICE_NAME", "ML-RISK-SERVICE")
SERVICE_PORT = int(os.getenv("SERVICE_PORT", "8002"))
SERVICE_HOST = os.getenv("SERVICE_HOST", "0.0.0.0")
HEALTH_SERVICE_BASE_URL = os.getenv("HEALTH_SERVICE_BASE_URL", "http://health-service:8086")
INTERNAL_TOKEN = os.getenv("INTERNAL_TOKEN", "")
GROQ_API_KEY = os.getenv("GROQ_API_KEY", "")
GROQ_MODEL = os.getenv("GROQ_MODEL", "qwen/qwen3-32b")
