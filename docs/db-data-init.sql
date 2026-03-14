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
SELECT title, description, content, image, tag, author_id, views, favorites, is_published, is_featured, published_at, created_at, updated_at
FROM (
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
                 (SELECT id FROM "tb_user" WHERE username = 'admin' LIMIT 1),
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
                 (SELECT id FROM "tb_user" WHERE username = 'admin' LIMIT 1),
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
                 (SELECT id FROM "tb_user" WHERE username = 'testuser' LIMIT 1),
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
                 (SELECT id FROM "tb_user" WHERE username = 'john_doe' LIMIT 1),
                 1567,
                 78,
                 true,
                 true,
                 CURRENT_TIMESTAMP - INTERVAL '5 days',
                 CURRENT_TIMESTAMP - INTERVAL '5 days',
                 CURRENT_TIMESTAMP - INTERVAL '5 days'
             )
     ) AS t(title, description, content, image, tag, author_id, views, favorites, is_published, is_featured, published_at, created_at, updated_at)
WHERE author_id IS NOT NULL
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

-- ============================================
-- Plan 模块初始数据
-- ============================================

-- 8. 插入规划数据
INSERT INTO tb_plans (user_id, title, type, destination, status, form_data, created_at, updated_at)
SELECT user_id, title, type, destination, status, form_data, created_at, updated_at
FROM (
         -- 赴美读研规划 (wang_li)
         SELECT (SELECT id FROM "tb_user" WHERE username = 'wang_li' LIMIT 1) as user_id,
                '赴美读研规划' as title,
                'study' as type,
                '{"country": "美国"}'::jsonb as destination,
                'completed' as status,
                '{"destination": {"country": "美国"}, "targetDegree": "硕士", "targetMajor": "计算机科学", "currentBackground": "北大 3.5", "languageAbility": "雅思7+", "financialAbility": "40-60万/年", "timePlan": "2026年秋季"}'::jsonb as form_data,
                CURRENT_TIMESTAMP - INTERVAL '2 days' as created_at,
                CURRENT_TIMESTAMP - INTERVAL '2 days' as updated_at
         UNION ALL
         -- 日本7日游 (wang_li)
         SELECT (SELECT id FROM "tb_user" WHERE username = 'wang_li' LIMIT 1),
                '日本7日游', 'tourism', '{"country": "日本"}'::jsonb, 'generating',
                '{"destination": {"country": "日本"}, "travelBudget": "标准型", "travelDays": "7-14天", "companions": "情侣", "passportStatus": "有护照(有效期>6个月)", "profession": "在职"}'::jsonb,
                CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
         UNION ALL
         -- 英国工作规划 (li_xiao)
         SELECT (SELECT id FROM "tb_user" WHERE username = 'li_xiao' LIMIT 1),
                '英国工作规划', 'work', '{"country": "英国"}'::jsonb, 'draft',
                '{"destination": {"country": "英国"}, "jobField": "IT/互联网", "certificates": "本科 + 软件工程师证书", "languageSkill": "良好(日常交流)", "workExperience": "3-5年", "familyAccompany": "不需要", "jobStatus": "观望中"}'::jsonb,
                CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'
     ) AS t(user_id, title, type, destination, status, form_data, created_at, updated_at)
WHERE user_id IS NOT NULL
ON CONFLICT DO NOTHING
RETURNING id, title;

-- 9. 插入阶段数据（针对赴美读研规划）
INSERT INTO tb_plan_phases (plan_id, title, description, sort_order, created_at)
SELECT plan_id, title, description, sort_order, created_at
FROM (
         SELECT (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) as plan_id,
                '选校定位' as title, '确定目标院校和专业' as description, 1 as sort_order,
                CURRENT_TIMESTAMP - INTERVAL '2 days' as created_at, CURRENT_TIMESTAMP - INTERVAL '2 days' as updated_at
         UNION ALL
         SELECT (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1), '语言考试', '准备并完成语言考试', 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1), '申请材料准备', '准备各项申请材料', 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1), '提交申请', '完成院校申请', 4, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1), '等待offer', '等待录取结果', 5, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1), '签证办理', '办理学生签证', 6, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1), '行前准备', '出发前的准备工作', 7, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
     ) AS t(plan_id, title, description, sort_order, created_at, updated_at)
