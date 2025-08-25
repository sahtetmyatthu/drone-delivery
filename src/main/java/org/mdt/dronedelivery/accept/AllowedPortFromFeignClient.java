package org.mdt.dronedelivery.accept;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AllowedPortFromFeignClient {
//
//    private final ScheduleFeignClient scheduleFeignClient;
//    private final PortManager portManager;   // inject your existing PortManager
//
//    @PostConstruct
//    public void loadAllowedPortsFromFeignClient() {
//        List<Integer> allowedPorts = new ArrayList<>();
//        try {
//            List<GCSIpAndPort> endPoints = scheduleFeignClient.getCurrentlyUsedGCSIpAndPorts().getBody();
//            if (endPoints != null) {
//                for (GCSIpAndPort endPoint : endPoints) {
//                    if (endPoint.getGcsPort1() != null) {
//                        allowedPorts.add(endPoint.getGcsPort1());
//                    }
//                    if (endPoint.getGcsPort2() != null) {
//                        allowedPorts.add(endPoint.getGcsPort2());
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Failed to load ports: " + e.getMessage());
//        }
//
//        portManager.addPortsToScan(allowedPorts); // <--- feed into PortManager
//        System.out.println("Loaded allowed ports: " + allowedPorts);
//    }
}
