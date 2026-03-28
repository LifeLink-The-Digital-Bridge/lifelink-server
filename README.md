<p align="center">
  <img 
    src="https://github.com/user-attachments/assets/2cffc638-6bfa-465d-b730-5b9d8229edff"
    alt="LifeLink Splash"
    width="220"
  />
</p>

<h1 align="center">LifeLink — Backend Server</h1>

<p align="center">
  <strong>A microservices-based backend powering the LifeLink donation platform — connecting blood, organ, tissue & stem cell donors with recipients across India.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-green?style=flat-square&logo=springboot" />
  <img src="https://img.shields.io/badge/Python-3.11-blue?style=flat-square&logo=python" />
  <img src="https://img.shields.io/badge/FastAPI-0.100+-teal?style=flat-square&logo=fastapi" />
  <img src="https://img.shields.io/badge/Docker-Compose-blue?style=flat-square&logo=docker" />
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" />
</p>

---

## 📖 Overview

**LifeLink Server** is the backend infrastructure of the LifeLink platform — a digital bridge that connects donors and recipients for **blood**, **organ**, **tissue**, and **stem cell** donations. The system is built on a Spring Cloud microservices architecture, with an intelligent ML-powered matching engine to pair donors with recipients based on medical compatibility and urgency.

---

## 🏗️ Architecture

The backend follows a **microservices architecture** with Netflix Eureka for service discovery and a Spring Cloud Gateway for routing.

```
                        ┌──────────────┐
                        │  API Gateway │  :8080
                        │  (JWT Auth)  │
                        └──────┬───────┘
                               │
           ┌───────────────────┼───────────────────┐
           │                   │                   │
    ┌──────▼──────┐   ┌──��─────▼───────┐  ┌───────▼──────┐
    │ Auth Service│   │  User Service  │  │ Donor Service│
    │   :8081     │   │    :8082       │  │    :8083     │
    └─────────────┘   └────────────────┘  └──────────────┘
           │                   │                   │
    ┌──────▼──────┐   ┌────────▼───────┐  ┌───────▼──────────┐
    │  Recipient  │   │Matching Service│  │ ML Matching Svc  │
    │  Service    │   │    :8085       │  │  (Python) :8001  │
    │   :8084     │   └────────────────┘  └──────────────────┘
    └─────────────┘            │
                        ┌──────▼──────────┐
                        │Notification Svc │  :8086
                        └─────────────────┘

                   Service Registry (Eureka): :8761
```

---

## 🧩 Microservices

| Service | Port | Tech | Responsibility |
|---|---|---|---|
| **service-registry** | 8761 | Spring Eureka | Service discovery & registration |
| **gateway-service** | 8080 | Spring Cloud Gateway | API routing, JWT validation, auth filter |
| **auth-service** | 8081 | Spring Boot | Login, registration, token refresh, password recovery |
| **user-service** | 8082 | Spring Boot | User profiles, follow/unfollow |
| **donor-service** | 8083 | Spring Boot | Donor registration, donation management |
| **recipient-service** | 8084 | Spring Boot | Recipient registration, receive requests |
| **matching-service** | 8085 | Spring Boot | Match orchestration, donor-recipient pairing, confirmations |
| **ml-matching-service** | 8001 | FastAPI + XGBoost | ML-based compatibility scoring & batch matching |
| **notification-service** | 8086 | Spring Boot | In-app notifications via Kafka events |

---

## 🩸 Supported Donation Types

| Type | Subtypes |
|---|---|
| **Blood** | All ABO/Rh blood groups (A+, A−, B+, B−, O+, O−, AB+, AB−) |
| **Organ** | Heart, Liver, Kidney, Lung, Pancreas, Intestine |
| **Tissue** | Multiple tissue types |
| **Stem Cell** | Peripheral Blood, Bone Marrow, Cord Blood |

---

## 🤖 ML Matching Engine

The `ml-matching-service` is a Python FastAPI service powered by **XGBoost** that performs intelligent donor-recipient matching:

- Processes **batch match requests** with configurable `topN` and `threshold` parameters
- Considers: blood type compatibility, age, weight, BMI, medical history, location distance, HLA profile, urgency level, and more
- Trained on **50,000+ synthetic samples** generated via `scripts/generate_training_data.py`
- Registers with Eureka for service discovery within the Docker network
- Endpoint: `POST /api/ml/batch-match`

---

## 🔐 Authentication & Security

- JWT-based authentication via the **Auth Service**
- The **API Gateway** intercepts all requests and validates tokens against `AUTH-SERVICE`
- Public endpoints: `/auth/login`, `/auth/refresh`, `/auth/password-recovery`, `/users/register`
- All protected endpoints require a `Bearer <token>` header
- Role-based access control (`DONOR`, `RECIPIENT`, `ADMIN`) enforced at the service level via `@RequireRole` AOP annotations

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| **Java 21** | Primary backend language |
| **Spring Boot 3.x** | Microservice framework |
| **Spring Cloud Gateway** | API gateway & routing |
| **Netflix Eureka** | Service discovery |
| **OpenFeign** | Inter-service HTTP clients |
| **Python 3.11** | ML service language |
| **FastAPI** | ML service REST framework |
| **XGBoost + scikit-learn** | Donor-recipient ML matching model |
| **Apache Kafka** | Event-driven notification pipeline |
| **Docker + Docker Compose** | Containerisation & orchestration |
| **Maven** | Java build tool |

---

## 🚀 Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Python 3.11+ (for ML service local development)

### Run with Docker Compose

```bash
# Clone the repository
git clone https://github.com/LifeLink-The-Digital-Bridge/lifelink-server.git
cd lifelink-server

# Start all services
docker compose up --build
```

Services will be available at:
- **API Gateway**: `http://localhost:8080`
- **Eureka Dashboard**: `http://localhost:8761`
- **ML Service**: `http://localhost:8001`

### Run ML Service Locally

```bash
cd ml-matching-service
pip install -r requirements.txt

# (Optional) Generate training data and train the model
python scripts/generate_training_data.py
python scripts/train_model.py

# Start the service
uvicorn app.main:app --host 0.0.0.0 --port 8001
```

---

## 📁 Project Structure

```
lifelink-server/
├── service-registry/        # Eureka server
├── gateway-service/         # API Gateway + JWT auth filter
├── auth-service/            # Authentication & token management
├── user-service/            # User profiles
├── donor-service/           # Donor & donation management
├── recipient-service/       # Recipient & receive-request management
├── matching-service/        # Match orchestration & confirmation flow
├── ml-matching-service/     # Python FastAPI + XGBoost ML engine
│   ├── app/
│   │   ├── main.py
│   │   ├── services/
│   │   ├── models/
│   │   └── integrations/
│   └── scripts/
│       ├── generate_training_data.py
│       └── train_model.py
└── notification-service/    # Kafka-driven in-app notifications
```

---

## 🔗 Frontend

This server powers the **LifeLink Mobile App** (React Native + Expo):  
👉 [https://github.com/LifeLink-The-Digital-Bridge/lifelink-frontend](https://github.com/LifeLink-The-Digital-Bridge/lifelink-frontend)

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).  
© 2025 AdepuSriCharan
