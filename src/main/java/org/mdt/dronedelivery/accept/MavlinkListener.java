package org.mdt.dronedelivery.accept;

import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import org.mdt.dronedelivery.process.MavlinkMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Component
public class MavlinkListener {

    private static final Logger logger = LoggerFactory.getLogger(MavlinkListener.class);
    private final ExecutorService executorService;
    private final int listenerTimeoutMs;
    private final MavlinkMessageHandler mavlinkMessageHandler;

    public MavlinkListener(
            @Value("${drone-delivery.thread-pool-size:100}") int threadPoolSize,
            @Value("${drone-delivery.listener-timeout-ms:30000}") int listenerTimeoutMs,
            MavlinkMessageHandler mavlinkMessageHandler) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.listenerTimeoutMs = listenerTimeoutMs;
        this.mavlinkMessageHandler = mavlinkMessageHandler;
    }

    public Future<?> listenOnPort(int port, Consumer<Integer> onStopCallback) {
        return executorService.submit(() -> {
            DatagramSocket udpSocket = null;
            try {
                udpSocket = new DatagramSocket(null);
                udpSocket.setReuseAddress(true);
                udpSocket.bind(new InetSocketAddress("0.0.0.0", port));
                udpSocket.setSoTimeout(listenerTimeoutMs);

                UdpInputStream udpInputStream = new UdpInputStream(udpSocket);
                MavlinkConnection mavlinkConnection = MavlinkConnection.create(udpInputStream, null);

                logger.info("Listener started on port {}", port);

                long lastPacketTime = System.currentTimeMillis();
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        MavlinkMessage<?> message = mavlinkConnection.next();
                        if (message != null) {
                            lastPacketTime = System.currentTimeMillis();
                            InetAddress sender = udpInputStream.getSenderAddress();
                            mavlinkMessageHandler.handleMessage(message, port, udpSocket, sender);
                            logger.info("MAVLink message {} from {} on port {}",
                                    message.getPayload().getClass().getSimpleName(), sender, port);
                        } else if (System.currentTimeMillis() - lastPacketTime > listenerTimeoutMs) {
                            logger.info("No packets on port {} for {}ms, stopping listener", port, listenerTimeoutMs);
                            onStopCallback.accept(port);
                            return;
                        }
                    } catch (SocketTimeoutException e) {
                        if (System.currentTimeMillis() - lastPacketTime > listenerTimeoutMs) {
                            logger.info("No packets on port {} for {}ms, stopping listener", port, listenerTimeoutMs);
                            onStopCallback.accept(port);
                            return;
                        }
                    } catch (IOException e) {
                        if (Thread.currentThread().isInterrupted()) {
                            logger.info("Listener thread interrupted for UDP port: {}", port);
                            return;
                        }
                        logger.error("IO error on UDP port {}: {}", port, e.getMessage());
                        onStopCallback.accept(port);
                        return;
                    }
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    logger.error("Error initializing UDP socket for port {}: {}", port, e.getMessage());
                }
            } finally {
                if (udpSocket != null && !udpSocket.isClosed()) {
                    try {
                        udpSocket.close();
                        logger.debug("Closed UDP socket for port {}", port);
                    } catch (Exception e) {
                        logger.error("Error closing UDP socket for port {}: {}", port, e.getMessage());
                    }
                }
            }
        });
    }


}