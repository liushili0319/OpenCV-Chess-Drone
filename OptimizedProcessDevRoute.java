import java.util.*;
import java.util.stream.Collectors;

public class OptimizedProcessDevRoute {
    
    public void processDevRoute(NearNode nearNode, List<NearbyDeviceInfoVo> nearDevs, String token) {
        Node node = nearNode.getNode();
        double[] sourceLngLat = nearNode.getSourceLngLat();
        
        // 1. 过滤有效设备并计算距离
        List<DeviceWithDistance> validDevices = filterAndCalculateDistances(nearDevs, sourceLngLat);
        
        // 2. 获取最近的5个设备
        List<NearbyDeviceInfoVo> nearestDevices = getNearestDevices(validDevices, 5);
        
        // 3. 计算路由
        List<CalcRouteVo> calcRouteVos = getRoutes(nearNode, token, nearestDevices, NearbyDeviceInfoVo::getLngLat);
        
        // 4. 构建结果
        List<ClosestNode> closestNodes = buildClosestNodes(node, sourceLngLat, nearestDevices, calcRouteVos);
        
        // 5. 处理结果
        handleNearNode(nearNode, closestNodes);
    }

    /**
     * 过滤有效设备并计算距离
     */
    private List<DeviceWithDistance> filterAndCalculateDistances(List<NearbyDeviceInfoVo> devices, double[] sourceLngLat) {
        return devices.stream()
            .filter(device -> {
                double[] lngLat = device.getLngLat();
                return lngLat != null && lngLat.length >= 2;
            })
            .map(device -> {
                double[] lngLat = device.getLngLat();
                double distance = LineDistanceQuery.calculateHaversineDistance(
                    sourceLngLat[0], sourceLngLat[1], lngLat[0], lngLat[1]);
                return new DeviceWithDistance(device, distance);
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取最近的N个设备
     */
    private List<NearbyDeviceInfoVo> getNearestDevices(List<DeviceWithDistance> devicesWithDistance, int count) {
        return devicesWithDistance.stream()
            .sorted(Comparator.comparingDouble(DeviceWithDistance::getDistance))
            .limit(count)
            .map(DeviceWithDistance::getDevice)
            .collect(Collectors.toList());
    }

    /**
     * 构建ClosestNode列表
     */
    private List<ClosestNode> buildClosestNodes(Node node, double[] sourceLngLat, 
                                              List<NearbyDeviceInfoVo> nearestDevices, 
                                              List<CalcRouteVo> calcRouteVos) {
        List<ClosestNode> closestNodes = new ArrayList<>();
        
        for (int i = 0; i < calcRouteVos.size() && i < nearestDevices.size(); i++) {
            CalcRouteVo routeVo = calcRouteVos.get(i);
            NearbyDeviceInfoVo devInfo = nearestDevices.get(i);
            
            // 跳过无效路由
            if (routeVo == null || routeVo.getTotalLength() == null || routeVo.getTotalLength() == 0.0) {
                continue;
            }
            
            ClosestNode closestNode = new ClosestNode(node, devInfo, sourceLngLat, devInfo.getLngLat());
            closestNode.setLength(routeVo.getTotalLength());
            closestNode.setRouteNodeList(routeVo.getNodeList());
            
            closestNodes.add(closestNode);
        }
        
        return closestNodes;
    }

    /**
     * 设备与距离的包装类
     */
    private static class DeviceWithDistance {
        private final NearbyDeviceInfoVo device;
        private final double distance;
        
        public DeviceWithDistance(NearbyDeviceInfoVo device, double distance) {
            this.device = device;
            this.distance = distance;
        }
        
        public NearbyDeviceInfoVo getDevice() {
            return device;
        }
        
        public double getDistance() {
            return distance;
        }
    }
}