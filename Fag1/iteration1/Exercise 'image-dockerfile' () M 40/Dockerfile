# Dockerfile

# Use latest Python runtime as base image
FROM henrikbaerbak/jdk17-gradle83

LABEL maintainer='golf'

# Set the working directory to /app and copy current dir
WORKDIR /root/cave
COPY Dockerfile /root
COPY skycave/ /root/cave

# Expose port 7777
EXPOSE 7777

# Run tests
RUN gradle test

# Run hello_world.py when the container launches
CMD ["gradle", "daemon", "-Pcpf=http.cpf"]