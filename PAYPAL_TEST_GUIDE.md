# Hướng Dẫn Test PayPal Sandbox

## 1. Chuẩn Bị Tài Khoản PayPal Sandbox

### 1.1 Tạo/Truy Cập PayPal Developer

1. Vào https://developer.paypal.com
2. Login với tài khoản PayPal của bạn (nếu chưa có thì đăng ký tại paypal.com)
3. Nếu lần đầu, click **Sign Up** → chọn **Personal** hoặc **Business**

### 1.2 Lấy Sandbox Credentials

1. Vào **Dashboard** (menu trái)
2. Chọn **Sandbox** (phía trên)
3. Tìm **Apps & Credentials** (menu trái)
4. Chọn tab **Sandbox**
5. Tìm section **REST API apps**
6. Bạn sẽ thấy app đã tạo (hoặc tạo mới nếu cần)

### 1.3 Lấy Client ID và Secret

1. Click vào app name trong REST API apps
2. Bạn sẽ thấy:
   - **Client ID** (dài, bắt đầu bằng chữ/số)
   - **Secret** (dài, bắt đầu bằng chữ/số)
3. Copy cả hai

**Chú ý:** Trong file `.env.local` của project:
```
PAYPAL_CLIENT_ID=<paste client ID here>
PAYPAL_CLIENT_SECRET=<paste secret here>
```

Hiện tại đã được cấu hình với credentials test.

## 2. Lấy Tài Khoản Test (Buyer Account)

### 2.1 Tạo Buyer Account

1. Vẫn ở trang PayPal Developer
2. Chọn **Dashboard** → **Sandbox** → **Accounts**
3. Tìm account có suffix **-buyer** (ví dụ: `sb-xxxxx@personal.example.com`)
4. Nếu chưa có, click **Create Account** → chọn **Personal** → tạo

### 2.2 Thông Tin Account

- **Email:** `sb-xxxxx@personal.example.com`
- **Password:** Bạn tự đặt khi tạo, hoặc mật khẩu mặc định là `Aa123456789`

## 3. Test Luồng Thanh Toán PayPal

### 3.1 Truy Cập Ứng Dụng

1. Mở browser → vào URL Codespaces: `https://psychic-engine-pjrxgjjxxrp5f945w-8080.app.github.dev/`
2. Hoặc: `http://localhost:8080` (nếu chạy local)

### 3.2 Bước 1: Trang Chủ (Index)

1. Trang sẽ hiển thị:
   - **Tên Ebook:** "E-Book AWS" (hoặc tiếng Việt nếu click VI)
   - **Giá:** 10000 VND
   - **Input Email**
   - **Nút Thanh Toán** (Checkout / Thanh toán)

2. Nhập email test:
   ```
   test@example.com
   ```
   (có thể là bất kỳ email nào, không cần tài khoản thực)

3. Bấm **"Thanh toán"** (VI) hoặc **"Checkout"** (EN)

### 3.3 Bước 2: Trang Thanh Toán (Payment)

Trang sẽ hiển thị:
- **Ebook name:** E-Book AWS
- **Price:** 10000 VND
- **Order Code:** (mã đơn hàng gồm 64 ký tự)
- **Tab navigation:** VietQR | PayPal

#### 3.3.1 Test VietQR (tùy chọn)

1. Click tab **VietQR**
2. Sẽ thấy:
   - Mã QR code (hình ảnh QR)
   - Dòng text "Quét mã VietQR để thanh toán" (VI) / "Scan VietQR code to pay" (EN)
3. Đây là demo, không cần scan thực

#### 3.3.2 Test PayPal (Chính)

1. Click tab **PayPal**
2. Sẽ thấy **PayPal Checkout Button** (nút xanh lớn với logo PayPal)
3. Bấm nút này

### 3.4 Bước 3: PayPal Login Sandbox

Khi bấm nút PayPal:
1. Sẽ pop-up hoặc redirect sang trang PayPal Sandbox
2. Trang sẽ hiển thị form login
3. Đăng nhập bằng **Buyer Account**:
   - **Email:** `sb-xxxxx@personal.example.com`
   - **Password:** Mật khẩu bạn đặt

### 3.5 Bước 4: Xác Nhận Thanh Toán

Sau khi login:
1. PayPal sẽ hiển thị:
   - **Số tiền:** $0.40 (10000 VND ÷ 25000 = ~0.40 USD)
   - **Mô tả:** E-Book AWS
   - Nút **"Approve"** (xanh) và **"Cancel"** (đỏ)

2. Bấm **"Approve"** để xác nhận thanh toán

