# 📚 PLAN: WEB BÁN EBOOK PRODUCTION-LIKE (JAVA + AWS + PayPal/VietQR)

## 🎯 KIẾN TRÚC TỔNG THỂ

```
┌─────────────────────────────────────────────────────────────────┐
│                     USERS (Internet)                            │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                ┌──────────▼──────────┐
                │   CloudFront (CDN)  │  → Cache static files
                └──────────┬──────────┘
                           │
                ┌──────────▼──────────────────┐
                │  ALB (Application Load      │
                │   Balancer) - Port 80/443   │
                └──────────┬──────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼─────┐  ┌────────▼────────┐ ┌──────▼──────┐
   │  EC2-1   │  │   EC2-2        │ │  EC2-3      │
   │(Spring   │  │ (Spring Boot)  │ │(Spring Boot)│
   │  Boot)   │  │                │ │             │
   └────┬─────┘  └────────┬────────┘ └──────┬──────┘
        │                 │                 │
        └─────────────────┼─────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
    ┌───▼───┐         ┌───▼────┐       ┌───▼────┐
    │  RDS  │         │  S3    │       │ Redis  │
    │(MySQL)│         │(Ebook) │       │(Cache) │
    └───────┘         │(Signed │       └────────┘
                      │URLs)   │
                      └────────┘

Payment Gateway: PayPal / VietQR API
Email: SES (AWS) hoặc SMTP
```

## 💰 ƯỚC TÍNH CHI PHÍ AWS (THÁNG)

| Dịch vụ | Free Tier | Giới hạn | Chi phí vượt |
|---------|-----------|---------|--------------|
| **EC2 t3.micro** | ✅ 750h/tháng | 1 instance | $0.0116/h (khi vượt) |
| **RDS MySQL t3.micro** | ✅ 750h/tháng | db.t3.micro | $0.017/h (khi vượt) |
| **S3** | ✅ 5GB lưu trữ | Storage + requests | ~$0.02-0.05/tháng |
| **CloudFront** | ✅ 1TB bandwidth | 1TB download | $0.085/GB (khi vượt) |
| **ALB** | ❌ | - | ~$16-20/tháng |
| **Redis ElastiCache** | ❌ | - | ~$15-20/tháng (cache.t3.micro) |
| **SES** | ✅ 62k emails/tháng | Outbound email | $0.10/1k emails |
| **Total (Free Tier)** | | | **~$0-5/tháng** |
| **Nếu dùng đủ (ALB+Cache)** | | | **~$40-50/tháng** |

**💡 CHIẾN LƯỢC TIẾT KIỆM (CHẠY 24/7):**
- Dùng ALB bắt buộc cho production (không thể bỏ) = ~$17/tháng
- Database RDS: dùng t3.micro trong Free Tier (750h free)
- Redis: dùng local Docker trên EC2 thay vì ElastiCache (tiết kiệm ~$15-20)
- S3: dùng Signed URLs (user tải trực tiếp, ko tính bandwidth ALB)
- **Tổng cộng: ~$20-25/tháng** (OK với 2-3 đô/tuần)

### 📹 **CHI PHÍ CHO DEMO MODE (Chỉ quay video + test, sau đó TẮT HẾT)**

**Kịch bản: Bật AWS 3-5 giờ/ngày để test + quay video, sau đó TẮT hoàn toàn**

```
Chi phí/Giờ = ALB + EC2 + RDS
- ALB: $16.29/tháng ÷ 730h = $0.022/h
- EC2 t3.micro: $0.0116/h (khi vượt free tier)
- RDS t3.micro: $0.017/h (khi vượt free tier)
─────────────────────────────
Tổng/h (nếu chạy continuous): $0.0506/h ≈ $0.05/h

TÍNH CHI PHÍ DEMO (Ví dụ):
- Quay 1 video demo: 3 giờ = 3h × $0.05 = $0.15
- Test liên tục 1 tuần (4 giờ/ngày × 7 ngày) = 28h × $0.05 = $1.40
- Test 1 tháng (3 giờ/ngày × 25 ngày) = 75h × $0.05 = $3.75
```

**⚠️ LƯU Ý QUAN TRỌNG:**
1. **ALB ko thể PAUSE** - mỗi ngày mỗi giờ chạy = ~$0.65/ngày (fixed cost)
2. **EC2 & RDS CÓ THỂ STOP** khi không dùng (chi phí = 0)
3. **Free Tier 750h/tháng** ≈ 25h/ngày MIỄN PHÍ
   - Nếu bạn dùng ≤ 25h/ngày, RDS + EC2 = **FREE**
   - Chỉ trả ALB: ~$0.65-0.70/ngày

