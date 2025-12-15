# MySQL密码重置备用方案

如果 `mysqld_safe` 命令不可用，尝试以下替代方案：

## 替代方案1：查找临时密码（MySQL 8.0）

```bash
# 查看MySQL错误日志，寻找临时密码
sudo grep 'temporary password' /var/log/mysqld.log

# 或者查看所有日志
sudo cat /var/log/mysqld.log | grep password
```

如果找到临时密码，使用它登录并修改：

```bash
mysql -u root -p
# 输入临时密码
```

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword123!';
FLUSH PRIVILEGES;
exit;
```

## 替代方案2：使用配置文件方式

```bash
# 1. 停止MySQL
sudo systemctl stop mysqld

# 2. 编辑MySQL配置文件
sudo vi /etc/my.cnf
# 或
sudo vi /etc/mysql/my.cnf
```

在 `[mysqld]` 部分添加：

```ini
[mysqld]
skip-grant-tables
skip-networking
```

保存并退出（vi中：按ESC，输入 `:wq`，回车）

```bash
# 3. 启动MySQL
sudo systemctl start mysqld

# 4. 无密码登录
mysql -u root

# 5. 在MySQL中重置密码
```

```sql
USE mysql;
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword123!';
FLUSH PRIVILEGES;
exit;
```

```bash
# 6. 移除配置文件中的跳过权限设置
sudo vi /etc/my.cnf
# 删除 skip-grant-tables 和 skip-networking 这两行

# 7. 重启MySQL
sudo systemctl restart mysqld

# 8. 测试登录
mysql -u root -p
```

## 替代方案3：使用mysql_secure_installation

如果MySQL服务可以启动但密码未知：

```bash
# 尝试直接登录（无密码）
mysql -u root

# 如果无法登录，使用安全安装脚本
sudo mysql_secure_installation
```

脚本会引导您：
1. 设置root密码
2. 删除匿名用户
3. 禁止root远程登录
4. 删除test数据库

## 检查MySQL版本

先确认您安装的是哪个版本：

```bash
# 检查MySQL版本
mysql --version

# 或者
rpm -qa | grep -i mysql

# 或者
rpm -qa | grep -i mariadb
```

根据版本选择对应的重置方法。

