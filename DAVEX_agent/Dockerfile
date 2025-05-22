# 精简版
FROM openjdk:17-jdk-alpine

# Install make, build tools, python, and pip
RUN apk update && apk add make build-base python3 python3-dev py3-pip

# Define build argument for the JAR file location
ARG JAR_FILE=target/davex-agent-0.0.1.jar

# Copy the JAR file into the container
COPY ${JAR_FILE} davex-agent-0.0.1.jar

# Define build argument for the yml file location
ARG YML_FILE=src/main/resources/application-template.yml

# Copy the YML file into the container
COPY ${YML_FILE} application-template.yml

# Specify the entry point to run the JAR
ENTRYPOINT ["sh", "-c", "java -jar /davex-agent-0.0.1.jar --spring.config.location=/application-template.yml"]


# 完全版（包含隐语）
# FROM openjdk:17-jdk-slim

# # 设置国内源，加速 apt 下载
# RUN sed -i 's|http://deb.debian.org|http://mirrors.aliyun.com|g' /etc/apt/sources.list && \
#     apt update && \
#     apt install -y --no-install-recommends \
#         python3 python3-dev python3-pip \
#         gcc g++ make build-essential \
#         libssl-dev && \
#     apt clean && rm -rf /var/lib/apt/lists/*

# # 升级 pip 并安装 Python 包
# RUN pip3 install --upgrade pip -i https://pypi.tuna.tsinghua.edu.cn/simple && \
#     pip3 install -i https://pypi.tuna.tsinghua.edu.cn/simple -U secretflow unidecode

# # 拷贝 JAR 和配置文件
# ARG JAR_FILE=target/davex-agent-0.0.1.jar
# COPY ${JAR_FILE} /app/davex-agent-0.0.1.jar

# ARG YML_FILE=src/main/resources/application-template.yml
# COPY ${YML_FILE} /app/application-template.yml

# # 启动应用
# ENTRYPOINT ["sh", "-c", "java -jar /app/davex-agent-0.0.1.jar --spring.config.location=/app/application-template.yml"]
