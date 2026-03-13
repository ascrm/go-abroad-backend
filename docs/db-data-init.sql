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

-- ============================================
-- Home 模块初始数据
-- ============================================

-- 4. 添加更多用户（文章和问答的作者）
INSERT INTO "tb_user" (username, nickname, avatar, gender, bio, status, created_at, updated_at)
VALUES
    ('wang_li', '留学顾问王老师', 'https://example.com/avatar/wang.png', 1, '资深留学顾问，从事留学申请指导10年', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('li_xiao', '英国留学生小李', 'https://example.com/avatar/li.png', 1, '英国大学在读研究生', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('xiaoming', '小明同学', 'https://example.com/avatar/xiaoming.png', 1, '准备申请英国研究生', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('kaoyadaren', '烤鸭达人', 'https://example.com/avatar/kaoya.png', 1, '雅思备考中', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('lixiaomeng', '留学小白', 'https://example.com/avatar/lxm.png', 1, '刚开始了解留学信息', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('london_dream', '伦敦梦', 'https://example.com/avatar/london.png', 1, '想去伦敦留学', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING
RETURNING id, username;

-- 5. 插入推荐文章数据
INSERT INTO tb_articles (title, description, content, image, tag, author_id, views, favorites, is_published, is_featured, published_at, created_at, updated_at)
VALUES
(
    '2024年留学申请全攻略',
    '从选校到拿到offer的完整指南，包含选校、申请材料、面试技巧等全方位指导',
    '出国留学是人生中重要的决定，需要做好充分的准备。本文将为你提供2024年留学申请的全面攻略，帮助你从选校到拿到offer的完整过程。

一、选校定位

1.1 确定专业方向

首先，你需要明确自己的兴趣和职业规划，选择适合自己的专业方向。建议考虑以下因素：
• 个人兴趣与爱好
• 专业的就业前景
• 自身学术背景
• 专业的录取难度

1.2 了解目标院校

收集目标院校的详细信息，包括：
• 学校排名和专业排名
• 学费和生活费
• 地理位置和就业机会
• 国际生比例

二、申请材料准备

2.1 学术材料
• 成绩单
• 在读证明/毕业证书
• 语言成绩（雅思/托福）
• GRE/GMAT成绩（如需要）

2.2 文书材料
• 个人陈述（Personal Statement）
• 推荐信
• 简历

三、申请流程

3.1 时间规划
建议按照以下时间节点进行准备：
• 提前1年开始准备
• 提前6-9个月准备文书
• 提前3-6个月提交申请

3.2 网申填写
注意仔细核对每一项信息，确保准确无误。

四、面试技巧

部分院校需要面试，以下是一些建议：
1. 提前准备常见问题
2. 保持自信和专业
3. 展示你的独特优势
4. 及时跟进面试结果

五、offer选择

收到offer后，需要考虑：
• 学校综合实力
• 奖学金情况
• 专业排名
• 就业前景

总结：留学申请是一个复杂的过程，需要提前规划和准备。希望本文能够帮助到你，祝你申请顺利！',
    'https://picsum.photos/400/200?random=1',
    '攻略',
    (SELECT id FROM "tb_user" WHERE username = 'admin'),
    1234,
    89,
    true,
    true,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    'QS排名前100的英国大学一览',
    '申请条件、学费信息一文搞定',
    '本文整理了QS排名前100的英国大学，包括牛津大学、剑桥大学、帝国理工学院、伦敦大学学院等。详细介绍各校的申请条件、学费信息、奖学金政策等。',
    'https://picsum.photos/200/150?random=2',
    '排名',
    (SELECT id FROM "tb_user" WHERE username = 'admin'),
    856,
    45,
    true,
    true,
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
),
(
    '出国行李清单汇总',
    '留学生必带的物品推荐',
    '即将踏上留学之旅，行李清单是每个留学生都需要准备的。本文为你整理了详细的行李清单，包括生活用品、学习用品、药品、证件等。',
    'https://picsum.photos/200/150?random=3',
    '生活',
    (SELECT id FROM "tb_user" WHERE username = 'testuser'),
    2100,
    120,
    true,
    true,
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
),
(
    '各国签证政策汇总2024',
    '最新各国入境政策及签证办理指南',
    '2024年各国签证政策有什么变化？本文为你汇总了美国、英国、澳大利亚、加拿大等热门留学国家的最新签证政策及办理指南。',
    'https://picsum.photos/200/150?random=4',
    '签证',
    (SELECT id FROM "tb_user" WHERE username = 'john_doe'),
    1567,
    78,
    true,
    true,
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
)
ON CONFLICT DO NOTHING;

-- 6. 插入问答问题数据
INSERT INTO tb_questions (title, content, author_id, category, views, replies_count, is_resolved, is_deleted, created_at, updated_at)
SELECT title, content, author_id, category, views, replies_count, is_resolved, is_deleted, created_at, updated_at
FROM (
    VALUES
    (
        '申请英国研究生需要哪些材料？',
        '我想申请英国的研究生，想了解一下需要准备哪些材料？',
        (SELECT id FROM "tb_user" WHERE username = 'xiaoming' LIMIT 1),
        '申请',
        156,
        12,
        false,
        false,
        CURRENT_TIMESTAMP - INTERVAL '1 hours',
        CURRENT_TIMESTAMP - INTERVAL '1 hours'
    ),
    (
        '雅思口语复议成功率高吗？',
        '我的雅思成绩口语差了0.5分，想问一下复议的成功率怎么样？值不值得去复议？',
        (SELECT id FROM "tb_user" WHERE username = 'kaoyadaren' LIMIT 1),
        '语言',
        89,
        8,
        false,
        false,
        CURRENT_TIMESTAMP - INTERVAL '3 hours',
        CURRENT_TIMESTAMP - INTERVAL '3 hours'
    ),
    (
        '有没有靠谱的留学中介推荐？',
        '想找一家靠谱的留学中介，大家有推荐的吗？最好是在英国申请方面比较专业的。',
        (SELECT id FROM "tb_user" WHERE username = 'lixiaomeng' LIMIT 1),
        '中介',
        342,
        25,
        false,
        false,
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    ),
    (
        '英国留学签证肺结核检查在哪做？',
        '准备去英国留学，需要做肺结核检查，想问一下在哪里可以做？需要提前预约吗？',
        (SELECT id FROM "tb_user" WHERE username = 'london_dream' LIMIT 1),
        '签证',
        78,
        6,
        false,
        false,
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    )
) AS t(title, content, author_id, category, views, replies_count, is_resolved, is_deleted, created_at, updated_at)
WHERE author_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 7. 插入问答回答数据
INSERT INTO tb_answers (question_id, author_id, content, likes, replies_count, is_official, is_best_answer, is_deleted, created_at, updated_at)
SELECT question_id, author_id, content, likes, replies_count, is_official, is_best_answer, is_deleted, created_at, updated_at
FROM (
    VALUES
    (
        (SELECT id FROM tb_questions WHERE title = '申请英国研究生需要哪些材料？' LIMIT 1),
        (SELECT id FROM "tb_user" WHERE username = 'wang_li' LIMIT 1),
        '申请英国研究生需要准备以下材料：

1. 学术材料
- 本科成绩单（需要学校盖章的英文版）
- 在读证明/毕业证书
- 学位证书（如已毕业）

2. 语言成绩
- 雅思或托福成绩（建议雅思6.5分以上，托福92分以上）

3. 文书材料
- 个人陈述（Personal Statement）
- 两封推荐信
- 简历（CV）

4. 其他材料
- 护照复印件
- 资金证明
- 作品集（部分专业需要）

建议提前6-9个月开始准备，祝申请顺利！',
        45,
        2,
        true,
        true,
        false,
        CURRENT_TIMESTAMP - INTERVAL '30 minutes',
        CURRENT_TIMESTAMP - INTERVAL '30 minutes'
    ),
    (
        (SELECT id FROM tb_questions WHERE title = '申请英国研究生需要哪些材料？' LIMIT 1),
        (SELECT id FROM "tb_user" WHERE username = 'li_xiao' LIMIT 1),
        '补充一下我当时的经验：

1. 成绩单一定要提前翻译好，有的学校可以提供英文版，有的需要自己找翻译机构

2. 推荐信建议提前2个月联系老师，让老师有足够时间准备

3. 个人陈述要突出自己的独特优势，不要泛泛而谈

4. 记得关注各学校的申请截止日期，不要错过！',
        18,
        1,
        false,
        false,
        false,
        CURRENT_TIMESTAMP - INTERVAL '1 hours',
        CURRENT_TIMESTAMP - INTERVAL '1 hours'
    ),
    (
        (SELECT id FROM tb_questions WHERE title = '雅思口语复议成功率高吗？' LIMIT 1),
        (SELECT id FROM "tb_user" WHERE username = 'wang_li' LIMIT 1),
        '雅思口语复议的成功率取决于多个因素：

1. 如果你的成绩和预期差距较大，复议成功的可能性较高
2. 如果口语单项成绩和其他三项差距明显，也值得尝试
3. 复议费用不高，建议尝试

一般复议周期为6周左右，如果成绩提升，会退还费用。祝你复议成功！',
        23,
        0,
        true,
        false,
        false,
        CURRENT_TIMESTAMP - INTERVAL '2 hours',
        CURRENT_TIMESTAMP - INTERVAL '2 hours'
    ),
    (
        (SELECT id FROM tb_questions WHERE title = '有没有靠谱的留学中介推荐？' LIMIT 1),
        (SELECT id FROM "tb_user" WHERE username = 'li_xiao' LIMIT 1),
        '我之前找中介的时候也纠结了很久，最后选择了一家有英国大学官方代理资质的中介。建议选择中介时注意以下几点：

1. 是否有英国大学的官方代理资质
2. 成功案例是否丰富
3. 文书老师是否专业
4. 合同条款是否透明

也可以考虑半DIY模式，自己负责选校和文书，中介负责申请提交和签证。',
        35,
        3,
        false,
        false,
        false,
        CURRENT_TIMESTAMP - INTERVAL '6 hours',
        CURRENT_TIMESTAMP - INTERVAL '6 hours'
    ),
    (
        (SELECT id FROM tb_questions WHERE title = '英国留学签证肺结核检查在哪做？' LIMIT 1),
        (SELECT id FROM "tb_user" WHERE username = 'wang_li' LIMIT 1),
        '英国签证肺结核检查需要在指定的体检中心进行。

中国境内指定的体检中心城市包括：北京、上海、广州、深圳、成都、重庆、武汉、杭州、南京、沈阳等。

注意事项：
1. 需要提前预约
2. 检查费用约为550元人民币
3. 需要携带护照、身份证、2张白底签证照片
4. 无需空腹
5. 检查结果当天可取

建议提前1-2周预约，旺季可能需要更早。',
        15,
        0,
        true,
        false,
        false,
        CURRENT_TIMESTAMP - INTERVAL '4 hours',
        CURRENT_TIMESTAMP - INTERVAL '4 hours'
    )
) AS t(question_id, author_id, content, likes, replies_count, is_official, is_best_answer, is_deleted, created_at, updated_at)
WHERE question_id IS NOT NULL AND author_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 验证Home模块数据
SELECT '文章表' as table_name, COUNT(*) as count FROM tb_articles
UNION ALL
SELECT '问题表', COUNT(*) FROM tb_questions
UNION ALL
SELECT '回答表', COUNT(*) FROM tb_answers;
