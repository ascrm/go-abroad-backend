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



--------------------- community部分的所有表 ----------------------------------


-- ============================================
-- 推荐文章表
-- ============================================
CREATE TABLE IF NOT EXISTS tb_articles (
                                        id BIGSERIAL PRIMARY KEY,
                                        title VARCHAR(200) NOT NULL,
                                        description TEXT,
                                        content TEXT NOT NULL,
                                        image VARCHAR(500),
                                        tag VARCHAR(50),
                                        author_id BIGINT REFERENCES tb_user(id),
                                        views INT DEFAULT 0,
                                        favorites INT DEFAULT 0,
                                        is_published BOOLEAN DEFAULT true,
                                        is_featured BOOLEAN DEFAULT false,
                                        published_at TIMESTAMP WITH TIME ZONE,
                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_articles_published ON tb_articles(is_published, published_at);
CREATE INDEX idx_articles_featured ON tb_articles(is_featured);

-- ============================================
-- 问答问题表
-- ============================================
CREATE TABLE IF NOT EXISTS tb_questions (
                                         id BIGSERIAL PRIMARY KEY,
                                         title VARCHAR(200) NOT NULL,
                                         content TEXT NOT NULL,
                                         author_id BIGINT REFERENCES tb_user(id),
                                         category VARCHAR(50),
                                         views INT DEFAULT 0,
                                         replies_count INT DEFAULT 0,
                                         is_resolved BOOLEAN DEFAULT false,
                                         is_deleted BOOLEAN DEFAULT false,
                                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_questions_author ON tb_questions(author_id);
CREATE INDEX idx_questions_category ON tb_questions(category);
CREATE INDEX idx_questions_resolved ON tb_questions(is_resolved);

-- ============================================
-- 问答回答表
-- ============================================
CREATE TABLE IF NOT EXISTS tb_answers (
                                       id BIGSERIAL PRIMARY KEY,
                                       question_id BIGINT NOT NULL REFERENCES tb_questions(id) ON DELETE CASCADE,
                                       author_id BIGINT REFERENCES tb_user(id),
                                       content TEXT NOT NULL,
                                       likes INT DEFAULT 0,
                                       replies_count INT DEFAULT 0,
                                       is_official BOOLEAN DEFAULT false,
                                       is_best_answer BOOLEAN DEFAULT false,
                                       is_deleted BOOLEAN DEFAULT false,
                                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_answers_question ON tb_answers(question_id);
CREATE INDEX idx_answers_author ON tb_answers(author_id);
CREATE INDEX idx_answers_official ON tb_answers(is_official);

-- ============================================
-- 互动表（合并收藏、点赞、关注）
-- ============================================
CREATE TABLE IF NOT EXISTS tb_interactions (
                                            id BIGSERIAL PRIMARY KEY,
                                            user_id BIGINT NOT NULL REFERENCES tb_user(id) ON DELETE CASCADE,
                                            target_id BIGINT NOT NULL,
                                            target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('article', 'question', 'answer')),
                                            action VARCHAR(20) NOT NULL CHECK (action IN ('favorite', 'like', 'follow', 'view')),
                                            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                            UNIQUE(user_id, target_id, target_type, action)
);

CREATE INDEX idx_interactions_user ON tb_interactions(user_id, action);
CREATE INDEX idx_interactions_target ON tb_interactions(target_id, target_type);



-- ================== plan模块内容 =============================

-- ============================================
-- 用户规划表
-- ============================================
CREATE TABLE IF NOT EXISTS tb_plans (
                                        id BIGSERIAL PRIMARY KEY,
                                        user_id BIGINT NOT NULL REFERENCES tb_user(id) ON DELETE CASCADE,
                                        title VARCHAR(200) NOT NULL,
                                        type VARCHAR(20) NOT NULL CHECK (type IN ('tourism', 'study', 'work', 'immigration')),
                                        destination JSONB,
                                        status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'generating', 'completed', 'archived')),
                                        form_data JSONB,
                                        cover_image VARCHAR(500),
                                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tb_plans_user ON tb_plans(user_id);
CREATE INDEX idx_tb_plans_type ON tb_plans(type);
CREATE INDEX idx_tb_plans_status ON tb_plans(status);

-- ============================================
-- 规划阶段表
-- ============================================
CREATE TABLE IF NOT EXISTS tb_plan_phases (
                                              id BIGSERIAL PRIMARY KEY,
                                              plan_id BIGINT NOT NULL REFERENCES tb_plans(id) ON DELETE CASCADE,
                                              title VARCHAR(100) NOT NULL,
                                              description TEXT,
                                              sort_order INT DEFAULT 0,
                                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tb_plan_phases_plan ON tb_plan_phases(plan_id);

-- ============================================
-- 规划任务表
-- ============================================
CREATE TABLE IF NOT EXISTS tb_plan_tasks (
                                             id BIGSERIAL PRIMARY KEY,
                                             phase_id BIGINT NOT NULL REFERENCES tb_plan_phases(id) ON DELETE CASCADE,
                                             title VARCHAR(200) NOT NULL,
                                             description TEXT,
                                             ai_suggestion TEXT,
                                             quick_links JSONB,
                                             is_completed BOOLEAN DEFAULT false,
                                             completed_at TIMESTAMP WITH TIME ZONE,
                                             sort_order INT DEFAULT 0,
                                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tb_plan_tasks_phase ON tb_plan_tasks(phase_id);