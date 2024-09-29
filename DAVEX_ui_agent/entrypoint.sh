#!/bin/sh

echo "Running entrypoint script..."

# 检查文件是否存在
if [ ! -f /usr/share/nginx/html/env-config.js.template ]; then
    echo "env-config.js.template not found!"
    exit 1
fi

# 处理环境变量并生成配置文件
envsubst '${VUE_APP_API_BASE_URL}' < /usr/share/nginx/html/env-config.js.template > /usr/share/nginx/html/env-config.js

# 确保生成文件成功
if [ ! -f /usr/share/nginx/html/env-config.js ]; then
    echo "Failed to create env-config.js!"
    exit 1
fi

echo "Successfully created env-config.js"

# 启动 Nginx
exec nginx -g "daemon off;"
