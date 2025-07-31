# Maven私服SNAPSHOT版本上传错误解决方案

## 问题描述
错误信息：`Version policy mismatch, cannot upload SNAPSHOT content to RELEASE repositories for file 'com/izhr/fruoyi-vue-zboot/2.0-SNAPSHOT/ruoyi-vue-zboot-2.0-SNAPSHOT.pom'`

这个错误表明您正在尝试将SNAPSHOT版本的依赖上传到只接受RELEASE版本的Maven仓库中。

## 解决方案

### 方案1：修改pom.xml版本号（推荐用于正式发布）

如果您要发布正式版本，请修改 `pom.xml` 中的版本号：

```xml
<groupId>com.izhr</groupId>
<artifactId>fruoyi-vue-zboot</artifactId>
<version>2.0</version>  <!-- 移除 -SNAPSHOT 后缀 -->
```

### 方案2：配置Maven settings.xml使用SNAPSHOT仓库

在 `~/.m2/settings.xml` 中配置正确的仓库：

```xml
<settings>
    <servers>
        <server>
            <id>nexus-releases</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
        <server>
            <id>nexus-snapshots</id>
            <username>your-username</username>
            <password>your-password</password>
        </server>
    </servers>
</settings>
```

### 方案3：修改pom.xml中的distributionManagement配置

在项目的 `pom.xml` 中正确配置仓库：

```xml
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
```

### 方案4：使用Maven deploy命令时指定正确的仓库

```bash
# 对于SNAPSHOT版本，使用snapshot仓库
mvn deploy -DaltDeploymentRepository=nexus-snapshots::default::http://your-nexus-server/repository/maven-snapshots/

# 对于RELEASE版本，使用release仓库
mvn deploy -DaltDeploymentRepository=nexus-releases::default::http://your-nexus-server/repository/maven-releases/
```

### 方案5：检查Nexus仓库配置

在Nexus管理界面中：

1. 登录Nexus管理控制台
2. 检查目标仓库的Version Policy设置：
   - Release仓库应设置为 `Release`
   - Snapshot仓库应设置为 `Snapshot` 或 `Mixed`

## 常用命令

```bash
# 清理并重新构建
mvn clean compile

# 部署到私服
mvn clean deploy

# 跳过测试部署
mvn clean deploy -DskipTests

# 查看有效的pom配置
mvn help:effective-pom
```

## 注意事项

1. **版本命名规范**：
   - SNAPSHOT版本：`2.0-SNAPSHOT`
   - RELEASE版本：`2.0`

2. **仓库选择**：
   - SNAPSHOT版本必须上传到snapshot仓库
   - RELEASE版本必须上传到release仓库

3. **权限检查**：
   - 确保用户有对应仓库的上传权限
   - 检查用户名和密码是否正确

## 推荐解决步骤

1. 首先确定您要发布的版本类型（SNAPSHOT还是RELEASE）
2. 如果是RELEASE版本，修改pom.xml移除-SNAPSHOT后缀
3. 如果是SNAPSHOT版本，确保使用snapshot仓库
4. 检查settings.xml中的服务器配置
5. 重新执行mvn deploy命令