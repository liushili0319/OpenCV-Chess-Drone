/**
 * 优化后的设备路由处理方法
 * 
 * 主要优化点：
 * 1. 减少重复变量声明
 * 2. 使用流式处理简化代码
 * 3. 提前验证参数
 * 4. 优化数据结构使用
 * 5. 改进异常处理
 */
public void processDevRoute(NearNode nearNode, List<NearbyDeviceInfoVo> nearDevs, String token) {
    // 参数验证
    if (nearNode == null || nearDevs == null || nearDevs.isEmpty() || token == null) {
        return;
    }

    Node node = nearNode.getNode();
    double[] sourceLngLat = nearNode.getSourceLngLat();
    
    // 验证源坐标
    if (sourceLngLat == null || sourceLngLat.length < 2) {
        return;
    }

    // 使用流式处理过滤并获取最近的5个有效设备
    List<NearbyDeviceInfoVo> nearestDevices = nearDevs.stream()
        .filter(this::isValidDevice)  // 过滤有效设备
        .sorted(Comparator.comparingDouble(dev -> 
            calculateDistance(sourceLngLat, dev.getLngLat())))  // 按距离排序
        .limit(5)  // 取前5个
        .collect(Collectors.toList());

    if (nearestDevices.isEmpty()) {
        return;
    }

    // 批量计算路由
    List<CalcRouteVo> calcRouteVos = getRoutes(nearNode, token, nearestDevices, NearbyDeviceInfoVo::getLngLat);
    
    // 构建最近节点列表
    List<ClosestNode> closestNodes = buildClosestNodes(node, sourceLngLat, nearestDevices, calcRouteVos);
    
    // 处理近邻节点
    handleNearNode(nearNode, closestNodes);
}

/**
 * 验证设备是否有效（坐标不为空且长度正确）
 */
private boolean isValidDevice(NearbyDeviceInfoVo device) {
    if (device == null) {
        return false;
    }
    double[] lngLat = device.getLngLat();
    return lngLat != null && lngLat.length >= 2;
}

/**
 * 计算两点间的距离
 */
private double calculateDistance(double[] sourceLngLat, double[] targetLngLat) {
    if (targetLngLat == null || targetLngLat.length < 2) {
        return Double.MAX_VALUE;
    }
    return LineDistanceQuery.calculateHaversineDistance(
        sourceLngLat[0], sourceLngLat[1], 
        targetLngLat[0], targetLngLat[1]
    );
}

/**
 * 构建最近节点列表
 */
private List<ClosestNode> buildClosestNodes(Node node, double[] sourceLngLat, 
                                          List<NearbyDeviceInfoVo> devices, 
                                          List<CalcRouteVo> routes) {
    List<ClosestNode> closestNodes = new ArrayList<>();
    
    int minSize = Math.min(devices.size(), routes.size());
    
    for (int i = 0; i < minSize; i++) {
        CalcRouteVo routeVo = routes.get(i);
        NearbyDeviceInfoVo devInfo = devices.get(i);
        
        // 验证路由有效性
        if (!isValidRoute(routeVo)) {
            continue;
        }

        ClosestNode closestNode = createClosestNode(node, devInfo, sourceLngLat, routeVo);
        closestNodes.add(closestNode);
    }
    
    return closestNodes;
}

/**
 * 验证路由是否有效
 */
private boolean isValidRoute(CalcRouteVo routeVo) {
    return routeVo != null && 
           routeVo.getTotalLength() != null && 
           routeVo.getTotalLength() > 0.0;
}

/**
 * 创建最近节点对象
 */
private ClosestNode createClosestNode(Node node, NearbyDeviceInfoVo devInfo, 
                                    double[] sourceLngLat, CalcRouteVo routeVo) {
    ClosestNode closestNode = new ClosestNode(node, devInfo, sourceLngLat, devInfo.getLngLat());
    closestNode.setLength(routeVo.getTotalLength());
    closestNode.setRouteNodeList(routeVo.getNodeList());
    
    // 如果需要设置其他属性，可以在这里添加
    // closestNode.setFeederId(devInfo.getFeederId());
    // closestNode.setFeederName(devInfo.getFeederName());
    
    return closestNode;
}