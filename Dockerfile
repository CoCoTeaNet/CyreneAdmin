# Multi-stage build for CyreneAdmin backend and frontend
# Stage 1: Build backend JAR
FROM maven:3.9.6-eclipse-temurin-17-alpine AS backend-builder

WORKDIR /app

# Copy pom.xml and download dependencies first (for better caching)
COPY pom.xml .
COPY cyrene-common/pom.xml ./cyrene-common/pom.xml
COPY cyrene-service-system/pom.xml ./cyrene-service-system/pom.xml
COPY cyrene-starter-solon/pom.xml ./cyrene-starter-solon/pom.xml
COPY cyrene-starter-springboot/pom.xml ./cyrene-starter-springboot/pom.xml

# Copy source code
COPY cyrene-common/ ./cyrene-common/
COPY cyrene-service-system/ ./cyrene-service-system/
COPY cyrene-starter-solon/ ./cyrene-starter-solon/
COPY cyrene-starter-springboot/ ./cyrene-starter-springboot/

# Build the project (select solon starter as default)
RUN mvn clean package -pl cyrene-starter-solon -am -DskipTests

# Stage 2: Build frontend
FROM node:20-alpine AS frontend-builder

WORKDIR /app

# Copy frontend files
COPY cyrene-ui/package*.json ./cyrene-ui/
COPY cyrene-ui/vite.config.ts ./cyrene-ui/
COPY cyrene-ui/tsconfig*.json ./cyrene-ui/
COPY cyrene-ui/babel.config.js ./cyrene-ui/

# Change to frontend directory and install dependencies
WORKDIR /app/cyrene-ui
RUN npm install

# Copy remaining frontend source
COPY cyrene-ui/src ./src/
COPY cyrene-ui/index.html ./
COPY cyrene-ui/shims-vue.d.ts ./

# Build frontend
RUN npm run build

# Stage 3: Create runtime image
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="CyreneAdmin" \
      description="CyreneAdmin Backend and Frontend Application" \
      version="1.0.0"

# Install nginx and supervisor
RUN apk add --no-cache nginx supervisor

# Create app directory
WORKDIR /app

# Copy built JAR from backend builder
COPY --from=backend-builder /app/cyrene-starter-solon/target/launcher.jar app.jar

# Copy built frontend from frontend builder
COPY --from=frontend-builder /app/cyrene-ui/dist/ /var/www/html/

# Copy configuration files
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# Expose ports
EXPOSE 80 9000

# Set health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:80/ || exit 1

# Run supervisord to manage both processes
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]