import os
from dotenv import load_dotenv
import socket

load_dotenv()

EUREKA_SERVER = os.getenv('EUREKA_SERVER', 'http://localhost:8761/eureka')
SERVICE_NAME = 'ML-MATCHING-SERVICE'
SERVICE_PORT = int(os.getenv('SERVICE_PORT', 8001))
SERVICE_HOST = os.getenv('SERVICE_HOST', '0.0.0.0')

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(('8.8.8.8', 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        return '127.0.0.1'

LOCAL_IP = get_local_ip()