**💰 CHI PHÍ THỰC TẾ DEMO MODE:**
```
Kịch bản A: Quay 1-2 video (mỗi 3-5 giờ)
- Tổng: ~2-5h chạy ALB = $0.10-0.30
- EC2/RDS: FREE (trong free tier)
- Total: ~$0.30-0.50 😊

Kịch bản B: Test/demo liên tục 1 tuần
- ALB: 7 ngày × $0.65 = $4.55
- EC2/RDS: FREE (750h/tháng = 25h/ngày miễn phí)
- Total: ~$4.50 cho 1 tuần ✅

Kịch bản C: Test/demo liên tục 1 tháng (3 giờ/ngày)
- ALB: 30 ngày × $0.65 = $19.50
- EC2/RDS: FREE (750h/tháng = 31.25h/ngày miễn phí, bạn dùng 3h/ngày)
- Total: ~$19.50 cho cả tháng ✅
- S3/SES: ~$0.50-1
- Grand Total: **~$20/tháng** 🎉
```

**🎯 CÁC CÁCH TIẾT KIỆM DEMO:**

| Strategy | Cost | Details |
|----------|------|---------|
| **Quay video (lần 1)** | ~$0.50 | Bật ALB + EC2 3h, sau đó tắt |
| **Test 1 tuần** | ~$4.50 | Chạy 3-4h/ngày, tắt hết sau giờ |
| **Test demo sẵn sàng** | ~$20/tháng | Bật liên tục nhưng mức tối thiểu |
| **Production 24/7** | ~$25/tháng | Luôn bật, có traffic thực |
| **Development local** | ~$0 | Chạy local, ko tốn AWS |

**🚀 KHUYẾN CÁO THỰC TẾ:**

1️⃣ **Phase 1-3 (Dev + Build)**: Chạy LOCAL → $0/tháng
   
2️⃣ **Demo Day (3-5 giờ)**: Bật AWS vài tiếng
   - Bật EC2, RDS từ snapshot (nhanh)
   - Bật ALB
   - Quay video + test
   - TẮT ngay sau đó
   - Chi phí: **~$0.50-1**

3️⃣ **Nếu muốn "warm deployment"** (sẵn sàng mọi lúc):
   - Chạy EC2 + RDS continuous
   - Free tier cover hết (750h/tháng)
   - Chỉ trả ALB: ~$16-17/tháng
   - **Chi phí: ~$17/tháng** cho production-ready system

4️⃣ **Nếu muốn chạy real production** (24/7, có user):
   - Chi phí: ~$20-25/tháng (như tính trên)

**📝 BẢNG SO SÁNH:**

| Use Case | Duration | Cost |
|----------|----------|------|
| **Dev local + 1 video demo** | 6 tuần | ~$1 (chỉ ALB lúc quay) |
| **Demo + test 1 tuần** | 1 tuần | ~$5 |
| **Ready-to-demo system** | 1 tháng | ~$17 (ALB only) |
| **Full production 1 tháng** | 1 tháng | ~$25 |

**✨ KẾT LUẬN:**
- **Cho demo/test ngắn hạn**: Chỉ cần tốn ~$0.50-5 (bật khi cần, tắt sau)
- **Cho "demo-ready" system**: ~$17/tháng (chủ yếu ALB)
- **Cho production thực**: ~$25/tháng

---

## ⚙️ TECHNOLOGY STACK

### Backend
- **Spring Boot 3.x** với Spring Web, Data JPA, Security
- **Spring i18n (Internationalization)** hỗ trợ đa ngôn ngữ (VI + EN)
- **JWT Token** cho download links (thay vì session)
- **MySQL 8.x** (RDS t3.micro)
- **Redis** (local Docker trên EC2) cho cache + session store
- **Maven** build tool
- **Docker** containerization

### Frontend
- **Thymeleaf** template (server-side rendering) + i18n messages
- **Bootstrap 5** CSS framework (CDN - free)
- **Language Switcher** (VI/EN) với cookie/session persistence
- **jQuery/Fetch API** cho polling payment status
- **QR.js** library generate QR code client-side

### Payment Integration
- **PayPal SDK** (REST API)
- **VietQR** sinh QR động qua URL `https://img.vietqr.io/image/{bank}-{account}-compact2.jpg?amount={amount}&addInfo={info}&accountName={name}` (không cần API key)
- **Stripe** (optional, alternative)

