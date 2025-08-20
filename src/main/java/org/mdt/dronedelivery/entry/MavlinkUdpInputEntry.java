package org.mdt.dronedelivery.entry;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MavlinkUdpInputEntry {

    private final ExecutorService  executorService = Executors.newFixedThreadPool(100);
    private final Map<Integer, Future<?>> activeListeners = new ConcurrentHashMap<>();
    private final List<Integer> portsToScan = new CopyOnWriteArrayList<>();
    private volatile  boolean running = true;
    private static final int MAX_PORTS = 100;
    private static final int LISTENER_TIMEOUT_MS = 30_000;
    private static final int SCANNER_TIMEOUT_MS = 3_000;
    private static final int BUFFER_SIZE = 1024;

    @PostConstruct
    public void init() {
        List<Integer> initialPorts = Arrays.asList(1500, 1501, 1502);
        for (Integer port : initialPorts) {
            if (isValidPort(port)) {
                portsToScan.add(port);
            } else {
                System.err.println("Invalid initial port " + port + ": Port must be between 1 and 65535.");
            }
        }
        if (!portsToScan.isEmpty()) {
            startPortScanner();
            System.out.println("Started port scanner for initial ports: " + portsToScan);
        } else {
            System.err.println("No valid initial ports provided. Port scanner not started.");
        }
    }


    private void startPortScanner(){
        executorService.submit(this::scanPorts);
        System.out.println("Port scanner thread started.");
    }

    private boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }

    private void scanPorts(){
        Map<Integer, DatagramChannel> channels = new ConcurrentHashMap<>();
        try(Selector selector = Selector.open()) {
            while (running && !Thread.currentThread().isInterrupted()) {
                // Update channels for add ports
                for (Integer port : portsToScan) {
                    if (!channels.containsKey(port) && !activeListeners.containsKey(port)) {
                        try {
                            DatagramChannel channel = DatagramChannel.open();
                            channel.configureBlocking(false);
                            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                            channel.bind(new InetSocketAddress("0.0.0.0", port));
                            channel.register(selector, SelectionKey.OP_READ);
                            channels.put(port, channel);
                            System.out.println("Monitoring new port " + port);
                        } catch (IOException e) {
                            System.err.println("Failed to open channel for port " + port + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {
                System.err.println("Error in port scanner: " + e.getMessage());
            }
        } finally {
            channels.values().forEach(channel -> {
                try {
                    channel.close();
                } catch (IOException e) {
                    System.err.println("Error closing channel: " + e.getMessage());
                }
            });
            channels.clear();
            System.out.println("Port scanner stopped, all channels closed.");
        }
    }
}