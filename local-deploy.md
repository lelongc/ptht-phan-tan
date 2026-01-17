Bạn đã có image `shima594/ebook:latest`, chỉ cần bật MySQL/Redis (từ docker-compose) rồi chạy container app gắn cùng network.

1) Bật DB + Redis:
- Trong thư mục dự án: `docker compose up -d mysql redis`

2) Chạy app dùng image đã pull:
- Lấy đúng tên network do compose tạo (thường là `ptht-phan-tan_ebook-network`, kiểm tra bằng `docker network ls`).
- Chạy:
```
docker build -t shima594/ebook:latest . && docker push shima594/ebook:latest
docker compose down -v
docker compose up -d mysql redis
sleep 25
docker rm -f ebook-app
docker run --rm -p 8080:8080 --name ebook-app --network ptht-phan-tan_ebook-network \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://ebook-mysql:3306/ebook_store?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8&useUnicode=true" \
  -e SPRING_DATASOURCE_USERNAME=ebook_user \
  -e SPRING_DATASOURCE_PASSWORD=ebook_pass \
  -e SPRING_REDIS_HOST=ebook-redis \
  -e PAYPAL_CLIENT_ID=AX7LSpgFzXNVoLu_XfijxVW-NMjY9YPqRKUFfyV1egZc0yqOgtHKT1ruHVyqtp6Idv7B9VYvMPx4gYlL \
  -e PAYPAL_CLIENT_SECRET=EAXWpSwQ_uGy8tt_8rZ4keEM-BUSqIDrSS7OS1QrkzX3CiElE9IhLQbjMlSUHFVI3UfQBZi_yj2aK4yA \
  shima594/ebook:latest
```

3) Truy cập: http://localhost:8080

Nếu network name khác, thay `ptht-phan-tan_ebook-network` bằng tên bạn thấy từ `docker network ls`. Volumes `mysql_data`/`redis_data` giữ dữ liệu; muốn reset DB: `docker compose down -v` rồi up lại.