-- ===================================================
-- SEED DATA - Demo Ebook
-- ===================================================

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
  'ebook.title',
  'ebook.title',
  'ebook.author',
  'ebook.author',
  'ebook.description',
  'ebook.description',
  1,  -- Placeholder, giá thực lấy từ messages
  '/images/covers/amazon-web-services-aws-920x613.jpg',
  'ebooks/demo.pdf',
  2.5,
  150,
  'ACTIVE'
);

SELECT id, title_vi, price, status FROM ebooks WHERE status = 'ACTIVE';
