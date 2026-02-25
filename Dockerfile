# Multi-stage build for CyreneAdmin backend and frontend
# Stage 1: Build backend JAR
FROM maven:3.9.12-eclipse-temurin-17 AS backend-builder

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
FROM node:current-alpine AS frontend-builder

WORKDIR /app

# Copy frontend files
COPY cyrene-ui/package*.json ./cyrene-ui/
COPY cyrene-ui/vite.config.ts ./cyrene-ui/
COPY cyrene-ui/tsconfig*.json ./cyrene-ui/
COPY cyrene-ui/babel.config.js ./cyrene-ui/

# Change to frontend directory and install dependencies
WORKDIR /app/cyrene-ui
RUN npm install -g pnpm && \
    pnpm config set store-dir ~/.pnpm-store && \
    pnpm install --frozen-lockfile=false

# Copy remaining frontend source
COPY cyrene-ui/src ./src/
COPY cyrene-ui/index.html ./
COPY cyrene-ui/src/shims-vue.d.ts ./src/

# Build frontend
RUN pnpm run build || (echo "Build failed, showing logs:" && ls -la /root/.npm/_logs/ 2>/dev/null && exit 1)

# Stage 3: Create runtime image with Alpine, Nginx and JDK 17
FROM alpine:latest

LABEL maintainer="CyreneAdmin" \
      description="CyreneAdmin Backend and Frontend Application" \
      version="1.0.0"

# Configure APK repositories to use Aliyun mirror for better reliability
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories

# Update package index
RUN apk update

# Install packages in separate steps for better reliability
RUN apk add --no-cache nginx
RUN apk add --no-cache openjdk17-jre
RUN apk add --no-cache curl
RUN apk add --no-cache fontconfig ttf-dejavu

# Create app directory
WORKDIR /app

# Copy built JAR from backend builder
COPY --from=backend-builder /app/cyrene-starter-solon/target/launcher.jar app.jar

# Copy built frontend from frontend builder
COPY --from=frontend-builder /app/cyrene-ui/dist/ /var/www/html/

# Create necessary directories and set permissions
RUN mkdir -p /var/log/supervisor /var/run/nginx && \
    chmod 755 /var/log/supervisor && \
    chown -R nginx:nginx /var/www/html

# Copy configuration files
COPY nginx.conf /etc/nginx/http.d/default.conf
COPY start.sh /start.sh
RUN chmod +x /start.sh

# Configure Nginx
RUN echo "daemon off;" >> /etc/nginx/nginx.conf

# Expose ports
EXPOSE 80 9000

# Set health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:80/ || exit 1

# Run start script to manage both processes
CMD ["/start.sh"]