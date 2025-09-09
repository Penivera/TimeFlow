# TimeFlow Deployment Guide

## Overview
This guide provides comprehensive instructions for deploying TimeFlow in various environments, from development to production.

## Deployment Options

### 1. Local Development Deployment
### 2. Docker Deployment
### 3. Cloud Deployment (AWS, Azure, GCP)
### 4. On-Premises Deployment
### 5. Kubernetes Deployment

## Prerequisites

### System Requirements
- **Operating System**: Linux (Ubuntu 20.04+), Windows 10+, macOS 10.15+
- **Java**: OpenJDK 16 or higher
- **Database**: PostgreSQL 12+
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Storage**: 10GB available disk space
- **Network**: Internet access for dependencies

### Required Software
- **Build Tools**: Maven 3.6+
- **Database**: PostgreSQL 12+
- **Reverse Proxy**: Nginx (optional)
- **SSL**: Let's Encrypt certificates (production)
- **Monitoring**: Prometheus + Grafana (optional)

## Environment-Specific Deployment

### 1. Local Development Deployment

#### Step 1: Environment Setup
```bash
# Install Java 16
sudo apt update
sudo apt install openjdk-16-jdk

# Install Maven
sudo apt install maven

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib
```

#### Step 2: Database Setup
```bash
# Start PostgreSQL
sudo systemctl start postgresql

# Create database and user
sudo -u postgres psql
CREATE DATABASE timeflow_db;
CREATE USER timeflow_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE timeflow_db TO timeflow_user;
\q
```

#### Step 3: Application Configuration
```bash
# Clone repository
git clone https://github.com/your-org/timeflow.git
cd timeflow

# Create environment file
cp .env.example .env
```

#### Step 4: Environment Variables
```bash
# Edit .env file
nano .env

# Add the following:
DB_URL=jdbc:postgresql://localhost:5432/timeflow_db
DB_USERNAME=timeflow_user
DB_PASSWORD=secure_password
EMAIL=your-email@domain.com
EMAIL_PASSWORD=your-app-password
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
```

#### Step 5: Build and Run
```bash
# Build application
mvn clean compile

# Run database migrations
mvn flyway:migrate

# Start application
mvn exec:java -Dexec.mainClass="org.timeflow.Main"
```

### 2. Docker Deployment

#### Docker Compose Configuration
```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: timeflow_db
      POSTGRES_USER: timeflow_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U timeflow_user"]
      interval: 30s
      timeout: 10s
      retries: 5

  timeflow:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      - DB_URL=jdbc:postgresql://postgres:5432/timeflow_db
      - DB_USERNAME=timeflow_user
      - DB_PASSWORD=${DB_PASSWORD}
      - EMAIL=${EMAIL}
      - EMAIL_PASSWORD=${EMAIL_PASSWORD}
    volumes:
      - ./logs:/app/logs
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - timeflow
    restart: unless-stopped

volumes:
  postgres_data:
```

#### Dockerfile
```dockerfile
FROM openjdk:16-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:16-jre-slim

WORKDIR /app

# Copy built JAR
COPY --from=0 /app/target/timeflow-*.jar app.jar

# Create non-root user
RUN useradd -r -s /bin/false timeflow

# Set permissions
RUN chown -R timeflow:timeflow /app
USER timeflow

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3. AWS Deployment

#### Step 1: RDS Setup
```bash
# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier timeflow-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username timeflow_user \
  --master-user-password secure_password \
  --allocated-storage 20 \
  --backup-retention-period 7
```

#### Step 2: EC2 Setup
```bash
# Launch EC2 instance
aws ec2 run-instances \
  --image-id ami-0abcdef1234567890 \
  --instance-type t3.small \
  --key-name your-key-pair \
  --security-groups timeflow-sg \
  --user-data file://user-data.sh
```

#### Step 3: User Data Script
```bash
#!/bin/bash
# user-data.sh

# Update system
yum update -y

# Install Java
yum install java-16-amazon-corretto -y

# Install PostgreSQL client
yum install postgresql -y

# Create application directory
mkdir -p /opt/timeflow
cd /opt/timeflow

