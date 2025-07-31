# Maven私服SNAPSHOT上传错误解决方案

## 错误信息
Version policy mismatch, cannot upload SNAPSHOT content to RELEASE repositories

## 问题原因
尝试将SNAPSHOT版本上传到只接受RELEASE版本的仓库中。

## 解决方案

### 方案1：修改为正式版本
修改pom.xml中的版本号，移除-SNAPSHOT后缀：
<version>2.0</version>

### 方案2：配置正确的仓库
在pom.xml中配置distributionManagement，确保SNAPSHOT版本使用snapshot仓库

### 方案3：命令行指定仓库
mvn deploy -DaltDeploymentRepository=nexus-snapshots::default::http://your-server/repository/maven-snapshots/

### 方案4：检查Nexus仓库配置
确保目标仓库的Version Policy设置正确

## 推荐步骤
1. 确定版本类型
2. 修改pom.xml版本号
3. 配置正确仓库地址
4. 执行mvn clean deploy
