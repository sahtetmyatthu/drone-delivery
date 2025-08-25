package org.mdt.dronedelivery.accept;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Component
public class PortManager {

    private static final Logger logger = LoggerFactory.getLogger(PortManager.class);
    private final List<Integer> portsToScan = new CopyOnWriteArrayList<>();
    private final int maxPorts;
    private final int minPort;
    private final int maxPort;

    public PortManager(
            @Value("${drone-delivery.max-ports:100}") int maxPorts,
            @Value("${drone-delivery.port-range.min:1}") int minPort,
            @Value("${drone-delivery.port-range.max:65535}") int maxPort) {
        this.maxPorts = maxPorts;
        this.minPort = minPort;
        this.maxPort = maxPort;
    }

    public synchronized void addPortsToScan(List<Integer> newPorts) {
        int currentSize = portsToScan.size();
        for (Integer port : newPorts) {
            if (currentSize >= maxPorts) {
                logger.error("Cannot add port {}: Maximum port limit ({}) reached.", port, maxPorts);
                continue;
            }
            if (!isValidPort(port)) {
                logger.error("Invalid port {}: Port must be between {} and {}.", port, minPort, maxPort);
                continue;
            }
            if (!portsToScan.contains(port)) {
                portsToScan.add(port);
                currentSize++;
                logger.info("Added port {} to scan list. Current ports: {}", port, portsToScan);
            } else {
                logger.info("Port {} is already in the scan list, skipping.", port);
            }
        }
    }

    public synchronized void removePortsFromScan(List<Integer> portsToRemove) {
        for (Integer port : portsToRemove) {
            if (portsToScan.remove(port)) {
                logger.info("Removed port {} from scan list. Current ports: {}", port, portsToScan);
            } else {
                logger.info("Port {} not in scan list, skipping.", port);
            }
        }
    }

    private boolean isValidPort(int port) {
        return port >= minPort && port <= maxPort;
    }
}