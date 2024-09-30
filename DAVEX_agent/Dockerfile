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
