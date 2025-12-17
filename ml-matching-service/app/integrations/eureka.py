import requests
import socket
from datetime import datetime
from app.config import EUREKA_SERVER, SERVICE_NAME, SERVICE_PORT
from app.logger_config import logger

eureka_instance_id = None


def get_service_hostname():
    """Return the Docker service name for network resolution"""
    # In Docker Compose, always use the service name defined in docker-compose.yml
    # This is what other services can resolve on the network
    return "ml-matching-service"


def register_with_eureka():
    global eureka_instance_id

    # Use Docker service name for proper network resolution
    service_hostname = get_service_hostname()

    eureka_instance_id = f"{SERVICE_NAME}:{service_hostname}:{SERVICE_PORT}"
    eureka_url = EUREKA_SERVER.rstrip('/')
    registration_url = f"{eureka_url}/apps/{SERVICE_NAME}"

    instance_data = {
        "instance": {
            "instanceId": eureka_instance_id,
            "hostName": service_hostname,  # Use service name
            "app": SERVICE_NAME,
            "ipAddr": service_hostname,  # Use service name for Docker DNS
            "status": "UP",
            "port": {
                "$": SERVICE_PORT,
                "@enabled": "true"
            },
            "securePort": {
                "$": 443,
                "@enabled": "false"
            },
            "homePageUrl": f"http://{service_hostname}:{SERVICE_PORT}/",
            "statusPageUrl": f"http://{service_hostname}:{SERVICE_PORT}/health",
            "healthCheckUrl": f"http://{service_hostname}:{SERVICE_PORT}/health",
            "vipAddress": SERVICE_NAME,
            "secureVipAddress": SERVICE_NAME,
            "isCoordinatingDiscoveryServer": False,
            "dataCenterInfo": {
                "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                "name": "MyOwn"
            },
            "metadata": {
                "instanceId": eureka_instance_id,
                "management.port": str(SERVICE_PORT),
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
        logger.info(f"   Hostname/IP: {service_hostname}")

        response = requests.post(registration_url, json=instance_data, headers=headers, timeout=10)

        if response.status_code in [200, 201, 204]:
            logger.info(f"✅ Successfully registered with Eureka!")
            logger.info(f"   Status Code: {response.status_code}")
            verify_registration()
            return True
        else:
            logger.error(f"❌ Eureka registration failed!")
            logger.error(f"   Status Code: {response.status_code}")
            logger.error(f"   Response: {response.text}")
            return False
    except requests.exceptions.ConnectionError as e:
        logger.error(f"❌ Connection Error to Eureka: {str(e)}")
        logger.error(f"   Make sure Eureka is running at {EUREKA_SERVER}")
        return False
    except Exception as e:
        logger.error(f"❌ Eureka registration error: {str(e)}")
        return False


def verify_registration():
    """Verify the service is registered in Eureka"""
    try:
        eureka_url = EUREKA_SERVER.rstrip('/')
        verify_url = f"{eureka_url}/apps/{SERVICE_NAME}"

        response = requests.get(verify_url, headers={"Accept": "application/json"}, timeout=5)

        if response.status_code == 200:
            data = response.json()
            instances = data.get("application", {}).get("instance", [])
            if not isinstance(instances, list):
                instances = [instances]

            logger.info(f"✅ Verified {len(instances)} instance(s) registered")
            for inst in instances:
                logger.info(f"   - {inst.get('instanceId')} [{inst.get('status')}]")
        else:
            logger.warning(f"⚠️ Could not verify registration: {response.status_code}")
    except Exception as e:
        logger.warning(f"⚠️ Verification error: {str(e)}")


def send_heartbeat():
    """Send heartbeat to Eureka to keep registration alive"""
    if not eureka_instance_id:
        logger.warning("Cannot send heartbeat: not registered")
        return False

    eureka_url = EUREKA_SERVER.rstrip('/')
    heartbeat_url = f"{eureka_url}/apps/{SERVICE_NAME}/{eureka_instance_id}"

    try:
        response = requests.put(heartbeat_url, timeout=5)
        if response.status_code == 200:
            logger.debug(f"💓 Heartbeat sent successfully")
            return True
        else:
            logger.warning(f"⚠️ Heartbeat failed: {response.status_code}")
            return False
    except Exception as e:
        logger.error(f"❌ Heartbeat error: {str(e)}")
        return False


def deregister_from_eureka():
    if not eureka_instance_id:
        logger.warning("Cannot deregister: not registered")
        return

    eureka_url = EUREKA_SERVER.rstrip('/')
    deregister_url = f"{eureka_url}/apps/{SERVICE_NAME}/{eureka_instance_id}"

    try:
        logger.info(f"Deregistering from Eureka...")
        response = requests.delete(deregister_url, timeout=5)

        if response.status_code in [200, 204]:
            logger.info(f"✅ Successfully deregistered from Eureka!")
        else:
            logger.warning(f"⚠️ Deregistration status: {response.status_code}")
    except Exception as e:
        logger.warning(f"⚠️ Deregistration error: {str(e)}")
