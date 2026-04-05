#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.run"
PID_DIR="$RUN_DIR/pids"
LOG_DIR="$RUN_DIR/logs"
mkdir -p "$PID_DIR" "$LOG_DIR"

SERVICES=(
  "service-registry|service-registry|8761"
  "user-service|user-service|9081"
  "auth-service|auth-service|8000"
  "donor-service|donor-service|8083"
  "recipient-service|recipient-service|8084"
  "matching-service|matching-service|8085"
  "health-service|health-service|8087"
  "notification-service|notification-service|8086"
  "gateway-service|gateway-service|8080"
)

wait_for_port() {
  local host="$1"
  local port="$2"
  local timeout="${3:-120}"
  local start_ts now
  start_ts=$(date +%s)
  while true; do
    if (echo >"/dev/tcp/${host}/${port}") >/dev/null 2>&1; then
      return 0
    fi
    now=$(date +%s)
    if (( now - start_ts > timeout )); then
      return 1
    fi
    sleep 1
  done
}

service_pid_file() {
  local name="$1"
  echo "$PID_DIR/${name}.pid"
}

is_running() {
  local name="$1"
  local pid_file
  pid_file="$(service_pid_file "$name")"
  [[ -f "$pid_file" ]] || return 1
  local pid
  pid="$(cat "$pid_file")"
  kill -0 "$pid" >/dev/null 2>&1
}

start_infra() {
  echo "[infra] Starting Kafka/Zookeeper/Redis from docker-compose.local.yml ..."
  docker compose -f "$ROOT_DIR/docker-compose.local.yml" up -d zookeeper kafka kafka-init redis

  echo "[infra] Waiting for Kafka (localhost:9092) ..."
  if ! wait_for_port localhost 9092 120; then
    echo "[infra] Kafka did not become ready in time. Check: docker compose -f docker-compose.local.yml logs kafka"
    exit 1
  fi

  echo "[infra] Waiting for Redis (localhost:6379) ..."
  if ! wait_for_port localhost 6379 60; then
    echo "[infra] Redis did not become ready in time. Check: docker compose -f docker-compose.local.yml logs redis"
    exit 1
  fi

  echo "[infra] Local infra is ready."
}

start_service() {
  local name="$1"
  local dir="$2"
  local port="$3"
  local pid_file log_file
  pid_file="$(service_pid_file "$name")"
  log_file="$LOG_DIR/${name}.log"

  if is_running "$name"; then
    echo "[$name] already running (pid $(cat "$pid_file"))"
    return
  fi

  echo "[$name] starting ..."
  (
    cd "$ROOT_DIR"
    nohup bash -lc "mvn -f ${dir}/pom.xml spring-boot:run" >"$log_file" 2>&1 &
    echo $! >"$pid_file"
  )

  if wait_for_port localhost "$port" 180; then
    echo "[$name] up on port $port"
  else
    echo "[$name] did not open port $port in time. Check log: $log_file"
    exit 1
  fi
}

start_all() {
  start_infra
  for entry in "${SERVICES[@]}"; do
    IFS='|' read -r name dir port <<<"$entry"
    start_service "$name" "$dir" "$port"
  done
  echo "[done] All local services are running."
  echo "[logs] Tail logs with: $0 logs <service-name>"
}

stop_service() {
  local name="$1"
  local pid_file
  pid_file="$(service_pid_file "$name")"

  if [[ ! -f "$pid_file" ]]; then
    echo "[$name] not running (no pid file)"
    return
  fi

  local pid
  pid="$(cat "$pid_file")"
  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "[$name] stopping pid $pid"
    kill "$pid" || true
    sleep 1
    if kill -0 "$pid" >/dev/null 2>&1; then
      kill -9 "$pid" || true
    fi
  else
    echo "[$name] stale pid file found"
  fi
  rm -f "$pid_file"
}

stop_all() {
  for entry in "${SERVICES[@]}"; do
    IFS='|' read -r name _ __ <<<"$entry"
    stop_service "$name"
  done

  echo "[infra] Stopping Kafka/Zookeeper/Redis ..."
  docker compose -f "$ROOT_DIR/docker-compose.local.yml" down
  echo "[done] Local stack stopped."
}

status_all() {
  echo "Spring services:"
  for entry in "${SERVICES[@]}"; do
    IFS='|' read -r name _ port <<<"$entry"
    if is_running "$name"; then
      echo "  - $name: RUNNING (pid $(cat "$(service_pid_file "$name")"), port $port)"
    else
      echo "  - $name: STOPPED"
    fi
  done

  echo
  echo "Docker infra containers:"
  docker compose -f "$ROOT_DIR/docker-compose.local.yml" ps
}

show_logs() {
  local name="${1:-}"
  if [[ -z "$name" ]]; then
    echo "Usage: $0 logs <service-name>"
    exit 1
  fi
  local log_file="$LOG_DIR/${name}.log"
  if [[ ! -f "$log_file" ]]; then
    echo "No log found for $name at $log_file"
    exit 1
  fi
  tail -f "$log_file"
}

case "${1:-}" in
  start)
    start_all
    ;;
  stop)
    stop_all
    ;;
  restart)
    stop_all
    start_all
    ;;
  status)
    status_all
    ;;
  logs)
    show_logs "${2:-}"
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status|logs <service-name>}"
    exit 1
    ;;
esac
