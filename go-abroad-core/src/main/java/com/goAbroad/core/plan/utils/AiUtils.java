package com.goAbroad.core.plan.utils;

import com.goAbroad.core.plan.dto.GeneratePlanRequest;

import java.util.Map;

public class AiUtils {

    /**
     * 构建用户提示词，包含输出示例（Few-shot）
     */
    public static String buildPrompt(GeneratePlanRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下信息生成一个留学/出境规划。\n");
        sb.append("请严格按照以下格式输出：\n\n");

        // 添加输出示例（Few-shot）
        sb.append("【输出示例】\n");
        sb.append("一、规划标题\n");
        sb.append("美国研究生申请全程规划\n\n");
        sb.append("二、阶段划分\n\n");
        sb.append("（一）准备阶段（2024年1月-3月）\n");
        sb.append("1. 准备阶段说明：进行留学前期准备，包括选校研究、材料准备等\n");
        sb.append("2. 参加语言考试：完成托福/雅思考试，目标分数100+/7.0+\n");
        sb.append("3. 准备申请材料：开具成绩单、准备在读证明等\n\n");
        sb.append("（二）申请阶段（2024年4月-8月）\n");
        sb.append("1. 申请阶段说明：完成学校申请提交\n");
        sb.append("2. 撰写个人陈述：突出个人优势和申请动机\n");
        sb.append("3. 获取推荐信：联系教授准备推荐信\n\n");
        sb.append("（三）录取阶段（2024年9月-12月）\n");
        sb.append("1. 录取阶段说明：等待录取结果\n");
        sb.append("2. 跟进申请状态：及时查看申请 portal\n");
        sb.append("3. 选择offer：综合考虑后确定最终入读学校\n\n");
        sb.append("（四）行前阶段（2025年1月-8月）\n");
        sb.append("1. 行前阶段说明：做好行前准备工作\n");
        sb.append("2. 办理签证：准备签证材料，预约面签\n");
        sb.append("3. 体检与疫苗：完成体检和required疫苗\n");
        sb.append("4. 预订机票和住宿：安排行程和住宿\n\n");
        sb.append("【注意】\n");
        sb.append("- 阶段名称使用中文数字：一、二、三、四\n");
        sb.append("- 阶段内使用阿拉伯数字：1. 2. 3.\n");
        sb.append("- 阶段说明格式：阶段名称+（时间范围）+说明文字\n");
        sb.append("- 任务说明格式：序号+任务名称：任务详细说明\n");
        sb.append("- 共3-4个阶段，每个阶段2-4个任务\n");
        sb.append("- 用简洁的中文\n\n");

        sb.append("【用户信息】\n");
        if (request.getDestination() != null) {
            sb.append("目的地：").append(request.getDestination()).append("\n");
        }
        if (request.getFormData() != null) {
            for (Map.Entry<String, Object> entry : request.getFormData().entrySet()) {
                sb.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
        }

        sb.append("\n请直接输出规划内容，不需要其他解释。");
        return sb.toString();
    }

    /**
     * 根据类型获取系统提示词
     */
    public static String getSystemPromptByType(String type) {
        if (type == null) {
            return "你是一个专业的出境规划顾问，请用简洁的中文回复。";
        }
        return switch (type.toLowerCase()) {
            case "study" -> "你是一个专业的留学规划顾问，专长于申请流程、学校选择、签证办理等。请用简洁的中文回复。";
            case "tourism" -> "你是一个专业的出境旅游规划顾问，专长于行程安排、签证办理、景点推荐等。请用简洁的中文回复。";
            case "work" -> "你是一个专业的海外工作规划顾问，专长于职业规划、求职技巧、工签办理等。请用简洁的中文回复。";
            case "immigration" -> "你是一个专业的移民规划顾问，专长于各国移民政策、投资移民、技术移民等。请用简洁的中文回复。";
            default -> "你是一个专业的出境规划顾问，请用简洁的中文回复。";
        };
    }

    /**
     * 构建解析 content 的提示词，将自然语言文本解析为 JSON 结构
     */
    public static String buildParseContentPrompt(String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("请将以下留学/出境规划文本解析为JSON结构。\n\n");
        sb.append("【解析要求】\n");
        sb.append("1. 提取规划标题（title）- 位于\"一、\"后面的内容\n");
        sb.append("2. 提取所有阶段（phases），每个阶段包含：\n");
        sb.append("   - title: 阶段标题，如\"准备阶段\"、\"申请阶段\"等\n");
        sb.append("   - description: 阶段说明，从\"阶段说明\"或括号内的内容提取\n");
        sb.append("   - tasks: 该阶段下的所有任务列表\n");
        sb.append("3. 每个任务（task）包含：\n");
        sb.append("   - title: 任务标题（序号后面的内容，冒号前）\n");
        sb.append("   - description: 任务详细说明（冒号后面的内容）\n\n");
        sb.append("【输出格式】\n");
        sb.append("只输出JSON，不要有其他文字。格式如下：\n");
        sb.append("{\n");
        sb.append("  \"title\": \"规划标题\",\n");
        sb.append("  \"phases\": [\n");
        sb.append("    {\n");
        sb.append("      \"title\": \"阶段标题\",\n");
        sb.append("      \"description\": \"阶段说明\",\n");
        sb.append("      \"tasks\": [\n");
        sb.append("        {\"title\": \"任务标题\", \"description\": \"任务说明\"}\n");
        sb.append("      ]\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n\n");
        sb.append("【待解析文本】\n");
        sb.append(content);
        return sb.toString();
    }
}
