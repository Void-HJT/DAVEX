FROM openjdk:17-jdk-alpine

# Install make, build tools, python, and pip
RUN apk update && apk add make build-base python3 python3-dev py3-pip

# Define build argument for the JAR file location
ARG JAR_FILE=target/davex-agent-0.0.1.jar

# Copy the JAR file into the container
COPY ${JAR_FILE} davex-agent-0.0.1.jar

# Specify the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "/davex-agent-0.0.1.jar"]
