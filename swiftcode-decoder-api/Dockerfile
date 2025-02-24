# Use an official Java runtime as a parent image for building
FROM openjdk:21-jdk-slim as builder

# Install Maven in the builder image
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Set the working directory in the container
WORKDIR /app

# Copy the Maven build file (pom.xml) into the container
COPY pom.xml .

# Copy the project files into the container
COPY src ./src

# Package the application (this will create the .jar file in the target folder)
RUN mvn clean package -DskipTests

# Use a full JDK image for running the application
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the builder image (ensure the JAR filename matches your artifactId from pom.xl)
COPY --from=builder /app/target/swiftcode-decoder-api-*.jar /app/swiftcode-decoder-api.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/swiftcode-decoder-api.jar"]