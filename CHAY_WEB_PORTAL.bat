@echo off
chcp 65001 > nul
title Khiem Thi Viet - Dev Super Admin Portal
echo =======================================================
echo  👑 KHIẾM THỊ VIỆT - DEV SUPER ADMIN PORTAL
echo =======================================================
echo.
echo Đang khởi chạy máy chủ Web cục bộ...
start "" http://localhost:3000
node server.js
pause
