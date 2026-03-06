# TRINETRA - Production Deployment Guide

## Pre-Deployment Checklist

### 1. Environment Configuration

Before deploying to production:

```bash
# Set environment variables
export SPRING_DATASOURCE_URL=jdbc:mysql://prod-db-server:3306/trinetra
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=<secure-password>
export JWT_SECRET=<generate-strong-secret-key>
export JWT_EXPIRATION=86400000
export SPRING_PROFILES_ACTIVE=prod
```

### 2. Database Setup

#### Create Production Database

```sql
-- Connect to MySQL server
mysql -u root -p

-- Create database and user
CREATE DATABASE trinetra;
CREATE USER 'trinetra_prod'@'prod-app-server' IDENTIFIED BY '<strong-password>';
GRANT ALL PRIVILEGES ON trinetra.* TO 'trinetra_prod'@'prod-app-server';
FLUSH PRIVILEGES;

-- Create backups user
CREATE USER 'trinetra_backup'@'backup-server' IDENTIFIED BY '<backup-password>';
GRANT SELECT, LOCK TABLES ON trinetra.* TO 'trinetra_backup'@'backup-server';
FLUSH PRIVILEGES;
```

#### Initialize Production Schema

```bash
# Run schema creation (data.sql will not run in prod)
mysql -u trinetra_prod -p trinetra < schema.sql
```

### 3. Application Build

```bash
# Build production JAR
mvn clean package -DskipTests -Pprod

# Verify JAR
java -jar demo/target/trinetra-0.0.1-SNAPSHOT.jar --version
```

## Deployment Strategies

### Strategy 1: Direct JAR Deployment

```bash
# Copy JAR to production server
scp demo/target/trinetra-0.0.1-SNAPSHOT.jar user@prod-server:/opt/applications/

# Create systemd service
sudo vim /etc/systemd/system/trinetra.service
```

**systemd Service File** (`/etc/systemd/system/trinetra.service`):

```ini
[Unit]
Description=TRINETRA Anonymous Reporting System
After=network.target
Wants=network-online.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/applications
ExecStart=/usr/bin/java -Xmx512m -Xms256m \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.url=jdbc:mysql://db-server:3306/trinetra \
  -Dspring.datasource.username=trinetra_prod \
  -Dspring.datasource.password=${DB_PASSWORD} \
  -Djwt.secret=${JWT_SECRET} \
  -jar trinetra-0.0.1-SNAPSHOT.jar

Environment="DB_PASSWORD=<password>"
Environment="JWT_SECRET=<secret>"

Restart=on-failure
RestartSec=10s
StandardOutput=journal
StandardError=journal
SyslogIdentifier=trinetra

[Install]
WantedBy=multi-user.target
```

**Start Service:**

```bash
sudo systemctl daemon-reload
sudo systemctl enable trinetra
sudo systemctl start trinetra
sudo systemctl status trinetra
```

### Strategy 2: Docker Deployment

#### Dockerfile

```dockerfile
# Build stage
FROM maven:3.8.1-openjdk-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /build/demo/target/trinetra-*.jar app.jar

# Create non-root user
RUN useradd -m appuser
USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
```

#### Build and Push Docker Image

```bash
# Build image
docker build -t trinetra:latest -f Dockerfile .
docker tag trinetra:latest myregistry.azurecr.io/trinetra:latest

# Push to registry
docker push myregistry.azurecr.io/trinetra:latest
```

#### Docker Compose (for orchestration)

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: trinetra
      MYSQL_USER: trinetra_prod
      MYSQL_PASSWORD: trinetra_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./schema.sql:/docker-entrypoint-initdb.d/1-schema.sql
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  trinetra:
    image: myregistry.azurecr.io/trinetra:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/trinetra
      SPRING_DATASOURCE_USERNAME: trinetra_prod
      SPRING_DATASOURCE_PASSWORD: trinetra_password
      JWT_SECRET: ${JWT_SECRET}
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8081:8081"
    depends_on:
      mysql:
        condition: service_healthy
    restart: unless-stopped

volumes:
  mysql_data:
```

**Deploy:**

```bash
docker-compose -f docker-compose.yml up -d
```

### Strategy 3: Kubernetes Deployment

#### Kubernetes Deployment Manifest

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: trinetra-config
spec:
  application.properties: |
    spring.datasource.url=jdbc:mysql://mysql-service:3306/trinetra
    spring.datasource.username=trinetra_prod
    spring.jpa.hibernate.ddl-auto=validate
    spring.profiles.active=prod

---
apiVersion: v1
kind: Secret
metadata:
  name: trinetra-secrets
type: Opaque
data:
  DB_PASSWORD: dHJpbmV0cmFfcGFzc3dvcmQ=  # base64 encoded
  JWT_SECRET: dHJpbmV0cmFfc2VjcmV0X2tleQ==  # base64 encoded

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: trinetra
spec:
  replicas: 3
  selector:
    matchLabels:
      app: trinetra
  template:
    metadata:
      labels:
        app: trinetra
    spec:
      containers:
      - name: trinetra
        image: myregistry.azurecr.io/trinetra:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: trinetra-secrets
              key: DB_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: trinetra-secrets
              key: JWT_SECRET
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/health
            port: 8081
          initialDelaySeconds: 20
          periodSeconds: 5
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"

---
apiVersion: v1
kind: Service
metadata:
  name: trinetra-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8081
  selector:
    app: trinetra
```

