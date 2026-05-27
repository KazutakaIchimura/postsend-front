-- 初期管理者スタッフ（パスワード: changeme）
-- BCrypt hash of "changeme"
INSERT INTO staffs (name, email, password_hash, role, is_active, force_password_change, created_at, updated_at)
SELECT '管理者', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE, TRUE, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM staffs WHERE email = 'admin@example.com'
);
