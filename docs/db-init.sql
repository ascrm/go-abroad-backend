create table tb_user
(
    id         bigserial
        primary key,
    username   varchar(50)
        unique,
    nickname   varchar(100),
    avatar     varchar(500),
    gender     integer   default 0,
    birthday   date,
    bio        varchar(500),
    status     integer   default 1,
    created_at timestamp default CURRENT_TIMESTAMP,
    updated_at timestamp default CURRENT_TIMESTAMP
);

comment on table tb_user is '用户表';

comment on column tb_user.gender is '性别: 0-未知, 1-男, 2-女';

comment on column tb_user.status is '状态: 0-禁用, 1-正常';

alter table tb_user
    owner to root;

create table tb_user_account
(
    id            bigserial
        primary key,
    user_id       bigint       not null
        references tb_user
            on delete cascade,
    account_type  integer      not null,
    account_value varchar(100) not null,
    password      varchar(255),
    salt          varchar(50),
    verified      boolean   default false,
    created_at    timestamp default CURRENT_TIMESTAMP,
    updated_at    timestamp default CURRENT_TIMESTAMP,
    unique (account_type, account_value),
    constraint uk3r7eb2sdn2rc8s777rj4li7r6
        unique (account_type, account_value)
);

comment on table tb_user_account is '账号表';

comment on column tb_user_account.account_type is '账号类型: 1-用户名, 2-邮箱, 3-手机号';

alter table tb_user_account
    owner to root;

create index idx_user_account_user_id
    on tb_user_account (user_id);

create index idx16srcgnlhli5gqso04sqf5ygr
    on tb_user_account (user_id);

create table tb_user_social
(
    id            bigserial
        primary key,
    user_id       bigint       not null
        references tb_user
            on delete cascade,
    social_type   integer      not null,
    openid        varchar(100) not null,
    unionid       varchar(100),
    access_token  varchar(500),
    refresh_token varchar(500),
    expires_at    timestamp,
    created_at    timestamp default CURRENT_TIMESTAMP,
    updated_at    timestamp default CURRENT_TIMESTAMP,
    unique (social_type, openid),
    constraint ukfwkt048vx1dogygb9j4vqqsrm
        unique (social_type, openid)
);

comment on table tb_user_social is '第三方登录表';

comment on column tb_user_social.social_type is '平台类型: 1-微信, 2-QQ, 3-Google, 4-Apple, 5-抖音';

alter table tb_user_social
    owner to root;

create index idx_user_social_user_id
    on tb_user_social (user_id);

create index idxqag34y3vcui2n7nv4trpmr30
    on tb_user_social (user_id);

create table tb_articles
(
    id           bigserial
        primary key,
    title        varchar(200) not null,
    description  text,
    content      text         not null,
    image        varchar(500),
    tag          varchar(50),
    author_id    bigint
        references tb_user,
    views        integer                  default 0,
    favorites    integer                  default 0,
    is_published boolean                  default true,
    is_featured  boolean                  default false,
    published_at timestamp with time zone,
    created_at   timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at   timestamp with time zone default CURRENT_TIMESTAMP
);

alter table tb_articles
    owner to root;

create index idx_articles_published
    on tb_articles (is_published, published_at);

create index idx_articles_featured
    on tb_articles (is_featured);

create table tb_questions
(
    id            bigserial
        primary key,
    title         varchar(200) not null,
    content       text         not null,
    author_id     bigint
        references tb_user,
    category      varchar(50),
    views         integer                  default 0,
    replies_count integer                  default 0,
    is_resolved   boolean                  default false,
    is_deleted    boolean                  default false,
    created_at    timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at    timestamp with time zone default CURRENT_TIMESTAMP
);

alter table tb_questions
    owner to root;

create index idx_questions_author
    on tb_questions (author_id);

create index idx_questions_category
    on tb_questions (category);

create index idx_questions_resolved
    on tb_questions (is_resolved);

create table tb_answers
(
    id             bigserial
        primary key,
    question_id    bigint not null
        references tb_questions
            on delete cascade,
    author_id      bigint
        references tb_user,
    content        text   not null,
    likes          integer                  default 0,
    replies_count  integer                  default 0,
    is_official    boolean                  default false,
    is_best_answer boolean                  default false,
    is_deleted     boolean                  default false,
    created_at     timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at     timestamp with time zone default CURRENT_TIMESTAMP
);

alter table tb_answers
    owner to root;

create index idx_answers_question
    on tb_answers (question_id);

create index idx_answers_author
    on tb_answers (author_id);

create index idx_answers_official
    on tb_answers (is_official);

create table tb_interactions
(
    id          bigserial
        primary key,
    user_id     bigint      not null
        references tb_user
            on delete cascade,
    target_id   bigint      not null,
    target_type varchar(20) not null
        constraint tb_interactions_target_type_check
            check ((target_type)::text = ANY
                   ((ARRAY ['article'::character varying, 'question'::character varying, 'answer'::character varying])::text[])),
    action      varchar(20) not null
        constraint tb_interactions_action_check
            check ((action)::text = ANY
                   ((ARRAY ['favorite'::character varying, 'like'::character varying, 'follow'::character varying, 'view'::character varying])::text[])),
    created_at  timestamp with time zone default CURRENT_TIMESTAMP,
    unique (user_id, target_id, target_type, action),
    constraint uk_user_target_type_action
        unique (user_id, target_id, target_type, action)
);

