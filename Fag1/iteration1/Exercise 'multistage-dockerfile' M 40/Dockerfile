# Dockerfile


# STEP 1
# Use latest Python runtime as base image
FROM henrikbaerbak/jdk17-gradle83 AS builder

LABEL maintainer='Golf'

# Set the working directory to /app and copy current dir
WORKDIR /root/cave
COPY skycave/ /root/cave

# Expose port 7777
EXPOSE 7777

# Run tests
RUN gradle test

# Create server fat jar
RUN gradle :server:jar




# STEP 2
FROM openjdk:17-slim

#Label maintainer
LABEL maintainer='Golf'

#Set working directory
WORKDIR /root/cave

#Copy fatJar daemon.jar file
COPY --from=builder /root/cave/server/build/libs/daemon.jar /root/cave

#Copy cpf files
COPY  --from=builder /root/cave/server/src/main/resources/cpf/ /root/cave/cpf/

# EXPOSE PORT 7777
EXPOSE 7777

# Default command to run
CMD ["java", "-jar", "daemon.jar", "http.cpf"]