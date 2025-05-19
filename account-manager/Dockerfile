# Use an OpenJDK base image for Java 21
FROM eclipse-temurin:21-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/account-manager-*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]