### Infrastructure
- **AWS EC2** t3.micro (1 primary + 1 standby optional)
- **AWS RDS** MySQL t3.micro
- **AWS S3** cho ebook files (Signed URLs - user download trực tiếp)
- **AWS ALB** Application Load Balancer
- **AWS CloudFront** CDN (optional, cho static files)
- **AWS SES** Simple Email Service

### DevOps
- **GitHub Actions** CI/CD pipeline
- **Docker Hub** image registry (free)
- **Git** version control

---

# 🔄 PHASES IMPLEMENTATION

## PHASE 1: LOCAL DEVELOPMENT (1-2 tuần)

### 1.1 Chuẩn bị tài nguyên
```
[ ] Tạo repo GitHub: lelongc/ebook-store
[ ] Clone project về
[ ] Prepare files:
    - 3-5 PDF ebooks (công khai dùng demo)
    - images/ folder để ebook covers
```

### 1.2 Database Design (Local Docker)

**docker-compose.yml:**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: ebook_store
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  mysql_data:
  redis_data:
```

**Database Schema:**
```sql
-- Ebooks (Multi-language support)
CREATE TABLE ebooks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  
  -- Vietnamese content
  title_vi VARCHAR(255) NOT NULL,
  author_vi VARCHAR(255),
  description_vi TEXT,
  
  -- English content
  title_en VARCHAR(255) NOT NULL,
  author_en VARCHAR(255),
  description_en TEXT,
  
  -- Common fields
  price DECIMAL(10, 2) NOT NULL,
  cover_url VARCHAR(255),
  s3_key VARCHAR(255) NOT NULL,  -- S3 file path
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  
  INDEX idx_title_vi (title_vi),
  INDEX idx_title_en (title_en)
);

-- Users
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  ebook_id BIGINT NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  status VARCHAR(20) NOT NULL,  -- PENDING, PAID, FAILED, EXPIRED
  payment_method VARCHAR(20),  -- PAYPAL, VIETQR
  transaction_id VARCHAR(255),
  secret_code VARCHAR(32) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  paid_at TIMESTAMP,
  expires_at TIMESTAMP,  -- Download link expires after 24h
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (ebook_id) REFERENCES ebooks(id),
  KEY idx_status (status),
  KEY idx_secret (secret_code)
);

-- Download Tokens (for secure download)
CREATE TABLE download_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  token VARCHAR(64) NOT NULL UNIQUE,
  expires_at TIMESTAMP NOT NULL,
  used_count INT DEFAULT 0,
  max_uses INT DEFAULT 5,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(id),
  KEY idx_token (token),
  KEY idx_expires (expires_at)
);

-- Webhooks log (debug)
CREATE TABLE webhook_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  provider VARCHAR(50),  -- PAYPAL, VIETQR
  order_id BIGINT,
  payload JSON,
  status_code INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 1.3 Backend Development (Spring Boot)

**Chi tiết phân rã 1.3 (để tránh nhầm với 1.4 Frontend):**
- **1.3.1** Khởi tạo project (Spring Initializr, pom.xml, cấu hình `application.properties`)
- **1.3.2** Entities + Enums (JPA, audit, i18n fields)
- **1.3.3** Repositories (JpaRepository + custom finders)
- **1.3.4** Services (business logic, cache, S3/SES/PayPal/VietQR)
- **1.3.5** Controllers (REST + Thymeleaf endpoints)
- **1.3.6** DTO + Exception + Config (Security, Redis, I18n, Thymeleaf)

**Project Structure:**
```
src/main/java/com/ebook/
├── entity/
│   ├── Ebook.java
│   ├── Order.java
│   ├── User.java
│   └── DownloadToken.java
├── repository/
│   ├── EbookRepository.java
│   ├── OrderRepository.java
│   └── DownloadTokenRepository.java
├── service/
│   ├── EbookService.java
│   ├── OrderService.java
│   ├── PaymentService.java (abstract)
│   ├── PayPalService.java
│   ├── VietQRService.java
│   ├── S3Service.java
│   ├── EmailService.java
│   └── TokenService.java
├── controller/
│   ├── HomeController.java
│   ├── CartController.java
│   ├── PaymentController.java
│   ├── DownloadController.java
│   ├── LanguageController.java (NEW - language switcher)
│   └── WebhookController.java
├── dto/
│   ├── EbookDTO.java
│   ├── OrderDTO.java
│   ├── PaymentResponseDTO.java
│   └── WebhookPayloadDTO.java
├── config/
│   ├── SecurityConfig.java
│   ├── AwsConfig.java
│   ├── RedisConfig.java
│   ├── ThymeleafConfig.java
│   └── I18nConfig.java (NEW - internationalization)
└── exception/
    └── GlobalExceptionHandler.java

src/main/resources/
├── templates/           # Thymeleaf HTML
├── static/             # CSS, JS, images
├── i18n/               # NEW - Language files
│   ├── messages_vi.properties
│   └── messages_en.properties
└── application.properties
```

