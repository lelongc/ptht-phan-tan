# Hướng dẫn test local & quản lý Docker

## 1. Chuẩn bị

- File `.env.local` (hoặc `.env`) đủ biến: `SPRING_DATASOURCE_URL`, `SPRING_REDIS_HOST`, `PAYPAL_CLIENT_ID`, `APP_JWT_SECRET`, `VIETQR_*`, v.v.
- Network thường là `ptht-phan-tan_ebook-network` (kiểm tra bằng `docker network ls`).
- DB/Redis tự seed khi volume mới.

## 2. Chạy app & test nhanh

```bash
docker compose up -d                # build & chạy tất cả (app, mysql, redis)
docker compose logs -f app          # xem log app
```
- Truy cập: http://localhost:8080

## 3. Reset sạch dữ liệu để test lại

```bash
docker compose down -v              # xóa toàn bộ container + volume (DB/Redis sẽ seed lại)
docker compose up -d                # chạy lại tất cả
```

## 4. Rebuild app khi sửa code

```bash
docker compose up -d --build app    # build lại image app & restart
```

## 5. Push image app lên Docker Hub

```bash
docker compose build app            # build image tên chuẩn
docker compose push app             # push lên Docker Hub
# hoặc:
docker push shima594/ebook:latest
```

## 6. Một số lệnh dọn dẹp Docker (giải phóng máy)

```bash
docker system prune -af             # xóa toàn bộ container, image, network, cache không dùng
docker volume prune -f              # xóa toàn bộ volume không dùng
docker network prune -f             # xóa toàn bộ network không dùng
```
> **Cẩn thận:** Các lệnh này sẽ xóa sạch mọi thứ không chạy trên Docker, kể cả dữ liệu DB/Redis cũ!

## 7. Thao tác nhanh khác

- Xóa cache Redis: `docker exec ebook-redis redis-cli FLUSHALL`
- Kiểm tra network: `docker network ls`
- Kiểm tra volume: `docker volume ls`

---

**Tóm tắt:**  
- Chạy app: `docker compose up -d`
- Reset sạch: `docker compose down -v && docker compose up -d`
- Rebuild app: `docker compose up -d --build app`
- Push image: `docker compose push app`
- Dọn Docker: `docker system prune -af`
