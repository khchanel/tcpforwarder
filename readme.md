# TCP Forwarder

Listen on a port and forward traffic to a target host and port.

## Build

```bash
mvn install
```

## Usage

### Local Execution (Server Mode)
Runs the full Spring Boot application with the Web UI and rule management.
```bash
java -jar target/tcpforwarder-*.jar <incomingPort> <targetHost> <targetPort>
# Example
java -jar target/tcpforwarder-1.0.jar 8000 localhost 8888
```

### Simple Forwarder (Ad-hoc CLI Mode)
A lightweight, one-shot utility for quick debugging without the Spring Boot overhead.
```bash
java -cp "target/tcpforwarder-*.jar" tcpforwarder.tcpforwarder.cli.SimpleForwarder <listenPort> <targetHost> <targetPort>
# Example
java -cp "target/tcpforwarder-1.0.jar" tcpforwarder.tcpforwarder.cli.SimpleForwarder 8001 google.com 80
```

### Docker
You can run the forwarder using Docker. Note that any ports you intend to forward must be exposed via the `-p` flag.

**Build the image:**
```bash
docker build -t tcpforwarder .
```

**Run the container:**
```bash
docker run -d \
  -p 8080:8080 \
  -p 8000:8000 \
  -e FORWARDER_API_KEY=mysecret \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/logs:/app/logs \
  --name tcp-forwarder \
  tcpforwarder
```

### Docker Compose
The easiest way to manage the service and its volumes is using Docker Compose.

1. Edit `docker-compose.yml` to add the ports you need to forward under the `ports` section.
2. Start the service:
```bash
docker compose up -d
```