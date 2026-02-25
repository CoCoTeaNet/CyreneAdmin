#!/bin/sh

# 创建必要的目录
mkdir -p /var/log/supervisor /var/run/nginx

# 启动 Nginx（后台运行）
echo "Starting Nginx..."
nginx &

# 等待 Nginx 启动
sleep 2

# 检查 Nginx 是否启动成功
if ! pgrep nginx > /dev/null; then
    echo "ERROR: Nginx failed to start"
    exit 1
fi

echo "Nginx started successfully"

# 启动 Java 应用（前台运行）
echo "Starting Java application..."
exec java -Xms512m -Xmx512m -XX:+UseContainerSupport \
    -Dfile.encoding=UTF-8 \
    -jar /app/app.jar --env=docker