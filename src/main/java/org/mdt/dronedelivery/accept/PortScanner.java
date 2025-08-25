package org.mdt.dronedelivery.accept;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Component
public class PortScanner {

    private static final Logger logger = LoggerFactory.getLogger(PortScanner.class);
    private final PortManager portManager;
    private final MavlinkListener mavlinkListener;
    private final Map<Integer, Future<?>> activeListeners = new ConcurrentHashMap<>();
    private final int scannerTimeoutMs;
    private final int bufferSize;

    public PortScanner(
            PortManager portManager,
            MavlinkListener mavlinkListener,
            @Value("${drone-delivery.scanner-timeout-ms:3000}") int scannerTimeoutMs,
            @Value("${drone-delivery.buffer-size:1024}") int bufferSize) {
        this.portManager = portManager;
        this.mavlinkListener = mavlinkListener;
        this.scannerTimeoutMs = scannerTimeoutMs;
        this.bufferSize = bufferSize;
    }



    public void scanPorts() {
        Map<Integer, DatagramChannel> channels = new ConcurrentHashMap<>();
        try (Selector selector = Selector.open()) {
            while (!Thread.currentThread().isInterrupted()) {
                updateChannels(channels, selector);
                removeChannel(channels);
                processIncomingPackets(selector, channels);
            }
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {
                logger.error("Error in port scanner: {}", e.getMessage());
            }
        } finally {
            channels.values().forEach(channel -> {
                try {
                    channel.close();
                } catch (IOException e) {
                    logger.error("Error closing channel: {}", e.getMessage());
                }
            });
            channels.clear();
            logger.info("Port scanner stopped, all channels closed.");
        }
    }

    private void processIncomingPackets(Selector selector, Map<Integer, DatagramChannel> channels) {
        try {
            selector.select(scannerTimeoutMs);
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isReadable()) {
                    DatagramChannel channel = (DatagramChannel) key.channel();
                    int port = ((InetSocketAddress) channel.getLocalAddress()).getPort();
                    ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
                    SocketAddress sender = channel.receive(buffer);
                    if (sender != null) {
                        logger.info("Received packet on port {} from {} (size={} bytes)", port, sender, buffer.position());
                        synchronized (this) {
                            if (!activeListeners.containsKey(port)) {
                                try {
                                    channel.close();
                                    channels.remove(port);
                                    Future<?> future = mavlinkListener.listenOnPort(port);
                                    Future<?> existing = activeListeners.putIfAbsent(port, future);
                                    if (existing == null) {
                                        logger.info("Started MAVLink UDP listener on port {}", port);
                                    } else {
                                        future.cancel(true);
                                    }
                                } catch (IOException e) {
                                    logger.error("Error closing channel for port {}: {}", port, e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
            selector.selectedKeys().clear();
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {
                logger.error("Error in selector: {}", e.getMessage());
            }
        }
    }

    private void removeChannel(Map<Integer, DatagramChannel> channels) {
        channels.entrySet().removeIf(entry -> {
            Integer port = entry.getKey();
            if (!portManager.getPortsToScan().contains(port)) {
                try {
                    entry.getValue().close();
                    logger.info("Closed channel for removed port {}", port);
                    return true;
                } catch (IOException e) {
                    logger.error("Error closing channel for port {}: {}", port, e.getMessage());
                    return true;
                }
            }
            return false;
        });
    }

    private void updateChannels(Map<Integer, DatagramChannel> channels, Selector selector) {
        for (Integer port : portManager.getPortsToScan()) {
            if (!channels.containsKey(port) && !activeListeners.containsKey(port)) {
                try {
                    DatagramChannel channel = DatagramChannel.open();
                    channel.configureBlocking(false);
                    channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                    channel.bind(new InetSocketAddress("0.0.0.0", port));
                    channel.register(selector, SelectionKey.OP_READ);
                    channels.put(port, channel);
                    logger.info("Monitoring new port {}", port);
                } catch (IOException e) {
                    logger.error("Failed to open channel for port {}: {}", port, e.getMessage());
                }
            }
        }
    }

    public synchronized void stopAllListeners() {
        activeListeners.keySet().forEach(this::stopListeningOnPort);
    }

    public synchronized void stopListeningOnPort(int port) {
        Future<?> future = activeListeners.remove(port);
        if (future != null) {
            future.cancel(true);
            logger.info("Stopped MAVLink UDP listener on port {}", port);
        } else {
            logger.info("No active listener found on port {}", port);
        }
    }
}