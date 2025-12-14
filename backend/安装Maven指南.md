# Maven安装指南（Windows）

## 🚨 问题说明

如果您看到 "Maven未安装或未配置环境变量" 的错误，请按照本指南安装Maven。

## 📥 方法1：下载安装（推荐）

### 步骤1：下载Maven

1. 访问Maven官网：https://maven.apache.org/download.cgi

2. 下载**Binary zip archive**版本
   - 文件名例如：`apache-maven-3.9.5-bin.zip`

### 步骤2：解压到本地

1. 解压下载的zip文件
2. 建议解压到：`C:\apache-maven-3.9.5`
   - 注意：路径不要包含中文或空格

### 步骤3：配置环境变量

#### Windows 10/11 方法：

1. **打开系统属性**
   - 右键"此电脑" -> "属性"
   - 或按 `Win + Pause` 键

2. **进入环境变量**
   - 点击"高级系统设置"
   - 点击"环境变量"按钮

3. **新建MAVEN_HOME变量**
   - 在"系统变量"区域点击"新建"
   - 变量名：`MAVEN_HOME`
   - 变量值：`C:\apache-maven-3.9.5`（你解压的路径）
   - 点击"确定"

4. **编辑Path变量**
   - 在"系统变量"中找到`Path`变量
   - 点击"编辑"
   - 点击"新建"
   - 添加：`%MAVEN_HOME%\bin`
   - 点击"确定"保存所有窗口

5. **重启PowerShell**
   - 关闭所有打开的PowerShell窗口
   - 重新打开一个新的PowerShell窗口

### 步骤4：验证安装

在新的PowerShell窗口中输入：

```powershell
mvn -version
```

如果看到类似以下的输出，说明安装成功：

```
Apache Maven 3.9.5
Maven home: C:\apache-maven-3.5.3
Java version: 1.8.0_xxx
```

## 🚀 方法2：使用Chocolatey（快速）

如果您已安装Chocolatey包管理器：

```powershell
# 以管理员身份运行PowerShell
choco install maven
```

## 📝 快速配置脚本

您也可以创建一个批处理脚本来自动配置（需要管理员权限）：

**创建文件：`setup_maven.bat`**

```batch
@echo off
echo ====================================
echo Maven环境配置脚本
echo ====================================
echo.

set MAVEN_HOME=C:\apache-maven-3.9.5
set MAVEN_PATH=%MAVEN_HOME%\bin

echo 检查Maven路径是否存在...
if not exist "%MAVEN_HOME%" (
    echo ❌ 找不到Maven目录: %MAVEN_HOME%
    echo 请先解压Maven到该目录
    pause
    exit /b 1
)

echo ✅ Maven目录存在
echo.

echo 配置环境变量...
setx MAVEN_HOME "%MAVEN_HOME%" /M >nul 2>&1
if errorlevel 1 (
    echo ❌ 需要管理员权限才能配置环境变量
    echo 请以管理员身份运行此脚本
    pause
    exit /b 1
)

echo ✅ MAVEN_HOME已配置
echo.

echo 配置Path变量...
for /f "tokens=2*" %%a in ('reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v PATH') do set CURRENT_PATH=%%b
echo %CURRENT_PATH% | findstr /C:"%MAVEN_PATH%" >nul
if errorlevel 1 (
    setx PATH "%CURRENT_PATH%;%MAVEN_PATH%" /M >nul 2>&1
    echo ✅ Path已更新
) else (
    echo ✅ Path已包含Maven
)

echo.
echo ====================================
echo 配置完成！
echo ====================================
echo.
echo ⚠️  重要提示：
echo    - 请关闭所有PowerShell窗口
echo    - 重新打开新的PowerShell窗口
echo    - 运行命令: mvn -version
echo.

pause
```

**使用方法**：
1. 编辑脚本中的`MAVEN_HOME`路径为你的Maven路径
2. 以管理员身份运行此脚本
3. 重启PowerShell

## 🎯 安装完成后

安装成功后：

1. **重启PowerShell**（重要！）

2. **验证安装**
   ```powershell
   mvn -version
   ```

3. **配置国内镜像（可选，加速下载）**
   
   编辑文件：`%MAVEN_HOME%\conf\settings.xml`
   
   在`<settings>`标签内添加：
   ```xml
   <mirrors>
     <mirror>
       <id>aliyun</id>
       <mirrorOf>central</mirrorOf>
       <name>阿里云Maven镜像</name>
       <url>https://maven.aliyun.com/repository/public</url>
     </mirror>
   </mirrors>
   ```

4. **运行后端项目**
   ```powershell
   cd G:\myproject\starwar\backend
   start.bat
   ```

## 🔧 常见问题

### Q1: 安装后还是找不到mvn命令？

**解决**：
1. 确认环境变量配置正确
2. 关闭所有PowerShell窗口
3. 重新打开新的PowerShell窗口
4. 验证：`mvn -version`

### Q2: 需要管理员权限吗？

**A**: 配置系统环境变量需要管理员权限，但如果只配置用户环境变量则不需要。

### Q3: 下载Maven很慢？

**A**: 可以使用国内镜像下载：
- 阿里云镜像：https://mirrors.aliyun.com/apache/maven/
- 清华大学镜像：https://mirrors.tuna.tsinghua.edu.cn/apache/maven/

### Q4: 一定要安装Maven吗？

**A**: 如果使用IDEA，IDEA自带Maven可以不用单独安装。但使用命令行方式必须安装。

## 📚 参考资料

- Maven官方文档：https://maven.apache.org/guides/getting-started/
- Maven下载页面：https://maven.apache.org/download.cgi

---

安装完成后就可以运行后端项目了！🚀

