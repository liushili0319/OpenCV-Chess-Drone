# Maven私服SNAPSHOT上传错误解决方案

## 错误信息
```
Version policy mismatch, cannot upload SNAPSHOT content to RELEASE repositories for file 'com/izhr/fruoyi-vue-zboot/2.0-SNAPSHOT/ruoyi-vue-zboot-2.0-SNAPSHOT.pom'
```

## 问题原因
您正在尝试将SNAPSHOT版本上传到只接受RELEASE版本的仓库中。

## 解决方案

### 方案1：修改为正式版本（推荐）
修改pom.xml中的版本号：
```xml
<version>2.0</version>  <!-- 移除-SNAPSHOT后缀 -->
```

### 方案2：配置正确的仓库
在pom.xml中配置distributionManagement：
```xml
<distributionManagement>
    <repository>
        <id>nexus-releases</id>
        <url>http://your-nexus-server/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <url>http://your-nexus-server/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

### 方案3：命令行指定仓库
```bash
# SNAPSHOT版本使用snapshot仓库
mvn deploy -DaltDeploymentRepository=nexus-snapshots::default::http://your-nexus-server/repository/maven-snapshots/

# RELEASE版本使用release仓库
mvn deploy -DaltDeploymentRepository=nexus-releases::default::http://your-nexus-server/repository/maven-releases/
```

### 方案4：配置settings.xml
在~/.m2/settings.xml中配置：
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

## 重要提示
- SNAPSHOT版本只能上传到snapshot仓库
- RELEASE版本只能上传到release仓库
- 检查Nexus仓库的Version Policy设置

## 推荐步骤
1. 确定要发布的版本类型
2. 修改pom.xml中的版本号
3. 配置正确的仓库地址
4. 执行 mvn clean deploy