# Download application
aws s3 cp s3://your-bucket/timeflow.jar .

# Create systemd service
cat > /etc/systemd/system/timeflow.service << EOF
[Unit]
Description=TimeFlow Application
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/timeflow
ExecStart=/usr/bin/java -jar timeflow.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Start service
systemctl daemon-reload
systemctl enable timeflow
systemctl start timeflow
```

### 4. Azure Deployment

#### Azure Container Instances
```yaml
# azure-container-instance.yaml
apiVersion: 2019-12-01
location: eastus
name: timeflow-container
properties:
  containers:
  - name: timeflow
    properties:
      image: your-registry.azurecr.io/timeflow:latest
      resources:
        requests:
          cpu: 1
          memoryInGB: 2
      ports:
      - port: 8080
        protocol: TCP
      environmentVariables:
      - name: DB_URL
        value: jdbc:postgresql://timeflow-db.postgres.database.azure.com:5432/timeflow_db
      - name: DB_USERNAME
        value: timeflow_user@timeflow-db
      - name: DB_PASSWORD
        secureValue: secure_password
  osType: Linux
  restartPolicy: Always
```

#### Azure App Service
```bash
# Create App Service
az webapp create \
  --resource-group timeflow-rg \
  --plan timeflow-plan \
  --name timeflow-app \
  --runtime "JAVA|16-java"

# Configure app settings
az webapp config appsettings set \
  --resource-group timeflow-rg \
  --name timeflow-app \
  --settings \
    DB_URL="jdbc:postgresql://timeflow-db.postgres.database.azure.com:5432/timeflow_db" \
    DB_USERNAME="timeflow_user@timeflow-db" \
    DB_PASSWORD="secure_password"
```

### 5. Kubernetes Deployment

#### Namespace and RBAC
```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: timeflow
  labels:
    name: timeflow

---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: timeflow-sa
  namespace: timeflow

---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: timeflow-role
  namespace: timeflow