**Deploy to Kubernetes:**

```bash
kubectl apply -f deployment.yaml
kubectl get pods
kubectl get services
```

---

## SSL/TLS Configuration

### Using Let'sEncrypt with Nginx

#### Nginx Configuration

```nginx
server {
    listen 443 ssl http2;
    server_name api.trinetra.com;

    ssl_certificate /etc/letsencrypt/live/api.trinetra.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.trinetra.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 80;
    server_name api.trinetra.com;
    return 301 https://$server_name$request_uri;
}
```

### Configure Application for HTTPS

Update `application.properties`:

```properties
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${KEYSTORE_PASSWORD}
server.ssl.keystore-type=PKCS12
server.ssl.key-alias=tomcat
```

---

## Monitoring & Logging

### Setup ELK Stack (Elasticsearch, Logstash, Kibana)

#### Logback Configuration (`logback-spring.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_FILE" value="/var/log/trinetra/application.log"/>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE"/>
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

### Database Monitoring

```sql
-- Create a monitoring user
CREATE USER 'monitor'@'localhost' IDENTIFIED BY '<password>';
GRANT SELECT ON *.* TO 'monitor'@'localhost';

-- Check database size
SELECT 
    table_schema AS 'Database', 
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.TABLES 
WHERE table_schema = 'trinetra'
GROUP BY table_schema;

-- Check slow queries
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

---

## Backup & Recovery

### Automated Daily Backups

```bash
#!/bin/bash
# /home/backup/backup-trinetra.sh

BACKUP_DIR="/backups/trinetra"
DATE=$(date +%Y%m%d_%H%M%S)
DB_USER="trinetra_backup"
DB_PASSWORD="<password>"
DB_NAME="trinetra"

mkdir -p $BACKUP_DIR

# Create backup
mysqldump -h db-server -u $DB_USER -p$DB_PASSWORD $DB_NAME | gzip > $BACKUP_DIR/trinetra_$DATE.sql.gz

# Keep only last 30 days
find $BACKUP_DIR -name "trinetra_*.sql.gz" -mtime +30 -exec rm {} \;

echo "Backup completed: $BACKUP_DIR/trinetra_$DATE.sql.gz"
```

### Cron Job Setup

```bash
crontab -e

# Add this line to run backup daily at 2 AM
0 2 * * * /home/backup/backup-trinetra.sh >> /var/log/backup-trinetra.log 2>&1
```

### Restore from Backup

```bash
# Decompress and restore
gunzip < /backups/trinetra/trinetra_20240306_020000.sql.gz | mysql -u trinetra_backup -p trinetra
```

---

## Security Hardening

### 1. Update Dependencies

```bash
mvn dependency:update-check
mvn -DdryRun=false -Dresolution=newest dependency:update-properties
```

### 2. Security Headers

Update `SecurityConfig.java`:

```java
http.headers().contentSecurityPolicy("default-src 'self'")
    .and()
    .frameOptions().deny()
    .and()
    .xssProtection();
```

### 3. SQL Injection Prevention

- Use parameterized queries (JPA handles this)
- Validate and sanitize all inputs
- Use prepared statements

### 4. Password Policy

Update `application.properties`:

```properties
# Password requirements
security.password.min-length=12
security.password.require-uppercase=true
security.password.require-numbers=true
security.password.require-special-chars=true
```

---

## Health Checks & Monitoring

### Health Check Endpoint

```bash
curl -H "Authorization: Bearer $TOKEN" http://api.trinetra.com/api/health
```

### Prometheus Metrics

Add to `pom.xml`:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Access metrics at: `http://api.trinetra.com/actuator/prometheus`

---

## Disaster Recovery Plan

1. **Automated Backups**: Daily at 2 AM
2. **Backup Verification**: Weekly integrity checks
3. **RTO (Recovery Time Objective)**: < 1 hour
4. **RPO (Recovery Point Objective)**: < 1 day
5. **Failover Strategy**: Hot standby with load balancing

---

## Performance Tuning

### Database Connection Pool

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
```

### Caching

```java
@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("reports", "analytics");
    }
}
```

---

## Incident Response

### If Database is Down

1. Check MySQL service: `systemctl status mysql`
2. Check connectivity: `mysql -u user -p -h host`
3. Review logs: `/var/log/mysql/error.log`
4. Restart service: `systemctl restart mysql`
5. Restore from backup if needed

### If Application is Down

1. Check logs: `journalctl -u trinetra -n 50`
2. Verify database connectivity
3. Restart application: `systemctl restart trinetra`
4. Check health endpoint
5. Alert monitoring system

---

## Compliance

- **Data Protection**: Encrypt sensitive data at rest and in transit
- **Audit Logging**: All actions logged for compliance
- **Access Control**: Role-based access control (RBAC)
- **Backups**: Regular backups for disaster recovery
- **Security Updates**: Apply patches immediately

---

For questions or issues, refer to the main README or contact the DevOps team.
