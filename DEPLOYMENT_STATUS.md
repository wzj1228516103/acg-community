# 部署与 APK 状态

## 已完成

- `acg-frontend`: `npm run build` 成功，产物在 `acg-frontend/dist`。
- `acg-backend`: `mvn clean package -DskipTests` 成功，产物为 `acg-backend/target/acg-community-1.0.0.jar`。
- 公网站点 `http://39.105.128.249/` 可访问，入口文件与本地最新前端构建一致。
- 公网接口 `http://39.105.128.249/api/category/list` 返回正常数据。
- 已生成 Android WebView/Capacitor 调试 APK：[output/acg-community-debug.apk](output/acg-community-debug.apk)。

## 当前阻塞

本轮无法通过 SSH 更新服务器：服务器 `39.105.128.249:22` 可达，但现有密钥和仓库内旧密码均返回 `Permission denied`。因此没有覆盖线上文件，也没有重启线上服务。

重新发布需要有效的 SSH 私钥/密码，或在宝塔终端执行：

```bash
mkdir -p /opt/acg-community/acg-frontend/dist /opt/acg-community/acg-backend/uploads
systemctl stop acg-community
cp acg-community-1.0.0.jar /opt/acg-community/acg-backend/target/acg-community-1.0.0.jar
cp -r dist/* /opt/acg-community/acg-frontend/dist/
systemctl start acg-community
nginx -t && systemctl reload nginx
```

数据库、Redis、上传目录和 `CORS_ALLOWED_ORIGINS` 仍应使用服务器上的生产配置；不要把真实密码提交到仓库。