rules:
- apiGroups: [""]
  resources: ["pods", "services", "configmaps"]
  verbs: ["get", "list", "watch"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: timeflow-rolebinding
  namespace: timeflow
subjects:
- kind: ServiceAccount
  name: timeflow-sa
  namespace: timeflow
roleRef:
  kind: Role
  name: timeflow-role
  apiGroup: rbac.authorization.k8s.io
```

#### Deployment Configuration
```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: timeflow-deployment
  namespace: timeflow
  labels:
    app: timeflow
spec:
  replicas: 3
  selector:
    matchLabels:
      app: timeflow
  template:
    metadata:
      labels:
        app: timeflow
    spec:
      serviceAccountName: timeflow-sa
      containers:
      - name: timeflow
        image: your-registry/timeflow:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: timeflow-secrets
              key: db-url
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: timeflow-secrets
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: timeflow-secrets
              key: db-password
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /ready
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

#### Service Configuration
```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: timeflow-service
  namespace: timeflow
spec:
  selector:
    app: timeflow
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer

---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: timeflow-ingress
  namespace: timeflow
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - timeflow.yourdomain.com
    secretName: timeflow-tls
  rules:
  - host: timeflow.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: timeflow-service
            port:
              number: 80
```

## Environment Configuration

### Environment Variables Reference

#### Database Configuration
```bash
# Required
DB_URL=jdbc:postgresql://localhost:5432/timeflow_db
DB_USERNAME=timeflow_user
DB_PASSWORD=secure_password

# Optional
DB_POOL_SIZE=10
DB_CONNECTION_TIMEOUT=30000
DB_IDLE_TIMEOUT=600000
DB_MAX_LIFETIME=1800000
```

#### Application Configuration
```bash
# Server
SERVER_PORT=8080
SERVER_CONTEXT_PATH=/timeflow

# Security
JWT_SECRET=your-jwt-secret-key
JWT_EXPIRATION=86400

# Email
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password
EMAIL_FROM=noreply@yourdomain.com

# Logging
LOG_LEVEL=INFO
LOG_FILE_PATH=/var/log/timeflow/
```

#### Cloud-Specific Configuration
```bash
# AWS
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
S3_BUCKET=timeflow-assets

# Azure
AZURE_TENANT_ID=your-tenant-id
AZURE_CLIENT_ID=your-client-id
AZURE_CLIENT_SECRET=your-client-secret
AZURE_STORAGE_ACCOUNT=yourstorageaccount

# GCP
GOOGLE_CLOUD_PROJECT=your-project-id
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
```

## Monitoring and Logging

### Application Monitoring
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'timeflow'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: /actuator/prometheus
```

### Log Aggregation
```yaml
# fluentd-config.yaml
<source>
  @type tail
  path /var/log/timeflow/*.log
  pos_file /var/log/fluentd/timeflow.log.pos
  tag timeflow.application
  format json
</source>

<match timeflow.**>
  @type elasticsearch
  host elasticsearch
  port 9200
  logstash_format true
  logstash_prefix timeflow
</match>
```

### Health Checks
```bash
# Application health endpoint
curl -f http://localhost:8080/actuator/health

# Database connectivity
curl -f http://localhost:8080/actuator/health/db

# External service health
curl -f http://localhost:8080/actuator/health/external-services
```

## Security Best Practices

### SSL/TLS Configuration
```nginx
# nginx.conf
server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/ssl/certs/yourdomain.com.crt;
    ssl_certificate_key /etc/ssl/private/yourdomain.com.key;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Database Security
```sql
-- Create restricted user
CREATE USER timeflow_app WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE timeflow_db TO timeflow_app;
GRANT USAGE ON SCHEMA public TO timeflow_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO timeflow_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO timeflow_app;

-- Enable SSL
ALTER SYSTEM SET ssl = on;
ALTER SYSTEM SET ssl_cert_file = 'server.crt';
ALTER SYSTEM SET ssl_key_file = 'server.key';
```

## Backup and Disaster Recovery

### Automated Backup Strategy
```bash
#!/bin/bash
# backup-strategy.sh

# Daily backup
pg_dump -h localhost -U timeflow_user timeflow_db > backup_$(date +%Y%m%d).sql

# Weekly full backup with compression
pg_dump -h localhost -U timeflow_user timeflow_db | gzip > backup_$(date +%Y%m%d).sql.gz

# Monthly archive backup
pg_dumpall -h localhost -U postgres | gzip > full_backup_$(date +%Y%m%d).sql.gz

# Upload to cloud storage
aws s3 cp backup_$(date +%Y%m%d).sql.gz s3://your-backup-bucket/
```

### Disaster Recovery Plan
```bash
# Recovery procedure
1. Restore from latest backup
pg_restore -h localhost -U timeflow_user -d timeflow_db backup_latest.sql

2. Verify data integrity
psql -h localhost -U timeflow_user -d timeflow_db -c "SELECT COUNT(*) FROM users;"

3. Update DNS if needed
4. Notify stakeholders
5. Document incident
```

## Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Check port usage
netstat -tulpn | grep :8080

# Kill process
kill -9 $(lsof -t -i:8080)
```

#### Database Connection Issues
```bash
# Test connection
psql -h localhost -U timeflow_user -d timeflow_db

# Check logs
tail -f /var/log/postgresql/postgresql.log
```

#### Memory Issues
```bash
# Increase JVM heap size
java -Xmx2g -Xms1g -jar timeflow.jar

# Monitor memory usage
jstat -gc $(pgrep java)
```

### Performance Tuning
```bash
# JVM tuning
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar timeflow.jar

# Database tuning
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET work_mem = '4MB';
```

## Support and Maintenance

### Regular Maintenance Tasks
- [ ] Update dependencies monthly
- [ ] Review security patches weekly
- [ ] Monitor disk space daily
- [ ] Check log files for errors
- [ ] Verify backup integrity
- [ ] Update SSL certificates
- [ ] Review performance metrics

### Emergency Contacts
- **Technical Support**: support@timeflow.com
- **Emergency Hotline**: +1-800-TIMEFLOW
- **Documentation**: docs.timeflow.com
- **Community Forum**: forum.timeflow.com
