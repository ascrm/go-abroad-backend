-- ============================================
-- 初始数据插入脚本 (PostgreSQL)
-- ============================================

-- 1. 插入测试用户
INSERT INTO "tb_user" (username, nickname, avatar, gender, bio, status, created_at, updated_at)
VALUES 
    ('admin', '管理员', 'https://example.com/avatar/admin.png', 1, '系统管理员', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('testuser', '测试用户', 'https://example.com/avatar/test.png', 1, '这是一个测试用户', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('john_doe', '约翰·多伊', 'https://example.com/avatar/john.png', 1, 'Hello World!', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING
RETURNING id, username;

-- 2. 插入账号数据
-- 密码: 123456 (BCrypt加密，正确格式是60字符)
-- 使用一个预先计算好的 BCrypt 哈希（密码为 123456）
-- 实际项目中请使用 BCryptPasswordEncoder 生成

-- 先获取用户ID（如果已存在则跳过）
DO $$
DECLARE
    admin_id BIGINT;
    testuser_id BIGINT;
    john_doe_id BIGINT;
BEGIN
    -- 获取用户ID
    SELECT id INTO admin_id FROM "tb_user" WHERE username = 'admin' LIMIT 1;
    SELECT id INTO testuser_id FROM "tb_user" WHERE username = 'testuser' LIMIT 1;
    SELECT id INTO john_doe_id FROM "tb_user" WHERE username = 'john_doe' LIMIT 1;

    -- 插入 admin 的用户名账号
    IF admin_id IS NOT NULL THEN
        INSERT INTO tb_user_account (user_id, account_type, account_value, password, verified, created_at, updated_at)
        VALUES (admin_id, 1, 'admin', '$2a$10$EQDnc/H1RZwIGiLeP6p4qu.qpfV.Vn5L7d4I0V8z3xE5Z6Z5Z5Z5Z', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (account_type, account_value) DO NOTHING;
        
        -- admin 的邮箱
        INSERT INTO tb_user_account (user_id, account_type, account_value, password, verified, created_at, updated_at)
        VALUES (admin_id, 2, 'admin@example.com', '$2a$10$EQDnc/H1RZwIGiLeP6p4qu.qpfV.Vn5L7d4I0V8z3xE5Z6Z5Z5Z5Z', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (account_type, account_value) DO NOTHING;
        
        -- admin 的手机号
        INSERT INTO tb_user_account (user_id, account_type, account_value, password, verified, created_at, updated_at)
        VALUES (admin_id, 3, '13800138000', '$2a$10$EQDnc/H1RZwIGiLeP6p4qu.qpfV.Vn5L7d4I0V8z3xE5Z6Z5Z5Z5Z', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (account_type, account_value) DO NOTHING;
    END IF;

    -- 插入 testuser 的账号
    IF testuser_id IS NOT NULL THEN
        INSERT INTO tb_user_account (user_id, account_type, account_value, password, verified, created_at, updated_at)
        VALUES (testuser_id, 1, 'testuser', '$2a$10$EQDnc/H1RZwIGiLeP6p4qu.qpfV.Vn5L7d4I0V8z3xE5Z6Z5Z5Z5Z', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (account_type, account_value) DO NOTHING;
        
        INSERT INTO tb_user_account (user_id, account_type, account_value, password, verified, created_at, updated_at)
        VALUES (testuser_id, 2, 'test@example.com', '$2a$10$EQDnc/H1RZwIGiLeP6p4qu.qpfV.Vn5L7d4I0V8z3xE5Z6Z5Z5Z5Z', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (account_type, account_value) DO NOTHING;
        
        INSERT INTO tb_user_account (user_id, account_type, account_value, password, verified, created_at, updated_at)
        VALUES (testuser_id, 3, '13800138001', '$2a$10$EQDnc/H1RZwIGiLeP6p4qu.qpfV.Vn5L7d4I0V8z3xE5Z6Z5Z5Z5Z', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (account_type, account_value) DO NOTHING;
    END IF;

    -- 插入 john_doe 的账号
    IF john_doe_id IS NOT NULL THEN
        INSERT INTO tb_user_account (user_id, account_type, account_value, password, verified, created_at, updated_at)
        VALUES (john_doe_id, 1, 'john_doe', '$2a$10$EQDnc/H1RZwIGiLeP6p4qu.qpfV.Vn5L7d4I0V8z3xE5Z6Z5Z5Z5Z', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (account_type, account_value) DO NOTHING;
    END IF;
END $$;

-- 3. 插入第三方登录数据（示例）
INSERT INTO tb_user_social (user_id, social_type, openid, unionid, access_token, refresh_token, expires_at, created_at, updated_at)
SELECT id, 1, 'wechat_openid_12345', 'wechat_unionid_12345', 'access_token_xxx', 'refresh_token_xxx', CURRENT_TIMESTAMP + INTERVAL '30 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "tb_user" WHERE username = 'testuser'
ON CONFLICT (social_type, openid) DO NOTHING;

INSERT INTO tb_user_social (user_id, social_type, openid, unionid, access_token, refresh_token, expires_at, created_at, updated_at)
SELECT id, 2, 'qq_openid_67890', 'qq_unionid_67890', 'access_token_yyy', 'refresh_token_yyy', CURRENT_TIMESTAMP + INTERVAL '30 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "tb_user" WHERE username = 'john_doe'
ON CONFLICT (social_type, openid) DO NOTHING;

-- 验证数据
SELECT '用户表' as table_name, COUNT(*) as count FROM "tb_user"
UNION ALL
SELECT '账号表', COUNT(*) FROM tb_user_account
UNION ALL
SELECT '第三方登录表', COUNT(*) FROM tb_user_social;
