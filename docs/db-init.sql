-- ============================================
-- 用户认证模块数据库初始化脚本 (PostgreSQL)
-- ============================================

-- 1. 用户表 - 存储用户基本信息
CREATE TABLE IF NOT EXISTS "tb_user" (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) UNIQUE,          -- 用户名
    nickname        VARCHAR(100),               -- 昵称
    avatar          VARCHAR(500),               -- 头像URL
    gender          SMALLINT DEFAULT 0,        -- 性别: 0-未知, 1-男, 2-女
    birthday        DATE,                       -- 生日
    bio             VARCHAR(500),               -- 个人简介
    status          SMALLINT DEFAULT 1,         -- 状态: 0-禁用, 1-正常
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "tb_user" IS '用户表';
COMMENT ON COLUMN "tb_user".gender IS '性别: 0-未知, 1-男, 2-女';
COMMENT ON COLUMN "tb_user".status IS '状态: 0-禁用, 1-正常';

-- 2. 账号表 - 登录凭证（支持用户名、邮箱、手机号登录）
CREATE TABLE IF NOT EXISTS tb_user_account (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES "tb_user"(id) ON DELETE CASCADE,
    account_type    SMALLINT NOT NULL,          -- 账号类型: 1-用户名, 2-邮箱, 3-手机号
    account_value   VARCHAR(100) NOT NULL,      -- 账号值（用户名/邮箱/手机号）
    password        VARCHAR(255),               -- 密码（第三方登录时为空）
    salt            VARCHAR(50),                -- 盐值
    verified        BOOLEAN DEFAULT FALSE,      -- 是否验证
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(account_type, account_value)
);

CREATE INDEX IF NOT EXISTS idx_user_account_user_id ON tb_user_account(user_id);

COMMENT ON TABLE tb_user_account IS '账号表';
COMMENT ON COLUMN tb_user_account.account_type IS '账号类型: 1-用户名, 2-邮箱, 3-手机号';

-- 3. 第三方登录表 - 存储第三方登录信息
CREATE TABLE IF NOT EXISTS tb_user_social (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES "tb_user"(id) ON DELETE CASCADE,
    social_type     SMALLINT NOT NULL,          -- 平台类型: 1-微信, 2-QQ, 3-Google, 4-Apple, 5-抖音
    openid          VARCHAR(100) NOT NULL,      -- 第三方平台openid
    unionid         VARCHAR(100),               -- 微信/QQ unionid
    access_token    VARCHAR(500),               -- 访问令牌
    refresh_token   VARCHAR(500),               -- 刷新令牌
    expires_at      TIMESTAMP,                  -- 令牌过期时间
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(social_type, openid)
);

CREATE INDEX IF NOT EXISTS idx_user_social_user_id ON tb_user_social(user_id);

COMMENT ON TABLE tb_user_social IS '第三方登录表';
COMMENT ON COLUMN tb_user_social.social_type IS '平台类型: 1-微信, 2-QQ, 3-Google, 4-Apple, 5-抖音';

-- ============================================
-- 初始化测试数据（可选）
-- ============================================

-- 插入测试用户
INSERT INTO "tb_user" (username, nickname, gender, status)
VALUES ('admin', '管理员', 1, 1)
ON CONFLICT (username) DO NOTHING;

-- 获取测试用户ID并插入账号
-- INSERT INTO tb_user_account (user_id, account_type, account_value, password, salt, verified)
-- SELECT id, 1, 'admin', '$2a$10$xxxx', 'salt', true FROM "tb_user" WHERE username = 'admin';