**Core Features:**

[ ] **Ebook Service**
  - GET /api/ebooks → List all with pagination
  - GET /api/ebooks/{id} → Detail
  - Auto-detect language (vi/en) từ request
  - Return title_vi/title_en based on locale
  - DTO cho FE + caching trong Redis 1h (per language)

[ ] **Order Service**
  - POST /api/orders/create
    - Input: ebook_id, email
    - Generate order_id (ORDER_TIMESTAMP_RANDOM)
    - Set status = PENDING
    - Set expires_at = now + 15 minutes
    - Return: order_id, payment_url (PayPal/VietQR)
  - GET /api/orders/{secret_code}/status
    - Polling endpoint từ FE gọi 3 giây/lần
    - Return: {status, message}

[ ] **Payment Integration**
  - PayPal:
    - Create order via API
    - Return approve link
    - IPN webhook xử lý thanh toán
    - Update order status = PAID
  - VietQR (QR động, không cần API key):
    - Tạo URL dạng: `https://img.vietqr.io/image/{bank}-{account}-compact2.jpg?amount={amount}&addInfo={info}&accountName={name}`
    - Ví dụ: `https://img.vietqr.io/image/vietinbank-113366668888-compact2.jpg?amount=1000&addInfo=dong%20qop%20quy%20vac%20xin&accountName=Quy%20Vac%20Xin%20Covid`
    - `amount`: số tiền order (VND, không dấu chấm)
    - `addInfo`: nên chứa `orderId` hoặc `secretCode` để đối soát
    - `accountName`: URL-encode tên chủ tài khoản
    - Khi nhận giao dịch (webhook/nhập tay), xác nhận `amount` + `addInfo` và cập nhật order = PAID

[ ] **Download Token Service**
  - POST /api/orders/{secret_code}/generate-token
    - Kiểm tra order status = PAID
    - Generate JWT token (exp 24h)
    - Store trong Redis
    - Return token

[ ] **Download Controller**
  - GET /download/{token}
    - Verify JWT + kiểm tra Redis
    - Generate S3 Signed URL (1h valid)
    - Redirect user to S3 (user tải trực tiếp)
    - Log download

[ ] **Email Service**
  - Use AWS SES
  - Template: "Cảm ơn đã mua ebook. Click link để nhận file"
  - Link: https://yourdomain.com/download/{token}

### 1.4 Frontend Development (Thymeleaf)

**Pages:**

[ ] **index.html** (Home - Catalog)
  ```
  - Header: 
    - Logo, Search, Cart
    - Language Switcher: 🇻🇳 Tiếng Việt | 🇬🇧 English
  - Filter: By category, price range (optional)
  - Ebook Grid: Cover image + title + author + price
    (Display title_vi if locale=vi, title_en if locale=en)
  - "Mua Ngay" / "Buy Now" button → Modal checkout or /checkout/{ebook_id}
  ```

[ ] **checkout.html** (Cart/Checkout)
  ```
  - Show ebook: cover, title, price
  - Email input field
  - Radio buttons: PayPal / VietQR
  - "Thanh toán" button
  - Action: POST /api/orders/create
  - Response: Redirect to /payment/{order_id}
  ```

[ ] **payment.html** (Payment Page)
  ```
  - Display QR Code (từ VietQR API hoặc PayPal button)
  - Polling: setInterval → GET /api/orders/{secret_code}/status
  - Show countdown: "15 phút hết hạn"
  - JavaScript:
    ```javascript
    setInterval(async () => {
      const res = await fetch(`/api/orders/${orderId}/status`);
      const data = await res.json();
      if (data.status === 'PAID') {
        // Show success
        window.location.href = '/success?order_id=' + orderId;
      }
    }, 3000);
    ```
  ```

