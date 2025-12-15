# CentOS MySQL密码重置指南

## 🔍 问题描述

刚安装MySQL后，使用 `mysql -u root -p` 无法登录，提示 `ERROR 1045 (28000): Access denied`

## 🎯 解决方案

### 方法1：查找临时密码（MySQL 8.0+）

如果安装的是MySQL 8.0或更高版本，系统会生成临时密码，需要先查找：

```bash
# 查看MySQL日志，找到临时密码
sudo grep 'temporary password' /var/log/mysqld.log

# 或者查看所有相关日志
sudo grep -i password /var/log/mysqld.log | tail -5
```

找到临时密码后，使用它登录：

```bash
mysql -u root -p
# 输入临时密码（注意：临时密码可能有特殊字符，需要完整复制）
```

登录成功后，必须立即修改密码：

```sql
-- 设置新密码（MySQL 8.0要求密码强度）
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword123!';

-- 刷新权限
FLUSH PRIVILEGES;

-- 退出
exit;
```

**注意**：MySQL 8.0对密码强度有要求：
- 至少8个字符
- 包含大小写字母
- 包含数字
- 包含特殊字符

### 方法2：跳过密码验证重置（推荐，适用于所有情况）

如果找不到临时密码或临时密码不可用，可以使用跳过密码验证的方式：

#### 步骤1：停止MySQL服务

```bash
sudo systemctl stop mysqld
```

#### 步骤2：以安全模式启动MySQL

```bash
# 以跳过权限表的方式启动MySQL
sudo mysqld_safe --skip-grant-tables --skip-networking &
```

**注意**：如果命令不存在，尝试：

```bash
# 方式1：直接启动mysqld
sudo mysqld --skip-grant-tables --skip-networking &

# 方式2：修改配置文件（临时）
sudo systemctl stop mysqld
sudo mysqld --skip-grant-tables --user=mysql &
```

#### 步骤3：无密码登录MySQL

```bash
# 不需要密码直接登录
mysql -u root
```

#### 步骤4：重置密码

**对于MySQL 5.7及以下版本：**

```sql
USE mysql;
UPDATE user SET password=PASSWORD('YourNewPassword123!') WHERE user='root';
FLUSH PRIVILEGES;
exit;
```

**对于MySQL 8.0+：**

```sql
USE mysql;

-- 先清空root密码
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
FLUSH PRIVILEGES;

-- 退出MySQL
exit;
```

#### 步骤5：重启MySQL服务

```bash
# 杀死安全模式进程
sudo pkill mysqld

# 正常启动MySQL
sudo systemctl start mysqld

# 验证服务状态
sudo systemctl status mysqld
```

#### 步骤6：使用新密码登录（MySQL 8.0需要再次设置密码）

**MySQL 5.7：**
```bash
mysql -u root -p
# 输入新密码: YourNewPassword123!
```

**MySQL 8.0：**
```bash
mysql -u root -p
# 直接回车（密码为空）

# 登录后设置新密码
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword123!';
FLUSH PRIVILEGES;
exit;
```

### 方法3：使用mysql_secure_installation（推荐用于初始化）

如果MySQL服务已启动但无法登录，可以使用安全安装脚本：

```bash
sudo mysql_secure_installation
```

这个脚本会：
1. 询问是否设置密码验证插件
2. 设置root密码
3. 删除匿名用户
4. 禁止root远程登录
5. 删除test数据库
6. 刷新权限表

### 方法4：如果是MariaDB

CentOS 7默认可能安装的是MariaDB，处理方式略有不同：

```bash
# 停止服务
sudo systemctl stop mariadb

# 安全模式启动
sudo mysqld_safe --skip-grant-tables --skip-networking &

# 无密码登录
mysql -u root

# 重置密码（MariaDB）
USE mysql;
UPDATE user SET password=PASSWORD('YourNewPassword123!') WHERE user='root';
FLUSH PRIVILEGES;
exit;

# 重启服务
sudo pkill mysqld
sudo systemctl start mariadb
```

## 🔧 完整操作流程（推荐）

