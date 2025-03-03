# TCP Forwarder

listen on a port and forward traffic to a target host and port

## Build

```
mvn install
```

## Usage


```
java -jar TcpForwarder-<version>.jar <incomingPort> <targetHost> <targetPort>

java -jar TcpForwarder-1.0.jar 8000 localhost 8888
```
