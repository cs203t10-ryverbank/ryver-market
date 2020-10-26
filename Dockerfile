# Use Java 11 as a base image.
FROM openjdk:11-jre
# Set the working directory on the image.
WORKDIR /app
# Copy the packaged jar to the image.
COPY ./target/out.jar .
# Copy the lib files to the image.
COPY ./lib ./lib
# Run the application.
ENTRYPOINT ["java", "-jar", "./out.jar"]

