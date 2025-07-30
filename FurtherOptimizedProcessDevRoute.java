import java.util.*;
import java.util.stream.Collectors;

public class FurtherOptimizedProcessDevRoute {
    
    private static final int MAX_NEAREST_DEVICES = 5;
    
    public void processDevRoute(NearNode nearNode, List<NearbyDeviceInfoVo> nearDevs, String token) {
        Node node = nearNode.getNode();
        double[] sourceLngLat = nearNode.getSourceLngLat();
        
        // 1. 使用最小堆直接获取最近的设备，避免全量排序
        List<NearbyDeviceInfoVo> nearestDevices = getNearestDevicesOptimized(nearDevs, sourceLngLat, MAX_NEAREST_DEVICES);
        
        // 2. 计算路由
        List<CalcRouteVo> calcRouteVos = getRoutes(nearNode, token, nearestDevices, NearbyDeviceInfoVo::getLngLat);
        
        // 3. 构建结果（使用预分配容量提高性能）
        List<ClosestNode> closestNodes = buildClosestNodesOptimized(node, sourceLngLat, nearestDevices, calcRouteVos);
        
        // 4. 处理结果
        handleNearNode(nearNode, closestNodes);
    }

    /**
     * 使用最小堆优化获取最近的设备
     */
    private List<NearbyDeviceInfoVo> getNearestDevicesOptimized(List<NearbyDeviceInfoVo> devices, 
                                                               double[] sourceLngLat, int count) {
        // 使用最大堆保持最近的N个设备
        PriorityQueue<DeviceWithDistance> maxHeap = new PriorityQueue<>(
            count, Comparator.comparingDouble(DeviceWithDistance::getDistance).reversed());
        
        for (NearbyDeviceInfoVo device : devices) {
            double[] lngLat = device.getLngLat();
            if (lngLat == null || lngLat.length < 2) {
                continue;
            }
            
            double distance = LineDistanceQuery.calculateHaversineDistance(
                sourceLngLat[0], sourceLngLat[1], lngLat[0], lngLat[1]);
            
            DeviceWithDistance deviceWithDistance = new DeviceWithDistance(device, distance);
            
            if (maxHeap.size() < count) {
                maxHeap.offer(deviceWithDistance);
            } else if (distance < maxHeap.peek().getDistance()) {
                maxHeap.poll();
                maxHeap.offer(deviceWithDistance);
            }
        }
        
        // 转换为列表并排序
        return maxHeap.stream()
            .sorted(Comparator.comparingDouble(DeviceWithDistance::getDistance))
            .map(DeviceWithDistance::getDevice)
            .collect(Collectors.toList());
    }

    /**
     * 优化的构建ClosestNode列表
     */
    private List<ClosestNode> buildClosestNodesOptimized(Node node, double[] sourceLngLat, 
                                                        List<NearbyDeviceInfoVo> nearestDevices, 
                                                        List<CalcRouteVo> calcRouteVos) {
        // 预分配容量避免动态扩容
        int expectedSize = Math.min(calcRouteVos.size(), nearestDevices.size());
        List<ClosestNode> closestNodes = new ArrayList<>(expectedSize);
        
        for (int i = 0; i < calcRouteVos.size() && i < nearestDevices.size(); i++) {
            CalcRouteVo routeVo = calcRouteVos.get(i);
            NearbyDeviceInfoVo devInfo = nearestDevices.get(i);
            
            // 跳过无效路由
            if (isInvalidRoute(routeVo)) {
                continue;
            }
            
            ClosestNode closestNode = createClosestNode(node, devInfo, sourceLngLat, routeVo);
            closestNodes.add(closestNode);
        }
        
        return closestNodes;
    }

    /**
     * 检查路由是否无效
     */
    private boolean isInvalidRoute(CalcRouteVo routeVo) {
        return routeVo == null || routeVo.getTotalLength() == null || routeVo.getTotalLength() == 0.0;
    }

    /**
     * 创建ClosestNode对象
     */
    private ClosestNode createClosestNode(Node node, NearbyDeviceInfoVo devInfo, 
                                        double[] sourceLngLat, CalcRouteVo routeVo) {
        ClosestNode closestNode = new ClosestNode(node, devInfo, sourceLngLat, devInfo.getLngLat());
        closestNode.setLength(routeVo.getTotalLength());
        closestNode.setRouteNodeList(routeVo.getNodeList());
        return closestNode;
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