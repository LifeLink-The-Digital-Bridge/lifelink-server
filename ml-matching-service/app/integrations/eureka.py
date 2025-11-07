import requests
from datetime import datetime
from app.config import EUREKA_SERVER, SERVICE_NAME, LOCAL_IP, SERVICE_PORT
from app.logger_config import logger

eureka_instance_id = None

def register_with_eureka():
    global eureka_instance_id

    eureka_instance_id = f"{SERVICE_NAME}:{LOCAL_IP}:{SERVICE_PORT}"
    eureka_url = EUREKA_SERVER.rstrip('/')
    registration_url = f"{eureka_url}/apps/{SERVICE_NAME}"

    instance_data = {
        "instance": {
            "instanceId": eureka_instance_id,
            "hostName": LOCAL_IP,
            "app": SERVICE_NAME,
            "ipAddr": LOCAL_IP,
            "status": "UP",
            "port": {
                "$": SERVICE_PORT,
                "@enabled": "true"
            },
            "securePort": {
                "$": 443,
                "@enabled": "false"
            },
            "homePageUrl": f"http://{LOCAL_IP}:{SERVICE_PORT}/",
            "statusPageUrl": f"http://{LOCAL_IP}:{SERVICE_PORT}/health",
            "healthCheckUrl": f"http://{LOCAL_IP}:{SERVICE_PORT}/health",
            "vipAddress": SERVICE_NAME,
            "secureVipAddress": SERVICE_NAME,
            "isCoordinatingDiscoveryServer": False,
            "dataCenterInfo": {
                "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                "name": "MyOwn"
            },
            "lastUpdatedTimestamp": int(datetime.now().timestamp() * 1000),
            "lastDirtyTimestamp": int(datetime.now().timestamp() * 1000),
            "leaseInfo": {
                "renewalIntervalInSecs": 30,
                "durationInSecs": 90,
                "registrationTimestamp": int(datetime.now().timestamp() * 1000),
                "lastRenewalTimestamp": int(datetime.now().timestamp() * 1000),
                "evictionTimestamp": 0,
                "serviceUpTimestamp": int(datetime.now().timestamp() * 1000)
            }
        }
    }

    headers = {"Content-Type": "application/json"}

    try:
        logger.info(f"Registering with Eureka...")
        logger.info(f"   URL: {registration_url}")
        logger.info(f"   Instance ID: {eureka_instance_id}")

        response = requests.post(registration_url, json=instance_data, headers=headers, timeout=10)

        if response.status_code in [200, 201, 204]:
            logger.info(f"Successfully registered with Eureka!")
            logger.info(f"   Status Code: {response.status_code}")
            return True
        else:
            logger.error(f"❌ Eureka registration failed!")
            logger.error(f"   Status Code: {response.status_code}")
            logger.error(f"   Response: {response.text}")
            return False
    except requests.exceptions.ConnectionError as e:
        logger.error(f"Connection Error to Eureka: {str(e)}")
        logger.error(f"   Make sure Eureka is running at {EUREKA_SERVER}")
        return False
    except Exception as e:
        logger.error(f"Eureka registration error: {str(e)}")
        return False

def deregister_from_eureka():
    if not eureka_instance_id:
        return

    eureka_url = EUREKA_SERVER.rstrip('/')
    deregister_url = f"{eureka_url}/apps/{SERVICE_NAME}/{eureka_instance_id}"

    try:
        logger.info(f"Deregistering from Eureka...")
        response = requests.delete(deregister_url, timeout=5)

        if response.status_code in [200, 204]:
            logger.info(f"Successfully deregistered from Eureka!")
        else:
            logger.warning(f"Deregistration status: {response.status_code}")
    except Exception as e:
        logger.warning(f"Deregistration error: {str(e)}")
