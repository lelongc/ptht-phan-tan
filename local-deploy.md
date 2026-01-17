# Hướng dẫn test local (dev/test lại nhiều lần)

## 1. Chuẩn bị

- Đảm bảo có file `.env.local` (hoặc `.env`) với đủ biến môi trường:`SPRING_DATASOURCE_URL`, `SPRING_REDIS_HOST`, `SPRING_REDIS_PORT`, `PAYPAL_CLIENT_ID`, `PAYPAL_CLIENT_SECRET`, `PAYPAL_MODE`, `APP_JWT_SECRET`, `VIETQR_*`, v.v.
- Network docker compose thường là `ptht-phan-tan_ebook-network` (xem bằng `docker network ls`).
- DB/Redis sẽ tự seed dữ liệu mẫu khi volume mới.

## 2. Chạy app lần đầu hoặc test lại

```bash
# Bật MySQL + Redis (tự seed DB nếu volume mới)
docker compose up -d mysql redis

# Xóa container app cũ nếu có (không lỗi cũng không sao)
docker rm -f ebook-app

# Chạy app (dùng env-file, gắn đúng network)
docker run --rm -p 8080:8080 --name ebook-app \
  --network ptht-phan-tan_ebook-network \
  --env-file .env.local \
  shima594/ebook:latest
```

- Truy cập: http://localhost:8080
- Xem log app:
  ```bash
  docker logs -f ebook-app
  ```

## 3. Reset sạch DB/Redis để test lại từ đầu

```bash
# Xóa toàn bộ dữ liệu DB + Redis (cẩn thận, sẽ mất hết data)
docker compose down -v

# Bật lại DB/Redis (sẽ tự tạo schema + seed)
docker compose up -d mysql redis
```

- Sau đó chạy lại app như bước 2.

## 4. Một số thao tác nhanh

- **Xóa cache Redis (không xóa DB):**
  ```bash
  docker exec ebook-redis redis-cli FLUSHALL
  ```
- **Rebuild image local (nếu sửa code):**
  ```bash
  docker build -t shima594/ebook:latest . && docker push shima594/ebook:latest.
  ```
- **Kiểm tra network docker:**
  ```bash
  docker network ls
  ```
- **Kiểm tra volume dữ liệu:**
  ```bash
  docker volume ls
  ```

## 5. Lưu ý

- Nếu network tên khác, thay `ptht-phan-tan_ebook-network` bằng tên bạn thấy từ `docker network ls`.
- DB sẽ seed lại từ file `database/schema.sql` và `database/seed.sql` mỗi khi volume mới.
- Có thể sửa giá ebook, tên, mô tả... trong file seed hoặc messages để test nhiều trường hợp.
- Nếu lỗi port 8080, đổi sang port khác bằng `-p 8888:8080` (và truy cập http://localhost:8888).

---

**Tóm tắt:**Chỉ cần 3 lệnh:

1. `docker compose up -d mysql redis`
2. `docker rm -f ebook-app`
3. `docker run ...` như trên
   Muốn reset sạch: `docker compose down -v` rồi làm lại từ đầu.
