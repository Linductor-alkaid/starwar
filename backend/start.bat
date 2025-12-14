@echo off
chcp 65001 >nul
echo ====================================
echo 星际防卫战后端启动脚本
echo ====================================
echo.

REM 切换到脚本所在目录
cd /d %~dp0

echo [1/4] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java未安装或未配置环境变量！
    echo 请先安装JDK 8并配置JAVA_HOME环境变量
    echo.
    pause
    exit /b 1
)
echo ✅ Java环境检查通过
echo.

echo [2/4] 检查Maven环境...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo ❌ Maven未安装或未配置环境变量！
    echo.
    echo 解决方法：
    echo 1. 查看详细安装指南：安装Maven指南.md
    echo 2. 或者使用IDEA打开项目运行
    echo 3. 临时方案：配置IDEA自带的Maven
    echo.
    echo 按任意键查看详细说明...
    pause >nul
    start notepad "安装Maven指南.md"
    echo.
    echo 提示：安装好Maven后，请关闭所有命令行窗口重新打开
    echo.
    pause
    exit /b 1
)
echo ✅ Maven环境检查通过
echo.

echo [3/4] 检查配置文件...
if not exist "src\main\resources\application.yml" (
    echo ❌ 找不到配置文件 application.yml
    pause
    exit /b 1
)
echo ✅ 配置文件检查通过
echo.

echo [4/4] 启动项目...
echo.
echo ⚠️  提示：
echo    - 首次运行需要下载依赖，可能需要5-10分钟
echo    - 确保MySQL服务已启动
echo    - 确保端口8080未被占用
echo    - 按 Ctrl+C 可以停止服务
echo.
echo 正在启动，请稍候...
echo.

mvn spring-boot:run

echo.
echo 项目已停止运行
pause

