# 后端项目 - 终端启动说明

## 🚀 三种启动方式

### 方式1：双击启动脚本（最简单）⭐⭐⭐

**前提**：已安装JDK和Maven

1. 配置数据库密码（如果还没配置）
   - 打开：`src/main/resources/application.yml`
   - 修改第8行密码

2. 启动项目
   - 双击 `start.bat` 文件
   - 等待启动完成

**优点**：一键启动，最简单

---

### 方式2：使用Maven命令（推荐）⭐⭐

**前提**：已安装JDK和Maven

1. 打开PowerShell或CMD

2. 进入项目目录
   ```powershell
   cd G:\myproject\starwar\backend
   ```

3. 首次运行（下载依赖，只需一次）
   ```powershell
   mvn clean install
   ```

4. 启动项目
   ```powershell
   mvn spring-boot:run
   ```

**优点**：灵活，可以自定义参数

---

### 方式3：使用IDEA（适合开发）⭐⭐

**前提**：已安装IntelliJ IDEA

1. 打开IDEA
2. File -> Open -> 选择backend目录
3. 运行 StarWarApplication.java

**优点**：适合开发调试

---

## 📋 环境要求

### 必须安装

1. **JDK 8**
   - 下载：https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
   - 配置：JAVA_HOME环境变量
   - 验证：`java -version`

2. **Maven**
   - 下载：https://maven.apache.org/download.cgi
   - 配置：MAVEN_HOME环境变量
   - 验证：`mvn -version`

3. **MySQL 8.0**
   - 确保服务已启动
   - 执行数据库初始化脚本：`docs/database/init.sql`

---

## ✅ 验证启动成功

启动成功后您会看到：

```
Started StarWarApplication in X.XXX seconds (JVM running for X.XXX)
```

然后访问：
- Swagger文档：http://localhost:8080/swagger-ui.html
- 测试接口：http://localhost:8080/api/v1/game/leaderboard?page=1&size=20

---

## 📚 文档索引

- `终端快速开始.txt` - 快速参考
- `终端启动指南.md` - 详细教程
- `快速开始.txt` - 完整指南
- `开发指南.md` - 开发手册

---

## 🔧 常见问题

### Q1: mvn命令不识别？
**A**: 检查Maven是否安装，环境变量是否配置

### Q2: Java命令不识别？
**A**: 检查JDK是否安装，JAVA_HOME是否配置

### Q3: 端口被占用？
**A**: 修改application.yml中的port或关闭占用8080端口的程序

### Q4: 数据库连接失败？
**A**: 检查MySQL是否启动，密码是否正确

### Q5: 依赖下载慢？
**A**: 配置Maven阿里云镜像源

---

## 🎯 快速测试

启动成功后，在浏览器访问：

```
http://localhost:8080/api/v1/game/leaderboard?page=1&size=20
```

如果返回JSON数据，说明一切正常！

---

祝您使用愉快！🚀

