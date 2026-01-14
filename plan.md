GIAI ĐOẠN 1: CODE DƯỚI LOCAL (MÁY CÁ NHÂN)
Mục tiêu: App chạy được trên máy tính của bạn trước đã.

1. Chuẩn bị tài nguyên

[ ] Đăng ký tài khoản SePay hoặc Casso (Gói Free). Liên kết với ngân hàng cá nhân.

[ ] Lấy một file PDF bất kỳ làm Ebook (đặt tên ebook-demo.pdf).

[ ] Lấy App Password của Gmail để dùng gửi mail.

2. Database & Cache (Docker Local)

[ ] Viết file docker-compose.yml local chạy MySQL và Redis.

[ ] Chạy docker-compose up để bật DB lên.

3. Code Backend (Spring Boot)

[ ] Tạo Entity: Order (id, email, amount, status, secret_code).

[ ] API Tạo đơn: Lưu đơn hàng -> Trả về mã đơn (ví dụ ORDER_123).

[ ] API Webhook: Tạo endpoint POST /api/webhook để SePay gọi vào -> Check tiền -> Update Status = PAID.

[ ] Service Gửi Mail: Khi PAID -> Gửi mail chứa link: http://server-ip/download/{token}.

[ ] Service Tải file: Check Token trong Redis -> Nếu còn hạn -> Stream file từ S3 (hoặc file local giả lập trước) trả về user.

4. Code Frontend (Thymeleaf)

[ ] Trang Home: Form nhập Email + Nút "Mua Ngay".

[ ] Trang Thanh toán:

Hiển thị ảnh QR (Dùng API VietQR: img.vietqr.io...).

Dùng Javascript setInterval gọi API 3 giây/lần check xem đơn đã PAID chưa.

[ ] Trang Thành công: Thông báo "Đã gửi mail".

GIAI ĐOẠN 2: THIẾT LẬP HẠ TẦNG AWS (INFRASTRUCTURE)
Mục tiêu: Có server và chỗ chứa file trên mây.

1. AWS S3 (Lưu Ebook)

[ ] Tạo Bucket my-ebook-store (Block Public Access).

[ ] Upload file ebook-demo.pdf lên đó.

[ ] Tạo IAM User (hoặc Role) có quyền AmazonS3ReadOnlyAccess. Lấy AccessKey và SecretKey.

2. AWS EC2 (Máy chủ)

[ ] Thuê EC2 t3.micro (Ubuntu).

[ ] Mở Security Group (Firewall): Port 22, 8080 (cho App), 80 (cho Demo).

[ ] SSH vào EC2, cài: Docker, Docker Compose, Git.

GIAI ĐOẠN 3: ĐÓNG GÓI & TỰ ĐỘNG HÓA (DEVOPS)
Mục tiêu: Đẩy code lên GitHub là Server tự cập nhật.

1. Dockerize

[ ] Viết Dockerfile cho Java App (Dùng openjdk:17-alpine, giới hạn RAM -Xmx300m).

[ ] Đăng ký tài khoản Docker Hub.

2. GitHub Actions (CI/CD)

[ ] Tạo file .github/workflows/deploy.yml.

[ ] Config các bước:

Checkout code.

Build Java (Maven).

Build Docker Image -> Push lên Docker Hub.

SSH vào EC2 -> Pull Image mới -> Restart container.

[ ] Add Secrets vào GitHub (IP Server, Key SSH, Docker User/Pass...).

3. Test CI/CD

[ ] Push code lên GitHub.

[ ] Vào tab Actions xem nó chạy xanh lè là thành công.

[ ] Kiểm tra trên EC2 xem container đã chạy chưa.

GIAI ĐOẠN 4: LOAD BALANCER & TÊN MIỀN (DEMO READY)
Mục tiêu: Làm cho giống Production (Chỉ làm trước khi Demo).

1. Mua tên miền (Chi phí ~$2)

[ ] Mua tên miền rẻ (ví dụ .xyz) trên Namecheap/GoDaddy.

[ ] Chưa cần trỏ IP vội, chờ bước ALB.

2. Cấu hình ALB (AWS Application Load Balancer)

[ ] Vào EC2 -> Target Groups: Tạo group trỏ vào port 8080 của con EC2.

[ ] Vào Load Balancers: Tạo ALB (Internet-facing).

[ ] Listener: Port 80 trỏ về Target Group vừa tạo.

[ ] Lấy cái DNS Name của ALB (ví dụ my-alb-123.aws.com).

3. Kết nối Tên miền

[ ] Vào trang quản lý tên miền -> Tạo bản ghi CNAME: @ trỏ về DNS Name của ALB.

[ ] Đợi 5 phút. Truy cập vào tên miền xem Web có hiện ra không.

4. Cấu hình SePay/Casso (Webhook)

[ ] Vào trang quản trị SePay.

[ ] Cập nhật URL Webhook: http://<ten-mien-cua-ban>/api/webhook.

GIAI ĐOẠN 5: BẢO VỆ ĐỒ ÁN (DEMO SCRIPT)
Kịch bản để lấy điểm A.

1. Setup trước giờ G

[ ] Bật ALB lên (nếu đã tắt để tiết kiệm tiền).

[ ] Kiểm tra lại Webhook SePay.

[ ] Xóa hết đơn hàng cũ trong Database cho sạch.

2. Quy trình Demo (Show cái gì?)

Show GitHub: "Em vừa push code, CI/CD tự chạy deploy xong".

Show Web: Vào mua hàng -> Quét QR (dùng tiền thật 1.000đ - 2.000đ demo thôi).

Show Automation: Web tự reload báo thành công (nhờ Webhook).

Show Email: Mở mail -> Bấm link tải -> Tải file về (File này stream từ S3).

Show Architecture: Mở AWS Console -> Chỉ vào cái biểu tượng Load Balancer và sơ đồ Target Group -> "Hệ thống em dùng ALB để cân bằng tải và bảo mật EC2".

3. Sau khi Demo xong

[ ] XÓA NGAY ALB (Để không bị trừ tiền oan).

[ ] Tắt EC2 (nếu không dùng nữa).