WHERE plan_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 10. 插入任务数据
INSERT INTO tb_plan_tasks (phase_id, title, description, is_completed, ai_suggestion, sort_order, created_at)
SELECT phase_id, title, description, is_completed, ai_suggestion, sort_order, created_at
FROM (
         -- 选校定位阶段任务
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '选校定位' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1) as phase_id,
                '确定留学专业方向' as title,
                '明确想申请的计算机科学专业方向' as description,
                true as is_completed,
                '建议提前了解各校 CS 项目的特色，如 AI、ML、Systems 等方向' as ai_suggestion,
                1 as sort_order,
                CURRENT_TIMESTAMP - INTERVAL '2 days' as created_at,
                CURRENT_TIMESTAMP - INTERVAL '2 days' as updated_at
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '选校定位' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '收集目标院校信息', '了解各校的排名、录取要求、学费等', true, '重点关注 US News 排名靠前的 CS 项目', 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '选校定位' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '评估自身背景与院校匹配度', '根据 GPA、科研、实习等评估录取可能性', false, '建议使用 Stanford、MIT、CMU 等作为冲刺院校', 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '选校定位' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '确定申请院校 List', '列出冲刺、稳妥、保底院校', false, '建议 3-4 所冲刺，3-4 所稳妥，2-3 所保底', 4, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         -- 语言考试阶段任务
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '语言考试' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '确定目标院校语言要求', '了解各校的语言成绩要求', false, '顶尖 CS 项目一般要求雅思 7.0 或托福 100+', 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '语言考试' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '报名语言考试', '报名雅思或托福考试', false, '建议提前 2-3 个月报名，旺季可能满位', 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '语言考试' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '制定备考计划', '安排每天的备考时间', false, '建议每天保证 2-3 小时的有效学习时间', 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '语言考试' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '参加考试并达到目标分数', '完成考试并获得合格成绩', false, '建议预留 2 次考试机会', 4, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         -- 申请材料准备阶段任务
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '申请材料准备' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '开具成绩单和在读证明', '准备中英文成绩单和在读证明', false, '建议一次性开具 10 份左右备用', 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '申请材料准备' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '撰写个人陈述(PS)', '完成个人陈述的撰写和修改', false, 'PS 是申请中最关键的材料，建议提前 1 个月开始准备', 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '申请材料准备' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '准备推荐信', '联系推荐人并准备推荐信', false, '建议找了解你的教授或实习导师', 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '申请材料准备' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '制作简历(CV)', '准备英文简历', false, '控制在一页以内，突出科研和项目经验', 4, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         -- 提交申请阶段任务
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '提交申请' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '注册院校申请账号', '在各校申请系统注册账号', false, '建议使用专门邮箱管理申请', 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '提交申请' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '填写申请表', '完成各校的申请表填写', false, '注意各校截止日期，建议提前一周提交', 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '提交申请' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '上传申请材料', '上传成绩单、PS、CV 等材料', false, '确保文件格式和大小符合要求', 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '提交申请' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '缴纳申请费用', '支付各校申请费', false, '每所学校 50-150 美元不等', 4, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         -- 等待 offer 阶段任务
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '等待offer' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '跟进申请状态', '定期查看申请状态', false, '部分学校会发面试邀请', 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '等待offer' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '收到offer后确认入读', '在截止日期前确认入读学校', false, '注意各校 deposit 截止日期', 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '等待offer' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '缴纳定金/占位费', '向确认入读的學校缴纳定金', false, '定金一般为 100-1000 美元', 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         -- 签证办理阶段任务
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '签证办理' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '准备签证材料', '准备 I-20、护照、签证照片等', false, '需要提供资金证明', 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '签证办理' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '预约签证面签', '预约美国大使馆面签时间', false, '建议提前 1-2 个月预约', 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '签证办理' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '参加面签', '前往大使馆参加签证面谈', false, '提前准备好常见问题答案', 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '签证办理' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '获得签证', '领取护照和签证', false, '一般 3-5 个工作日出签', 4, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         -- 行前准备阶段任务
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '行前准备' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '申请住宿', '申请学校宿舍或校外租房', false, '建议提前 2-3 个月开始申请宿舍', 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '行前准备' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '购买机票', '预订赴美机票', false, '建议提前 1-2 个月购买', 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '行前准备' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '购买保险', '购买国际学生医疗保险', false, '学校通常会提供保险选项', 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '行前准备' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '体检', '完成出境体检和疫苗接种', false, '部分学校要求特定疫苗', 4, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
         UNION ALL
         SELECT (SELECT id FROM tb_plan_phases WHERE title = '行前准备' AND plan_id = (SELECT id FROM tb_plans WHERE title = '赴美读研规划' LIMIT 1) LIMIT 1),
                '准备行李', '整理赴美行李', false, '提前了解航空公司行李规定', 5, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
     ) AS t(phase_id, title, description, is_completed, ai_suggestion, sort_order, created_at, updated_at)
WHERE phase_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 验证Plan模块数据
SELECT '规划表' as table_name, COUNT(*) as count FROM tb_plans
UNION ALL
SELECT '阶段表', COUNT(*) FROM tb_plan_phases
UNION ALL
SELECT '任务表', COUNT(*) FROM tb_plan_tasks;
