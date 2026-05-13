-- =============================================
-- 1. 用户表
-- =============================================
create table tb_user
(
    id            bigserial primary key,
    username      varchar(50) unique,
    nickname      varchar(100),
    avatar        varchar(500),
    gender        integer   default 0,
    birthday      date,
    bio           varchar(500),
    status        integer   default 1,
    created_at    timestamp default CURRENT_TIMESTAMP,
    updated_at    timestamp default CURRENT_TIMESTAMP,
    search_vector tsvector
);

comment on table tb_user is '用户表';
comment on column tb_user.gender is '性别: 0-未知, 1-男, 2-女';
comment on column tb_user.status is '状态: 0-禁用, 1-正常';

create index idx_user_search on tb_user using gin (search_vector);


-- =============================================
-- 2. 账号表
-- =============================================
create table tb_user_account
(
    id            bigserial primary key,
    user_id       bigint       not null references tb_user on delete cascade,
    account_type  integer      not null,
    account_value varchar(100) not null,
    password      varchar(255),
    salt          varchar(50),
    verified      boolean      default false,
    created_at    timestamp    default CURRENT_TIMESTAMP,
    updated_at    timestamp    default CURRENT_TIMESTAMP,
    unique (account_type, account_value)
);

comment on table tb_user_account is '账号表';
comment on column tb_user_account.account_type is '账号类型: 1-用户名, 2-邮箱, 3-手机号';

create index idx_user_account_user_id on tb_user_account (user_id);


-- =============================================
-- 3. 第三方登录表
-- =============================================
create table tb_user_social
(
    id            bigserial primary key,
    user_id       bigint       not null references tb_user on delete cascade,
    social_type   integer      not null,
    openid        varchar(100) not null,
    unionid       varchar(100),
    access_token  varchar(500),
    refresh_token varchar(500),
    expires_at    timestamp,
    created_at    timestamp default CURRENT_TIMESTAMP,
    updated_at    timestamp default CURRENT_TIMESTAMP,
    unique (social_type, openid)
);

comment on table tb_user_social is '第三方登录表';
comment on column tb_user_social.social_type is '平台类型: 1-微信, 2-QQ, 3-Google, 4-Apple, 5-抖音';

create index idx_user_social_user_id on tb_user_social (user_id);


-- =============================================
-- 4. 文章表
-- =============================================
create table tb_articles
(
    id            bigserial primary key,
    title         varchar(200) not null,
    description   text,
    content       text         not null,
    image         varchar(500),
    tag           varchar(50),
    author_id     bigint references tb_user,
    views         integer                  default 0,
    favorites     integer                  default 0,
    is_published  boolean                  default true,
    is_featured   boolean                  default false,
    published_at  timestamp with time zone,
    created_at    timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at    timestamp with time zone default CURRENT_TIMESTAMP,
    search_vector tsvector
);

create index idx_articles_published on tb_articles (is_published, published_at);
create index idx_articles_featured on tb_articles (is_featured);


-- =============================================
-- 5. 问题表
-- =============================================
create table tb_questions
(
    id            bigserial primary key,
    title         varchar(200) not null,
    author_id     bigint references tb_user,
    category      varchar(50),
    views         integer                  default 0,
    replies_count integer                  default 0,
    is_resolved   boolean                  default false,
    is_deleted    boolean                  default false,
    created_at    timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at    timestamp with time zone default CURRENT_TIMESTAMP,
    favorites     integer                  default 0,
    search_vector tsvector
);

create index idx_questions_author on tb_questions (author_id);
create index idx_questions_category on tb_questions (category);
create index idx_questions_resolved on tb_questions (is_resolved);
create index idx_question_search on tb_questions using gin (search_vector);


-- =============================================
-- 6. 回答表
-- =============================================
create table tb_answers
(
    id             bigserial primary key,
    question_id    bigint not null references tb_questions on delete cascade,
    author_id      bigint references tb_user,
    content        text   not null,
    likes          integer                  default 0,
    replies_count  integer                  default 0,
    is_official    boolean                  default false,
    is_best_answer boolean                  default false,
    is_deleted     boolean                  default false,
    created_at     timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at     timestamp with time zone default CURRENT_TIMESTAMP,
    views          integer                  default 0,
    favorites      integer                  default 0
);

create index idx_answers_question on tb_answers (question_id);
create index idx_answers_author on tb_answers (author_id);
create index idx_answers_official on tb_answers (is_official);


-- =============================================
-- 7. 互动表
-- =============================================
create table tb_interactions
(
    id          bigserial primary key,
    user_id     bigint      not null references tb_user on delete cascade,
    target_id   bigint      not null,
    target_type varchar(20) not null,
    action      varchar(20) not null,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP,
    unique (user_id, target_id, target_type, action),
    check (target_type in ('article', 'question', 'answer', 'user', 'comment')),
    check (action in ('favorite', 'like', 'follow', 'view'))
);

create index idx_interactions_user on tb_interactions (user_id, action);
create index idx_interactions_target on tb_interactions (target_id, target_type);


-- =============================================
-- 8. 计划表
-- =============================================
create table tb_plans
(
    id            bigserial primary key,
    user_id       bigint       not null references tb_user on delete cascade,
    title         varchar(200) not null,
    type          varchar(20)  not null,
    destination   jsonb,
    status        varchar(20)  default 'generating',
    form_data     jsonb,
    cover_image   varchar(500),
    created_at    timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at    timestamp with time zone default CURRENT_TIMESTAMP,
    is_deleted    boolean                  default false,
    description   text,
    start_date    date,
    end_date      date,
    plan_date     date,
    resource      jsonb                    default '[]'::jsonb,
    search_vector tsvector,
    check (type in ('tourism', 'study', 'work', 'immigration')),
    check (status in ('draft', 'generating', 'paused', 'completed'))
);

