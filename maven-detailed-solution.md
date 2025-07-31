# Maven私服SNAPSHOT版本上传错误详细解决方案

## 🚨 错误分析

您遇到的错误信息：
```
Version policy mismatch, cannot upload SNAPSHOT content to RELEASE repositories for file 'com/izhr/fruoyi-vue-zboot/2.0-SNAPSHOT/ruoyi-vue-zboot-2.0-SNAPSHOT.pom'
```

**核心问题**：正在尝试将SNAPSHOT版本（开发版本）上传到只接受RELEASE版本（正式版本）的Maven仓库中。

## 🔧 解决方案

### ✅ 方案1：修改为正式版本（最推荐）

如果您要发布正式版本，修改项目的 `pom.xml` 文件：

```xml
<groupId>com.izhr</groupId>
<artifactId>ruoyi-vue-zboot</artifactId>
<!-- 原版本：2.0-SNAPSHOT -->
<!-- 修改为：-->
<version>2.0</version>
```

执行部署：
```bash
mvn clean deploy
```

### ✅ 方案2：配置正确的仓库分布

在 `pom.xml` 中添加 `distributionManagement` 配置：

```xml
<distributionManagement>
    <!-- 正式版本仓库 -->
    <repository>
        <id>nexus-releases</id>
        <name>Release Repository</name>
        <url>http://your-nexus-server/repository/maven-releases/</url>
    </repository>
    
    <!-- 快照版本仓库 -->
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <name>Snapshot Repository</name>
        <url>http://your-nexus-server/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

### ✅ 方案3：配置Maven认证

在用户目录 `~/.m2/settings.xml` 中配置：

```xml
<settings>
    <servers>
        <server>
            <id>nexus-releases</id>
            <username>你的用户名</username>
            <password>你的密码</password>
        </server>
        <server>
            <id>nexus-snapshots</id>
            <username>你的用户名</username>
            <password>你的密码</password>
        </server>
    </servers>
</settings>
```

### ✅ 方案4：命令行指定仓库

```bash
# 部署SNAPSHOT版本到快照仓库
mvn deploy -DaltDeploymentRepository=nexus-snapshots::default::http://your-nexus-server/repository/maven-snapshots/

# 部署RELEASE版本到正式仓库
mvn deploy -DaltDeploymentRepository=nexus-releases::default::http://your-nexus-server/repository/maven-releases/
```

### ✅ 方案5：检查Nexus仓库策略

1. 登录Nexus管理界面
2. 进入 Repositories 管理页面
3. 检查目标仓库的 Version Policy：
   - Release仓库：设置为 `Release`
   - Snapshot仓库：设置为 `Snapshot` 或 `Mixed`

## 📋 常用Maven命令

```bash
# 清理项目
mvn clean

# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目
mvn package

# 部署到私服
mvn deploy

# 跳过测试部署
mvn clean deploy -DskipTests

# 查看有效配置
mvn help:effective-pom

# 查看依赖树
mvn dependency:tree

# 调试模式（查看详细日志）
mvn deploy -X
```

## 🏷️ 版本管理规范

### SNAPSHOT版本（开发版本）
- **格式**：`x.y.z-SNAPSHOT`
- **特点**：可重复上传，自动覆盖
- **用途**：开发和测试阶段
- **示例**：`2.0-SNAPSHOT`、`1.5-SNAPSHOT`

### RELEASE版本（正式版本）
- **格式**：`x.y.z`
- **特点**：一旦发布不可修改，唯一性
- **用途**：生产环境部署
- **示例**：`2.0`、`1.5`

## 📝 完整配置示例

### pom.xml 完整配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.izhr</groupId>
    <artifactId>ruoyi-vue-zboot</artifactId>
    <version>2.0</version>
    <packaging>jar</packaging>
    
    <distributionManagement>
        <repository>
            <id>nexus-releases</id>
            <name>Nexus Release Repository</name>
            <url>http://your-nexus-server/repository/maven-releases/</url>
        </repository>
        <snapshotRepository>
            <id>nexus-snapshots</id>
            <name>Nexus Snapshot Repository</name>
            <url>http://your-nexus-server/repository/maven-snapshots/</url>
        </snapshotRepository>
    </distributionManagement>
    
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
```

## 🔍 故障排查指南

### 1. 网络连接检查
```bash
ping your-nexus-server
curl -I http://your-nexus-server
```

### 2. 用户权限验证
- 确认用户名密码正确
- 验证用户对目标仓库有upload权限
- 检查用户组权限设置

### 3. 查看详细错误日志
```bash
mvn deploy -X -e
```

### 4. 检查仓库状态
- 登录Nexus确认仓库在线
- 检查仓库是否为只读模式
- 验证仓库配置正确

### 5. 验证Maven配置
```bash
mvn help:effective-settings
mvn help:effective-pom
```

## 🎯 推荐解决步骤

1. **⚡ 快速解决**（推荐）
   - 修改 `pom.xml` 版本号，移除 `-SNAPSHOT`
   - 执行 `mvn clean deploy`

2. **🔧 完整配置**
   - 配置 `distributionManagement`
   - 设置 `settings.xml` 认证信息
   - 验证Nexus仓库策略

3. **✅ 验证部署**
   - 登录Nexus查看上传结果
   - 确认版本号正确
   - 测试依赖下载

## 🆘 常见问题与解决

### Q: 仍然报错"401 Unauthorized"
**A**: 检查用户名密码，确认用户有上传权限

### Q: 报错"409 Conflict"
**A**: RELEASE版本不能重复上传，需要升级版本号

### Q: 网络超时
**A**: 检查网络连接，配置Maven代理设置

### Q: 找不到仓库
**A**: 确认仓库URL正确，检查Nexus服务状态

## 📞 技术支持

如果按照以上方案仍然无法解决问题，建议：

1. 提供完整的错误日志：`mvn deploy -X`
2. 检查Nexus服务器端日志
3. 联系系统管理员确认仓库配置
4. 验证网络防火墙和代理设置

---
**最后更新**: 2024年1月
**适用版本**: Maven 3.x, Nexus 3.x