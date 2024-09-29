FROM nginx
# 复制nginx配置文件
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 复制构建好的前端文件到nginx的静态文件目录
COPY dist/  /usr/share/nginx/html/
# 复制env-config.js.template到静态文件目录
COPY env-config.js.template /usr/share/nginx/html/env-config.js.template
# 复制entrypoint脚本，用于容器启动时注入环境变量
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
# 设置容器启动时执行的脚本
ENTRYPOINT ["/entrypoint.sh"]