create index idx_tb_plans_user on tb_plans (user_id);
create index idx_tb_plans_type on tb_plans (type);
create index idx_tb_plans_status on tb_plans (status);
create index idx_plan_search on tb_plans using gin (search_vector);


-- =============================================
-- 9. 计划阶段表
-- =============================================
create table tb_plan_phases
(
    id            bigserial primary key,
    plan_id       bigint       not null references tb_plans on delete cascade,
    title         varchar(100) not null,
    description   text,
    sort_order    integer                  default 0,
    created_at    timestamp with time zone default CURRENT_TIMESTAMP,
    is_deleted    boolean                  default false,
    status        varchar(20)              default 'pending',
    start_date    date,
    end_date      date,
    plan_date     date,
    reminder_time timestamp with time zone,
    is_milestone  boolean                  default false
);

create index idx_tb_plan_phases_plan on tb_plan_phases (plan_id);


-- =============================================
-- 10. 计划任务表
-- =============================================
create table tb_plan_tasks
(
    id            bigserial primary key,
    phase_id      bigint       not null references tb_plan_phases on delete cascade,
    title         varchar(200) not null,
    description   text,
    ai_suggestion text,
    sort_order    integer                  default 0,
    created_at    timestamp with time zone default CURRENT_TIMESTAMP,
    is_deleted    boolean                  default false,
    status        varchar(20)              default 'pending',
    priority      varchar(20)              default 'medium',
    reminder_time timestamp with time zone,
    attachments   jsonb,
    start_date    date,
    end_date      date,
    plan_date     date,
    form_data     jsonb
);

create index idx_tb_plan_tasks_phase on tb_plan_tasks (phase_id);


-- =============================================
-- 11. 通知表
-- =============================================
create table tb_notification
(
    id           bigserial primary key,
    user_id      bigint       not null references tb_user on delete cascade,
    type         varchar(50)  not null,
    title        varchar(200) not null,
    content      varchar(500) not null,
    is_read      boolean                  default false,
    related_id   bigint,
    related_type varchar(50),
    created_at   timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at   timestamp with time zone default CURRENT_TIMESTAMP
);

comment on table tb_notification is '通知表';
comment on column tb_notification.type is '通知类型：system、comment、like、plan等';
comment on column tb_notification.is_read is '是否已读';
comment on column tb_notification.related_id is '关联业务ID（如动态ID、计划ID）';
comment on column tb_notification.related_type is '关联类型（post、plan等）';

create index idx_notification_user on tb_notification (user_id);
create index idx_notification_read on tb_notification (is_read);
create index idx_notification_type on tb_notification (type);


-- =============================================
-- 12. 用户画像表
-- =============================================
create table tb_user_profile
(
    id                 bigserial primary key,
    user_id            bigint not null references tb_user on delete cascade,
    country_preference jsonb,
    purpose            varchar(50),
    budget_range       jsonb,
    family_income      varchar(50),
    education_level    varchar(50),
    language_exam_type varchar(50),
    language_score     varchar(20),
    extra_data         jsonb     default '{}'::jsonb,
    status             integer   default 0,
    created_at         timestamp default CURRENT_TIMESTAMP,
    updated_at         timestamp default CURRENT_TIMESTAMP
);

comment on table tb_user_profile is '用户画像表';
comment on column tb_user_profile.id is '主键ID';
comment on column tb_user_profile.user_id is '关联用户ID';
comment on column tb_user_profile.country_preference is '意向国家/地区列表，JSON数组';
comment on column tb_user_profile.purpose is '出国目的：study_abroad留学/tourism旅游/working工作/immigration定居';
comment on column tb_user_profile.budget_range is '预算范围，JSON格式：{"min":200000,"max":500000,"currency":"CNY"}';
comment on column tb_user_profile.family_income is '家庭年收入范围';
comment on column tb_user_profile.education_level is '最高学历：high_school高中/associate大专/bachelor本科/master硕士/doctoral博士';
comment on column tb_user_profile.language_exam_type is '语言考试类型：ielts雅思/toefl托福/ptePTE/greGRE/gmatGMAT';
comment on column tb_user_profile.language_score is '语言成绩分数';
comment on column tb_user_profile.extra_data is '扩展信息，JSON格式，存储专业、GPA、学校、兴趣爱好等其他个性化字段';
comment on column tb_user_profile.status is '状态：0-未完善, 1-已完善';

create index idx_user_profile_user_id on tb_user_profile (user_id);


-- =============================================
-- 13. 评论表
-- =============================================
create table tb_comments
(
    id            bigserial primary key,
    answer_id     bigint                              not null,
    parent_id     bigint,
    user_id       bigint                              not null,
    content       text                                not null,
    likes         integer   default 0,
    replies_count integer   default 0,
    is_deleted    boolean   default false,
    created_at    timestamp default CURRENT_TIMESTAMP not null,
    updated_at    timestamp default CURRENT_TIMESTAMP not null
);

create index idx_comments_answer on tb_comments (answer_id);
create index idx_comments_parent on tb_comments (parent_id);
create index idx_comments_user on tb_comments (user_id);


-- =============================================
-- 14. 用户关注表
-- =============================================
create table tb_user_follows
(
    id           bigserial primary key,
    follower_id  bigint not null references tb_user,
    following_id bigint not null references tb_user,
    created_at   timestamp with time zone default CURRENT_TIMESTAMP,
    unique (follower_id, following_id),
    check (follower_id <> following_id)
);

create index idx_user_follows_follower on tb_user_follows (follower_id);
create index idx_user_follows_following on tb_user_follows (following_id);