### 3.6 Bước 5: Quay Lại App (Success Page)

Sau khi approve:
1. Sẽ tự động quay lại ứng dụng
2. Trang sẽ hiển thị:
   - **Thông báo thành công:** "Thanh toán thành công!" (VI) / "Payment Successful!" (EN)
   - **Mã đơn hàng**
   - Text: "Chúng tôi sẽ gửi link tải về qua email của bạn"

3. Đó là dấu hiệu thanh toán thành công!

## 4. Kiểm Tra Lịch Sử Thanh Toán

### 4.1 Trên PayPal Developer

1. Vào https://developer.paypal.com
2. **Dashboard** → **Sandbox**
3. **Transactions** → sẽ thấy giao dịch vừa tạo
4. Status: **COMPLETED** (hoàn thành)

### 4.2 Trên Database (Tùy chọn)

Connect vào MySQL để xem:
```sql
SELECT * FROM orders WHERE status = 'PAID';
```

Sẽ thấy:
- `payment_method`: PAYPAL
- `transaction_id`: PayPal Order ID
- `paid_at`: Timestamp của giao dịch
- `status`: PAID

## 5. Troubleshooting

### 5.1 Vấn Đề: PayPal Button Không Hiển Thị

**Nguyên nhân:** 
- Client ID không chính xác
- Script PayPal không load

**Fix:**
1. Kiểm tra `.env.local` có đúng `PAYPAL_CLIENT_ID` không
2. Mở F12 → Console → xem có lỗi JavaScript không
3. Reload trang (Ctrl+F5)

### 5.2 Vấn Đề: Login PayPal Bị Lỗi

**Nguyên nhân:**
- Email/password sai
- Tài khoản bị lock

**Fix:**
1. Kiểm tra email là format `sb-xxxxx@personal.example.com`
2. Dùng mật khẩu đã set khi tạo account
3. Nếu quên, reset trên https://developer.paypal.com → Accounts

### 5.3 Vấn Đề: Approve Xong Không Quay Lại App

**Nguyên nhân:**
- Server lỗi khi capture order
- CORS issue

**Fix:**
1. Mở F12 → Network → xem request `/api/orders/{secretCode}/paypal/capture` có 200 không
2. Kiểm tra docker logs: `docker logs ebook-app`
3. Nếu error, cập nhật `PAYPAL_CLIENT_ID` hoặc `PAYPAL_CLIENT_SECRET`

### 5.4 Vấn Đề: Trang Payment Hiển Thị Lỗi

**Nguyên nhân:**
- Database lỗi
- API endpoint fail

**Fix:**
```bash
# Check server logs
docker logs ebook-app

# Restart app
docker restart ebook-app
```

## 6. Các Bước Đã Test Thành Công

- ✅ Tạo order (POST `/api/orders`)
- ✅ Nhận secret code
- ✅ Hiển thị trang payment
- ✅ PayPal button render (sử dụng Checkout SDK)
- ✅ Create PayPal order (POST `/api/orders/{secretCode}/paypal/create-order`)
- ✅ Redirect sang PayPal Sandbox
- ✅ Login với buyer account
- ✅ Approve payment
- ✅ Capture order (POST `/api/orders/{secretCode}/paypal/capture`)
- ✅ Mark order as PAID trong database
- ✅ Redirect về success page

## 7. Lưu Ý Quan Trọng

1. **Mỗi test một order mới:**
   - Mỗi lần nhập email → tạo order mới
   - Có thể dùng email test giống nhau

2. **VND to USD conversion:**
   - App tính: `VND / 25000 = USD`
   - 10000 VND ≈ $0.40

3. **Sandbox vs Production:**
   - Hiện tại: SANDBOX (test mode)
   - Không tính tiền thực
   - Chỉ test flow, không real money

4. **API Response:**
   - Success: `{"success": true, "message": "Payment successful"}`
   - Error: `{"success": false, "error": "Chi tiết lỗi"}`

## 8. Khi Ready Cho Production

1. Lấy **Live Credentials** từ PayPal (không phải Sandbox)
2. Cập nhật `.env.local`:
   ```
   PAYPAL_CLIENT_ID=<live client id>
   PAYPAL_CLIENT_SECRET=<live secret>
   paypal.mode=production
   ```
3. Rebuild Docker: `docker build -t ebook-store:latest .`
4. Restart: `docker restart ebook-app`
5. Test lại flow (sẽ redirect sang paypal.com chứ không phải sandbox)

---

**Support:**
- PayPal Docs: https://developer.paypal.com/docs/checkout/
- Project Issues: Commit changes → Test → Report bugs