[ ] **success.html** (Confirm)
  ```
  - "Thanh toán thành công!"
  - "Chúng tôi đã gửi link tải về email của bạn"
  - "Không nhận được? Click để gửi lại"
  - Show order details
  ```

[ ] **error.html** (Failed)
  ```
  - "Thanh toán thất bại / Hết hạn"
  - "Quay lại để thử lại"
  ```

### 1.5 AWS Preparation (Local Config)

[ ] Create AWS IAM User (NOT root):
  - Permissions:
    - AmazonS3FullAccess (cho dev)
    - AmazonRDSFullAccess
    - AmazonElastiCacheFullAccess
    - AmazonSESFullAccess
    - EC2FullAccess
    - ElasticLoadBalancingFullAccess
  - Save Access Key + Secret Key (safe place)

[ ] application.properties (local):
  ```properties
  # Internationalization (i18n)
  spring.messages.basename=i18n/messages
  spring.messages.encoding=UTF-8
  spring.messages.fallback-to-system-locale=false
  spring.web.locale=vi
  spring.web.locale-resolver=cookie
  
  # AWS
  aws.s3.bucket-name=my-ebook-store
  aws.s3.region=ap-southeast-1
  aws.s3.access-key=${AWS_ACCESS_KEY}
  aws.s3.secret-key=${AWS_SECRET_KEY}
  
  aws.ses.from-email=noreply@yourdomain.com
  
  # PayPal
  paypal.client-id=${PAYPAL_CLIENT_ID}
  paypal.client-secret=${PAYPAL_CLIENT_SECRET}
  paypal.mode=sandbox
  
  # VietQR
  vietqr.api-key=${VIETQR_API_KEY}
  
  # Database
  spring.datasource.url=jdbc:mysql://localhost:3306/ebook_store
  spring.datasource.username=root
  spring.datasource.password=root123
  
  # Redis
  spring.redis.host=localhost
  spring.redis.port=6379
  ```

### 1.6 Docker Local Testing

[ ] Build & Run:
  ```bash
  docker-compose up -d
  mvn clean package
  java -jar target/ebook-store.jar
  ```

