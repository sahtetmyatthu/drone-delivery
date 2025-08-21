package org.mdt.dronedelivery.entry;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class MavlinkUdpInputEntry {

    private static final Logger logger = LoggerFactory.getLogger(MavlinkUdpInputEntry.class);
    private final ExecutorService executorService;
    private final PortManager portManager;
    private final PortScanner portScanner;
    private volatile boolean running = true;

    @Autowired
    public MavlinkUdpInputEntry(PortManager portManager, PortScanner portScanner,
                                @Value("${drone-delivery.max-ports}") int maxPorts) {
        this.portManager = portManager;
        this.portScanner = portScanner;
        this.executorService = Executors.newFixedThreadPool(maxPorts);
    }

    @PostConstruct
    public void init() {
        portManager.addPortsToScan(Arrays.asList(1500, 1501, 1502));
        if (!portManager.getPortsToScan().isEmpty()) {
            executorService.submit(portScanner::scanPorts);
            logger.info("Started port scanner for initial ports: {}", portManager.getPortsToScan());
        } else {
            logger.error("No valid initial ports provided. Port scanner not started.");
        }
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        portScanner.stopAllListeners();
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                logger.warn("Forcibly shutting down executor service.");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            logger.error("Shutdown interrupted: {}", e.getMessage());
        }
        logger.info("All MAVLink UDP listeners and port scanner stopped.");
    }
}