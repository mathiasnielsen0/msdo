# Dockerfile

# Use latest Python runtime as base image
FROM henrikbaerbak/jdk8-gradle

# Set the working directory to /app and copy current dir
WORKDIR /root/hello
COPY build.gradle /root/hello
COPY HelloSpark.java /root/hello/src/main/java/example/

# Run gradle hello when container starts
CMD ["gradle hello"]