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


-- ============================================
-- Resources 模块表结构与初始数据
-- ============================================

-- -------------------------------------------------------
-- 11. 资源类别表 tb_resource_category
-- 用途：存储出境资源分类，如签证办理、酒店住宿等
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_resource_category (
                                                    id          BIGSERIAL   PRIMARY KEY,
                                                    name        VARCHAR(50) NOT NULL,                            -- 类别名称，如"签证办理"
                                                    icon        VARCHAR(50) NOT NULL,                            -- 前端 Lucide 图标名称，如"ShieldAlert"
                                                    color       VARCHAR(20) NOT NULL,                            -- 类别主题色（十六进制），如"#3B82F6"
                                                    sort_order  INT         NOT NULL DEFAULT 0,                  -- 展示顺序，数值越小排越前
                                                    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,              -- 是否启用（软删除）
                                                    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_resource_category          IS '出境资源分类表，定义签证、酒店、交通等资源类别';
COMMENT ON COLUMN tb_resource_category.id         IS '类别主键ID';
COMMENT ON COLUMN tb_resource_category.name        IS '类别名称';
COMMENT ON COLUMN tb_resource_category.icon        IS '前端Lucide图标组件名';
COMMENT ON COLUMN tb_resource_category.color      IS '类别主题色（十六进制）';
COMMENT ON COLUMN tb_resource_category.sort_order  IS '展示顺序';
COMMENT ON COLUMN tb_resource_category.is_active  IS '是否启用（软删除）';
COMMENT ON COLUMN tb_resource_category.created_at  IS '创建时间';
COMMENT ON COLUMN tb_resource_category.updated_at  IS '更新时间';

-- 类别表索引：按展示顺序排序查询
CREATE INDEX idx_tb_resource_category_sort_order ON tb_resource_category (sort_order);

-- -------------------------------------------------------
-- 12. 资源表 tb_resource
-- 用途：存储各国各分类下的具体资源链接，如日本签证办理资源、美国酒店预订平台等
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_resource (
                                           id            BIGSERIAL   PRIMARY KEY,
                                           country       VARCHAR(50) NOT NULL,                            -- 国家/地区名称，如"日本"
                                           category_id   BIGINT      NOT NULL,                             -- 关联 tb_resource_category.id
                                           title         VARCHAR(200)NOT NULL,                             -- 资源标题，如"日本 e-Visa"
                                           description   VARCHAR(500)NOT NULL,                             -- 资源简短描述
                                           url           VARCHAR(500)NOT NULL,                             -- App 深链 URL，如"xcurrency://"
                                           web_url       VARCHAR(500),                                     -- 降级 Web URL，深链打不开时跳转
                                           image_url     VARCHAR(500),                                     -- 封面图 URL
                                           logo          VARCHAR(500),                                     -- App Logo 图标 URL（工具 App 展示用）
                                           is_featured   BOOLEAN     NOT NULL DEFAULT FALSE,              -- 是否精选（精选资源展示为大卡片）
                                           meta          JSONB       NOT NULL DEFAULT '{}',               -- 扩展元数据，可存储 highlights 等
                                           sort_order    INT         NOT NULL DEFAULT 0,                  -- 同类别内的展示顺序
                                           is_active     BOOLEAN     NOT NULL DEFAULT TRUE,               -- 是否启用（软删除）
                                           created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                           CONSTRAINT fk_resource_category FOREIGN KEY (category_id) REFERENCES tb_resource_category(id) ON DELETE RESTRICT
);

COMMENT ON TABLE tb_resource               IS '出境资源表，存储各国分类下的具体资源链接信息';
COMMENT ON COLUMN tb_resource.id           IS '资源主键ID';
COMMENT ON COLUMN tb_resource.country       IS '国家/地区名称';
COMMENT ON COLUMN tb_resource.category_id  IS '关联的资源类别ID';
COMMENT ON COLUMN tb_resource.title        IS '资源标题';
COMMENT ON COLUMN tb_resource.description  IS '资源简短描述';
COMMENT ON COLUMN tb_resource.url          IS 'App深链URL';
COMMENT ON COLUMN tb_resource.web_url      IS '降级Web URL（深链不可用时跳转）';
COMMENT ON COLUMN tb_resource.image_url    IS '封面图片URL';
COMMENT ON COLUMN tb_resource.is_featured  IS '是否精选（精选资源展示为大卡片）';
COMMENT ON COLUMN tb_resource.meta          IS '扩展元数据（JSON），可存highlights、cta等';
COMMENT ON COLUMN tb_resource.sort_order   IS '同类别内的展示顺序';
COMMENT ON COLUMN tb_resource.is_active     IS '是否启用（软删除）';
COMMENT ON COLUMN tb_resource.created_at    IS '创建时间';
COMMENT ON COLUMN tb_resource.updated_at    IS '更新时间';

-- 资源表索引优化
CREATE INDEX idx_tb_resource_country       ON tb_resource (country);                       -- 按国家查询
CREATE INDEX idx_tb_resource_category_id  ON tb_resource (category_id);                   -- 按类别查询
CREATE INDEX idx_tb_resource_country_category ON tb_resource (country, category_id);        -- 国家+类别联合查询（最常用查询路径）
CREATE INDEX idx_tb_resource_featured      ON tb_resource (is_featured) WHERE is_featured = TRUE; -- 精选资源快速筛选
CREATE INDEX idx_tb_resource_meta_gin      ON tb_resource USING GIN (meta);                 -- JSONB 元数字段全文检索