以下是完整的、最可靠的解决方案：

```bash
# 1. 停止MySQL服务
sudo systemctl stop mysqld

# 2. 以跳过权限表方式启动（在后台运行）
sudo mysqld_safe --skip-grant-tables --skip-networking &

# 等待几秒让MySQL启动
sleep 5

# 3. 无密码登录
mysql -u root

# 在MySQL中执行以下SQL（注意替换为您的密码）
```

MySQL命令行中：

```sql
USE mysql;

-- 对于MySQL 8.0+
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword123!';
FLUSH PRIVILEGES;

-- 或者对于MySQL 5.7
-- UPDATE user SET password=PASSWORD('YourNewPassword123!') WHERE user='root';
-- FLUSH PRIVILEGES;

exit;
```

然后：

```bash
# 4. 关闭跳过权限表的MySQL进程
sudo pkill mysqld
sudo pkill mysqld_safe

# 5. 正常启动MySQL
sudo systemctl start mysqld

# 6. 验证服务状态
sudo systemctl status mysqld

# 7. 使用新密码登录测试
mysql -u root -p
# 输入: YourNewPassword123!
```

## ✅ 验证登录成功

登录成功后，可以测试：

```sql
-- 显示所有数据库
SHOW DATABASES;

-- 显示当前用户
SELECT USER();

-- 退出
exit;
```

## 🎯 针对您的项目配置

重置密码后，需要：

1. **创建项目数据库**：

```bash
mysql -u root -p
```

```sql
CREATE DATABASE star_war_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
exit;
```

2. **导入数据库结构**：

```bash
mysql -u root -p star_war_db < /path/to/docs/database/init.sql
```

3. **更新后端配置**：

编辑 `/opt/starwar/config/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/star_war_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: YourNewPassword123!  # 使用您设置的新密码
```

## 🐛 常见问题

### 问题1：mysqld_safe命令不存在

**解决方案**：
```bash
# 查找MySQL安装路径
which mysql
# 通常mysqld_safe在MySQL的bin目录
sudo /usr/bin/mysqld_safe --skip-grant-tables --skip-networking &
```

### 问题2：端口已被占用

**解决方案**：
```bash
# 查找占用3306端口的进程
sudo netstat -tlnp | grep 3306
# 或
sudo lsof -i:3306

# 杀死进程
sudo kill -9 <PID>
```

### 问题3：MySQL服务无法启动

**解决方案**：
```bash
# 查看错误日志
sudo tail -f /var/log/mysqld.log

# 检查数据目录权限
sudo chown -R mysql:mysql /var/lib/mysql

# 初始化数据目录（如果数据目录为空）
sudo mysql_install_db --user=mysql --datadir=/var/lib/mysql
```

### 问题4：密码强度不够（MySQL 8.0）

**解决方案**：
```sql
-- 临时降低密码强度要求
SET GLOBAL validate_password.policy=LOW;
SET GLOBAL validate_password.length=8;

-- 然后设置密码
ALTER USER 'root'@'localhost' IDENTIFIED BY 'SimplePass123!';
```

## 📝 建议的密码策略

为了安全，建议：
- 密码长度至少12位
- 包含大小写字母、数字和特殊字符
- 不要使用常见密码
- 将密码保存在安全的地方

## ⚠️ 安全提醒

1. 重置密码后，确保MySQL配置文件权限正确：
```bash
sudo chmod 600 /etc/my.cnf
sudo chown root:root /etc/my.cnf
```

2. 建议创建项目专用的数据库用户，而不是使用root：
```sql
CREATE USER 'starwar'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON star_war_db.* TO 'starwar'@'localhost';
FLUSH PRIVILEGES;
```

3. 在部署指南中更新数据库用户名和密码配置。

---

如果以上方法都无法解决问题，请提供以下信息以便进一步排查：

1. MySQL版本：`mysql --version` 或 `rpm -qa | grep mysql`
2. 服务状态：`sudo systemctl status mysqld`
3. 错误日志：`sudo tail -20 /var/log/mysqld.log`

