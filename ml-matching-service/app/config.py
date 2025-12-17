import os
from dotenv import load_dotenv

load_dotenv()

EUREKA_SERVER = os.getenv('EUREKA_SERVER', 'http://localhost:8761/eureka')
SERVICE_NAME = 'ML-MATCHING-SERVICE'
SERVICE_PORT = int(os.getenv('SERVICE_PORT', 8001))
SERVICE_HOST = os.getenv('SERVICE_HOST', '0.0.0.0')

# Docker service name for inter-service communication
LOCAL_IP = 'ml-matching-service'
