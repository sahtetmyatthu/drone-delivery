# MAVLink UDP Input Entry System

## Overview
The MAVLink UDP Input Entry system is a Spring-based Java application designed to monitor and process MAVLink messages over UDP for a drone delivery system. It dynamically scans configured UDP ports, detects incoming packets, and spawns listeners to handle MAVLink messages using the `dronefleet.mavlink` library. The system is modular, scalable, and thread-safe, with components for port management, scanning, and message processing.

### Components
- **MavlinkUdpInputEntry**: Initializes and shuts down the system, managing the port scanner and listeners.
- **PortScanner**: Monitors UDP ports for packets and spawns listeners when packets are detected.
- **MavlinkListener**: Processes MAVLink messages on specific ports with timeout handling.
- **PortManager**: Manages the list of ports to scan, enforcing valid ranges and limits.

## Features
- Non-blocking UDP communication using Java NIO (`Selector`, `DatagramChannel`).
- Thread-safe operations with `ConcurrentHashMap` and `CopyOnWriteArrayList`.
- Configurable via Spring properties for port ranges, timeouts, and thread pool sizes.
- Graceful shutdown with resource cleanup.
- Logging with SLF4J for debugging and monitoring.

## Prerequisites
- Java 17 or higher
- Maven for dependency management
- Spring Boot
- Dependencies:
    - `org.slf4j:slf4j-api` (logging)
    - `io.dronefleet:mavlink` (MAVLink message parsing)
    - `org.projectlombok:lombok` (optional, for `PortManager`)

## Setup
1. **Clone the Repository**:
   ```bash
   git clone  https://github.com/sahtetmyatthu/drone-delivery.git
   cd drone-delivery
