-- ===================================================
-- SEED DATA - Demo Ebook
-- ===================================================

-- Insert demo ebook (1 ebook duy nhất)
INSERT INTO ebooks (
  title_vi, 
  title_en, 
  author_vi, 
  author_en, 
  description_vi, 
  description_en, 
  price, 
  cover_url, 
  s3_key,
  file_size_mb,
  page_count,
  status
) VALUES (
  'Hướng Dẫn Bán Hàng Online Toàn Tập',
  'Complete Guide to Online Selling',
  'Tác Giả Demo',
  'Demo Author',
  'Cuốn sách hướng dẫn chi tiết cách bán hàng online, xây dựng cửa hàng, quản lý khách hàng và tăng doanh thu. Phù hợp cho người mới bắt đầu.',
  'Comprehensive guide on online selling, building your store, managing customers and increasing revenue. Perfect for beginners.',
  10000,  -- 10,000 VND
  'https://via.placeholder.com/300x400?text=Selling+Guide',
  'ebooks/selling-guide.pdf',
  2.5,
  150,
  'ACTIVE'
);

-- Verify insert
SELECT id, title_vi, price, status FROM ebooks WHERE status = 'ACTIVE';
