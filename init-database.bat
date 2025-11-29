@echo off
REM 初始化 MySQL 数据库（默认账号 root/123123）
mysql -uroot -p123123 -e "CREATE DATABASE IF NOT EXISTS library_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
IF %ERRORLEVEL% NEQ 0 (
  echo 数据库初始化失败，请确认 MySQL 已启动且账户密码正确。
) ELSE (
  echo 数据库初始化完成/已存在：library_db
)