[ ] Test Flow:
  - Mua ebook (create order)
  - Verify DB order created
  - Test webhook (curl -X POST http://localhost:8080/webhook ...)
  - Test download link
  - Kiểm tra email log

---

## PHASE 2: AWS INFRASTRUCTURE SETUP (1 tuần)

### 2.1 AWS S3 (Ebook Storage)

[ ] Create S3 Bucket:
  ```bash
  aws s3api create-bucket \
    --bucket ebook-store-prod \
    --region ap-southeast-1 \
    --create-bucket-configuration LocationConstraint=ap-southeast-1
  ```

[ ] Block Public Access:
  ```bash
  aws s3api put-public-access-block \
    --bucket ebook-store-prod \
    --public-access-block-configuration \
    "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"
  ```

[ ] Upload ebooks:
  ```bash
  aws s3 cp ebook1.pdf s3://ebook-store-prod/ebooks/
  aws s3 cp cover1.jpg s3://ebook-store-prod/covers/
  ```

[ ] Create IAM Role for EC2 (instead of Access Key):
  - Policy JSON:
  ```json
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Action": [
          "s3:GetObject",
          "s3:PutObject",
          "s3:ListBucket"
        ],
        "Resource": [
          "arn:aws:s3:::ebook-store-prod",
          "arn:aws:s3:::ebook-store-prod/*"
        ]
      }
    ]
  }
  ```
  - Attach to EC2 instance (later)

[ ] CloudFront Distribution (optional, for CDN):
  - Origin: S3 bucket
  - Behaviors: Cache static files 1 day
  - TTL: 86400 seconds
  - (Tiết kiệm: có thể bỏ, dùng ALB direct thôi)

### 2.2 AWS RDS (MySQL Database)

[ ] Create RDS Instance:
  ```bash
  aws rds create-db-instance \
    --db-instance-identifier ebook-store-db \
    --db-instance-class db.t3.micro \
    --engine mysql \
    --engine-version 8.0 \
    --master-username admin \
    --master-user-password $(aws secretsmanager get-random-password | jq -r '.RandomPassword') \
    --allocated-storage 20 \
    --storage-type gp2 \
    --backup-retention-period 7 \
    --multi-az=false \
    --publicly-accessible=false \
    --vpc-security-group-ids sg-xxx
  ```

[ ] Security Group:
  - Inbound: Port 3306 from EC2 security group only
  - Outbound: Allow all

[ ] Initial Database:
  ```bash
  # Sau khi RDS ready, SSH vào EC2 và:
  mysql -h ebook-store-db.xxx.ap-southeast-1.rds.amazonaws.com \
    -u admin -p < schema.sql
  ```

[ ] Store password in AWS Secrets Manager:
  ```bash
  aws secretsmanager create-secret \
    --name prod/ebook-store/db-password \
    --secret-string "your-password-here"
  ```

### 2.3 AWS ElastiCache (Redis) - OPTIONAL

**💡 Tiết kiệm: Dùng local Redis trên EC2 thay vì ElastiCache ($15-20/tháng)**

Nếu muốn redundancy:
```bash
aws elasticache create-cache-cluster \
  --cache-cluster-id ebook-store-cache \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1
```

### 2.4 AWS EC2 (Application Server)

[ ] Launch EC2 Instance:
  - Image: Ubuntu Server 22.04 LTS (free tier eligible)
  - Instance type: t3.micro
  - VPC: Default (or custom)
  - Subnet: Public (để ALB có thể gọi)
  - Security Group:
    - Inbound: 
      - Port 22 (SSH) from YOUR IP only
      - Port 8080 from ALB security group
    - Outbound: Allow all
  - Storage: 20GB gp2
  - Enable public IP: Yes
  - IAM Role: Attach S3 + SES + Secrets Manager role

[ ] Post-Launch Setup (SSH vào instance):
  ```bash
  # Update system
  sudo apt-get update && sudo apt-get upgrade -y
  
  # Install Docker
  curl -fsSL https://get.docker.com -o get-docker.sh
  sudo sh get-docker.sh
  sudo usermod -aG docker ubuntu
  
  # Install Docker Compose
  sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
  
  # Install Git
  sudo apt-get install -y git
  
  # Install Java (nếu chạy JAR direct)
  sudo apt-get install -y openjdk-17-jre
  
  # Clone repo
  git clone https://github.com/lelongc/ebook-store.git
  cd ebook-store
  ```

[ ] Docker Compose trên EC2:
  ```yaml
  version: '3.8'
  services:
    redis:
      image: redis:7-alpine
      ports:
        - "6379:6379"
      volumes:
        - redis_data:/data
      healthcheck:
        test: ["CMD", "redis-cli", "ping"]
        interval: 10s
        timeout: 3s
    
    app:
      build: .
      ports:
        - "8080:8080"
      environment:
        - SPRING_DATASOURCE_URL=jdbc:mysql://ebook-store-db.xxx.rds.amazonaws.com:3306/ebook_store
        - SPRING_DATASOURCE_USERNAME=admin
        - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
        - AWS_S3_BUCKET=ebook-store-prod
        - AWS_REGION=ap-southeast-1
        - PAYPAL_CLIENT_ID=${PAYPAL_CLIENT_ID}
        - PAYPAL_CLIENT_SECRET=${PAYPAL_CLIENT_SECRET}
      depends_on:
        redis:
          condition: service_healthy
      restart: always
      healthcheck:
        test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
        interval: 30s
        timeout: 10s
        retries: 3
  
  volumes:
    redis_data:
  ```

### 2.5 AWS ALB (Application Load Balancer)

[ ] Create Target Group:
  - Protocol: HTTP, Port 8080
  - Health Check:
    - Path: /health (app health endpoint)
    - Interval: 30s
    - Healthy threshold: 2
    - Unhealthy threshold: 3

[ ] Create ALB:
  - Scheme: Internet-facing
  - Listeners:
    - HTTP:80 → Target Group
    - HTTPS:443 → Target Group (setup after domain)
  - Security Groups:
    - Inbound: 80, 443 from 0.0.0.0/0
    - Outbound: All

[ ] Register Target:
  - EC2 instance port 8080

[ ] Get ALB DNS:
  - Example: `ebook-store-alb-123.ap-southeast-1.elb.amazonaws.com`

### 2.6 Setup SSL Certificate (AWS ACM)

[ ] Create Certificate:
  ```bash
  aws acm request-certificate \
    --domain-name yourdomain.com \
    --validation-method DNS \
    --region ap-southeast-1
  ```

[ ] Validate via DNS:
  - Copy CNAME record từ ACM console
  - Add vào domain provider (Namecheap)
  - Đợi validation

[ ] Attach to ALB HTTPS listener

---

## PHASE 3: CI/CD PIPELINE (1 tuần)

### 3.1 Dockerfile

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-Xmx300m", "-Xms128m", "-jar", "app.jar"]
```

### 3.2 GitHub Actions Workflow

[ ] Create `.github/workflows/deploy.yml`:

```yaml
name: Build & Deploy to AWS

on:
  push:
    branches: [main, dev]
  pull_request:
    branches: [main]

env:
  AWS_REGION: ap-southeast-1
  ECR_REGISTRY: docker.io
  IMAGE_NAME: lelongc/ebook-store

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Build with Maven
        run: mvn clean package -DskipTests
      
      - name: Build Docker image
        run: docker build -t ${{ env.ECR_REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }} .
      
      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      
      - name: Push to Docker Hub
        run: |
          docker push ${{ env.ECR_REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
          docker tag ${{ env.ECR_REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }} \
                     ${{ env.ECR_REGISTRY }}/${{ env.IMAGE_NAME }}:latest
          docker push ${{ env.ECR_REGISTRY }}/${{ env.IMAGE_NAME }}:latest
  
  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy to EC2
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ubuntu
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            cd /home/ubuntu/ebook-store
            git pull origin main
            
            # Load environment variables
            export DB_PASSWORD=$(aws secretsmanager get-secret-value \
              --secret-id prod/ebook-store/db-password \
              --region ${{ env.AWS_REGION }} \
              --query SecretString --output text)
            
            # Export for docker-compose
            export PAYPAL_CLIENT_ID=${{ secrets.PAYPAL_CLIENT_ID }}
            export PAYPAL_CLIENT_SECRET=${{ secrets.PAYPAL_CLIENT_SECRET }}
            export VIETQR_API_KEY=${{ secrets.VIETQR_API_KEY }}
            
            # Restart containers
            docker-compose down
            docker-compose pull
            docker-compose up -d
            
            # Health check
            sleep 10
            curl -f http://localhost:8080/health || exit 1
```

### 3.3 GitHub Secrets Setup

[ ] Add to GitHub repository settings:
  ```
  EC2_HOST: your-ec2-public-ip
  EC2_SSH_KEY: (paste entire .pem key content)
  DOCKER_USERNAME: your-docker-hub-username
  DOCKER_PASSWORD: your-docker-hub-token
  PAYPAL_CLIENT_ID: xxx
  PAYPAL_CLIENT_SECRET: xxx
  VIETQR_API_KEY: xxx
  ```

---

## PHASE 4: DOMAIN & DNS SETUP (1 ngày)

[ ] You already have domain - verify with provider

[ ] Update DNS Records:
  - Get ALB DNS name: `ebook-store-alb-123.ap-southeast-1.elb.amazonaws.com`
  - Create CNAME record:
    ```
    www CNAME ebook-store-alb-123.ap-southeast-1.elb.amazonaws.com
    @ CNAME ebook-store-alb-123.ap-southeast-1.elb.amazonaws.com (or A record pointing to ALB IP)
    ```
  - TTL: 300 (5 minutes for testing)
  - Wait for DNS propagation (5-10 minutes)

[ ] Configure Payment Webhooks:
  - PayPal: https://yourdomain.com/webhook/paypal
  - VietQR: https://yourdomain.com/webhook/vietqr

---

## PHASE 5: MONITORING & SCALING (Ongoing)

### 5.1 CloudWatch Monitoring

[ ] Setup Alarms:
  - EC2 CPU > 80% → Alert
  - RDS connections > 80% → Alert
  - ALB unhealthy targets → Alert
  - Application errors > 10/min → Alert

### 5.2 Auto Scaling (Optional)

[ ] Create Auto Scaling Group:
  - Min: 1, Desired: 2, Max: 3
  - Scaling policy: CPU average > 70% → +1 instance
  - (Chi phí: +$0.0116/h per extra instance)

### 5.3 Database Backup

[ ] RDS Automated Backups:
  - Retention: 7 days (included)
  - Manual snapshots: Weekly

### 5.4 Logging

[ ] CloudWatch Logs:
  - Collect app logs từ Docker
  - S3 access logs
  - RDS slow query logs

---

## 🎬 PHASE 6: FINAL TESTING & DEPLOYMENT

### 6.1 Pre-Production Testing

[ ] Load Testing:
  ```bash
  ab -n 1000 -c 10 https://yourdomain.com/
  ```

[ ] Payment Flow:
  - Sandbox PayPal transaction
  - Test VietQR webhook

[ ] Download Link:
  - Generate token
  - Verify expiry
  - Test concurrent downloads

[ ] Email Delivery:
  - Verify SES configuration
  - Check spam folder

### 6.2 Security Checklist

[ ] SSL/TLS: Green lock on browser
[ ] SQL Injection: Prepared statements (Spring JPA handles this)
[ ] CSRF: Spring Security CSRF token enabled
[ ] XSS: Thymeleaf auto-escapes by default
[ ] Rate Limiting: Add spring-boot-starter-ratelimiter (100 req/min)
[ ] API Keys: All in AWS Secrets Manager (not hardcoded)
[ ] Database: Backup enabled, encryption at rest
[ ] S3: Block public access, signed URLs only

### 6.3 Performance Tuning

[ ] Database:
  - Add indexes on `orders.status`, `orders.secret_code`
  - Connection pooling: HikariCP (min 5, max 20)

[ ] Redis:
  - Cache ebooks list (1h TTL)
  - Cache order status (5min TTL)

[ ] App:
  - Enable gzip compression
  - Static asset caching (1 year for versioned)
  - Lazy load images on homepage

---

## 💰 COST BREAKDOWN (Monthly Estimate)

| Service | Pricing | Calc | Notes |
|---------|---------|------|-------|
| **ALB** | $16.29/mo + $0.006/LCU | $16.29 | Fixed; ~5 LCU = $0.03 |
| **EC2 t3.micro** | $0 (Free 750h) + $0.0116/h | $0 (free tier) | ~730h/month covered |
| **RDS MySQL t3.micro** | $0 (Free 750h) + $0.017/h | $0 (free tier) | ~730h/month covered |
| **S3** | $0.023/GB (storage) + request fees | $0.05 | 5GB free + 10GB ebooks |
| **CloudFront** | $0.085/GB (outbound) | $0.10 | 1.2 GB/month typical |
| **SES** | $0 (62k free) + $0.10/1k | $0.50 | ~500 emails/month |
| **Data Transfer** | $0 out of region, inter-AWS free | $0 | EC2→S3→User efficient |
| **Redis (local Docker)** | $0 | $0 | Runs on EC2 |
| **CloudWatch** | $0 (free tier) | $0 | 10 alarms free |
| **Secrets Manager** | $0.40 per secret/month | $0.40 | 1 secret |
| **Total** | | **~$17.37/month** | **Within budget** ✅ |

---

## 📋 QUICK START CHECKLIST

### Week 1: Local Dev
- [ ] Setup Spring Boot project + Docker Compose
- [ ] Implement Order + Payment service
- [ ] Frontend with Thymeleaf
- [ ] Test locally with payment sandbox

### Week 2: AWS Infrastructure
- [ ] Create S3 bucket + upload ebooks
- [ ] Launch RDS MySQL + EC2
- [ ] Setup ALB + security groups
- [ ] Configure domain DNS

### Week 3: DevOps
- [ ] Dockerize application
- [ ] Setup GitHub Actions CI/CD
- [ ] Configure payment webhooks
- [ ] Run end-to-end testing

### Week 4: Production
- [ ] Performance tuning + monitoring
- [ ] Security hardening
- [ ] Monitor first transactions
- [ ] Plan maintenance & scaling

---

## 🆘 TROUBLESHOOTING COMMON ISSUES

| Issue | Solution |
|-------|----------|
| **Payment webhook not firing** | Check Security Group allows inbound from payment provider |
| **Download link expired** | Verify JWT token generation + Redis connection |
| **ALB shows unhealthy** | Check EC2 health endpoint `/health` returns 200 |
| **RDS connection timeout** | Verify EC2 security group is in RDS security group inbound |
| **S3 access denied** | Verify EC2 IAM role has S3 permissions |
| **Emails not sending** | Verify email is verified in SES, check sandbox mode |
| **Slow page load** | Enable Thymeleaf caching, add Redis for ebook list |

---

## 🚀 DEPLOYMENT DAY (Final Steps)

```bash
# On EC2
cd /home/ubuntu/ebook-store

# Verify environment
docker-compose logs app | tail -20

# Check database
mysql -h <RDS-ENDPOINT> -u admin -p ebook_store -e "SELECT * FROM ebooks;"

# Test payment webhook
curl -X POST https://yourdomain.com/webhook/paypal \
  -H "Content-Type: application/json" \
  -d '{"resource":{"status":"COMPLETED"}}'

# Monitor logs
docker-compose logs -f app
```

Done! 🎉 Your production-like ebook store is ready.