alter table tb_interactions
    owner to root;

create index idx_interactions_user
    on tb_interactions (user_id, action);

create index idx_interactions_target
    on tb_interactions (target_id, target_type);

create table tb_plans
(
    id          bigserial
        primary key,
    user_id     bigint       not null
        references tb_user
            on delete cascade,
    title       varchar(200) not null,
    type        varchar(20)  not null
        constraint tb_plans_type_check
            check ((type)::text = ANY
                   ((ARRAY ['tourism'::character varying, 'study'::character varying, 'work'::character varying, 'immigration'::character varying])::text[])),
    destination jsonb,
    status      varchar(20)              default 'generating'::character varying
        constraint tb_plans_status_check
            check ((status)::text = ANY
                   ((ARRAY ['draft'::character varying, 'generating'::character varying, 'completed'::character varying, 'archived'::character varying])::text[])),
    form_data   jsonb,
    cover_image varchar(500),
    created_at  timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at  timestamp with time zone default CURRENT_TIMESTAMP
);

alter table tb_plans
    owner to root;

create index idx_tb_plans_user
    on tb_plans (user_id);

create index idx_tb_plans_type
    on tb_plans (type);

create index idx_tb_plans_status
    on tb_plans (status);

create table tb_plan_phases
(
    id          bigserial
        primary key,
    plan_id     bigint       not null
        references tb_plans
            on delete cascade,
    title       varchar(100) not null,
    description text,
    sort_order  integer                  default 0,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP
);

alter table tb_plan_phases
    owner to root;

create index idx_tb_plan_phases_plan
    on tb_plan_phases (plan_id);

create table tb_plan_tasks
(
    id            bigserial
        primary key,
    phase_id      bigint       not null
        references tb_plan_phases
            on delete cascade,
    title         varchar(200) not null,
    description   text,
    ai_suggestion text,
    quick_links   jsonb,
    is_completed  boolean                  default false,
    completed_at  timestamp with time zone,
    sort_order    integer                  default 0,
    created_at    timestamp with time zone default CURRENT_TIMESTAMP
);

alter table tb_plan_tasks
    owner to root;

create index idx_tb_plan_tasks_phase
    on tb_plan_tasks (phase_id);

create table tb_resource_category
(
    id         bigserial
        primary key,
    name       varchar(50)                         not null,
    icon       varchar(50)                         not null,
    color      varchar(20)                         not null,
    sort_order integer   default 0                 not null,
    is_active  boolean   default true              not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp default CURRENT_TIMESTAMP not null
);

comment on table tb_resource_category is '出境资源分类表，定义签证、酒店、交通等资源类别';

comment on column tb_resource_category.id is '类别主键ID';

comment on column tb_resource_category.name is '类别名称';

comment on column tb_resource_category.icon is '前端Lucide图标组件名';

comment on column tb_resource_category.color is '类别主题色（十六进制）';

comment on column tb_resource_category.sort_order is '展示顺序';

comment on column tb_resource_category.is_active is '是否启用（软删除）';

comment on column tb_resource_category.created_at is '创建时间';

comment on column tb_resource_category.updated_at is '更新时间';

alter table tb_resource_category
    owner to root;

create index idx_tb_resource_category_sort_order
    on tb_resource_category (sort_order);

create table tb_resource
(
    id          bigserial
        primary key,
    country     varchar(50)                         not null,
    category_id bigint                              not null
        constraint fk_resource_category
            references tb_resource_category
            on delete restrict,
    title       varchar(200)                        not null,
    description varchar(500)                        not null,
    url         varchar(500)                        not null,
    web_url     varchar(500),
    image_url   varchar(500),
    logo        varchar(500),
    is_featured boolean   default false             not null,
    meta        jsonb     default '{}'::jsonb       not null,
    sort_order  integer   default 0                 not null,
    is_active   boolean   default true              not null,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    updated_at  timestamp default CURRENT_TIMESTAMP not null
);

comment on table tb_resource is '出境资源表，存储各国分类下的具体资源链接信息';

comment on column tb_resource.id is '资源主键ID';

comment on column tb_resource.country is '国家/地区名称';

comment on column tb_resource.category_id is '关联的资源类别ID';

comment on column tb_resource.title is '资源标题';

comment on column tb_resource.description is '资源简短描述';

comment on column tb_resource.url is 'App深链URL';

comment on column tb_resource.web_url is '降级Web URL（深链不可用时跳转）';

comment on column tb_resource.image_url is '封面图片URL';

comment on column tb_resource.is_featured is '是否精选（精选资源展示为大卡片）';

comment on column tb_resource.meta is '扩展元数据（JSON），可存highlights、cta等';

comment on column tb_resource.sort_order is '同类别内的展示顺序';

comment on column tb_resource.is_active is '是否启用（软删除）';

comment on column tb_resource.created_at is '创建时间';

comment on column tb_resource.updated_at is '更新时间';

alter table tb_resource
    owner to root;

create index idx_tb_resource_country
    on tb_resource (country);

create index idx_tb_resource_category_id
    on tb_resource (category_id);

create index idx_tb_resource_country_category
    on tb_resource (country, category_id);

create index idx_tb_resource_featured
    on tb_resource (is_featured)
    where (is_featured = true);

create index idx_tb_resource_meta_gin
    on tb_resource using gin (meta);

