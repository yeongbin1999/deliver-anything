#!/bin/bash
set -euxo pipefail

# Swap (4GB)
if ! grep -q "/swapfile" /etc/fstab; then
  dd if=/dev/zero of=/swapfile bs=128M count=32
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo "/swapfile swap swap defaults 0 0" >> /etc/fstab
fi

# Docker
dnf update -y
dnf install -y docker git
systemctl enable --now docker
usermod -aG docker ec2-user || true

# Docker 네트워크/볼륨
docker network create common || true
mkdir -p /dockerProjects/npm/volumes/data
mkdir -p /dockerProjects/npm/volumes/etc/letsencrypt
mkdir -p /dockerProjects/redis/volumes/data
mkdir -p /dockerProjects/mysql/volumes/var/lib/mysql
mkdir -p /dockerProjects/mysql/volumes/etc/mysql/conf.d

# --- Nginx Proxy Manager ---
docker rm -f npm || true
docker run -d \
  --name npm \
  --restart unless-stopped \
  --network common \
  -p 80:80 \
  -p 443:443 \
%{ if open_npm_admin ~}
  -p 81:81 \
%{ else ~}
  -p 127.0.0.1:81:81 \
%{ endif ~}
  -e TZ=${timezone} \
  -e INITIAL_ADMIN_EMAIL=${npm_admin_email} \
  -e INITIAL_ADMIN_PASSWORD=${default_password} \
  -v /dockerProjects/npm/volumes/data:/data \
  -v /dockerProjects/npm/volumes/etc/letsencrypt:/etc/letsencrypt \
  ${npm_image}

# --- Elasticsearch (내부 전용) ---
docker rm -f elasticsearch || true
mkdir -p /dockerProjects/elasticsearch/volumes/data
docker run -d \
  --name elasticsearch \
  --restart unless-stopped \
  --network common \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "xpack.security.enabled=false" \
  -v /dockerProjects/elasticsearch/volumes/data:/usr/share/elasticsearch/data \
  ${elasticsearch_image}

# --- Kafka (Zookeeper 없이 KRaft 모드) ---
docker rm -f kafka || true
mkdir -p /dockerProjects/kafka/volumes/kafka
docker run -d \
  --name kafka \
  --restart unless-stopped \
  --network common \
  -p 9092:9092 \
  -e KAFKA_KRAFT_MODE=true \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@kafka:9093 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_LOG_DIRS=/kafka/logs \
  -v /dockerProjects/kafka/volumes/kafka:/kafka \
  bitnami/kafka:latest

# --- Redis (내부 전용) ---
docker rm -f redis || true
docker run -d \
  --name redis \
  --restart unless-stopped \
  --network common \
  -e TZ=${timezone} \
  -v /dockerProjects/redis/volumes/data:/data \
  ${redis_image} \
  redis-server --appendonly yes --requirepass ${default_password}

# --- MySQL (기본 내부 전용, 필요 시 공개) ---
docker rm -f mysql || true
docker run -d \
  --name mysql \
  --restart unless-stopped \
  --network common \
%{ if expose_mysql ~}
  -p 3306:3306 \
%{ endif ~}
  -e MYSQL_ROOT_PASSWORD=${default_password} \
  -v /dockerProjects/mysql/volumes/var/lib/mysql:/var/lib/mysql \
  -v /dockerProjects/mysql/volumes/etc/mysql/conf.d:/etc/mysql/conf.d \
  ${mysql_image}

# MySQL 준비 대기
until docker exec mysql mysql -uroot -p${default_password} -e "SELECT 1" &> /dev/null; do
  sleep 5
done

# 앱 DB 생성
docker exec mysql mysql -uroot -p${default_password} -e "CREATE DATABASE IF NOT EXISTS \`${app_db_name}\`;"

# 사용자/권한
%{ for u in mysql_user_list ~}
docker exec mysql mysql -uroot -p${default_password} -e "CREATE USER IF NOT EXISTS '${u.name}'@'${u.host}' IDENTIFIED WITH caching_sha2_password BY '${u.password}';"
docker exec mysql mysql -uroot -p${default_password} -e "GRANT ${u.privileges} TO '${u.name}'@'${u.host}';"
%{ endfor ~}
docker exec mysql mysql -uroot -p${default_password} -e "FLUSH PRIVILEGES;"

# GHCR 로그인 (값 있을 때만)
if [ -n "${github_token}" ] && [ -n "${github_user}" ]; then
  echo "${github_token}" | docker login ghcr.io -u ${github_user} --password-stdin || true
fi