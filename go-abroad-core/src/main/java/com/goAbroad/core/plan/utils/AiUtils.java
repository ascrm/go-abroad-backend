package com.goAbroad.core.plan.utils;

import com.goAbroad.common.exception.BusinessException;
import com.goAbroad.core.plan.dto.GeneratePlanRequest;
import com.goAbroad.core.plan.dto.SaveGeneratedRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiUtils {

    public static String buildPrompt(GeneratePlanRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("请根据以下用户信息，生成一个出国规划方案。\n\n");

        // 规划类型
        sb.append("【规划类型】").append(nullSafe(request.getType())).append("\n\n");

        // 目的地信息
        if (request.getDestination() != null && !request.getDestination().isEmpty()) {
            sb.append("【目的地信息】\n");
            for (var entry : request.getDestination().entrySet()) {
                sb.append("- ").append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
            sb.append("\n");
        }

        // 用户表单数据
        if (request.getFormData() != null && !request.getFormData().isEmpty()) {
            sb.append("【用户信息】\n");
            for (var entry : request.getFormData().entrySet()) {
                sb.append("- ").append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("【输出格式要求】\n");
        sb.append("请用 Markdown 格式输出规划方案，严格遵循以下结构：\n");
        sb.append("- 第一行：一级标题（## ），作为规划总标题\n");
        sb.append("- 每个阶段：一行二级标题（###），空一行后接一段阶段描述，再空一行后接若干任务列表\n");
        sb.append("- 每个任务：数字序号（1.），格式为 1. **任务标题** | 任务详细描述\n\n");

        sb.append("【格式规范】\n");
        sb.append("- 阶段数量建议 3~6 个，从准备到出发递进排列\n");
        sb.append("- 每个阶段包含 3~6 个具体任务\n");
        sb.append("- 任务描述应包含具体操作步骤、时间建议、注意事项，使任务可执行\n");
        sb.append("- 只输出 Markdown 格式内容，不要输出任何解释性文字\n\n");

        // 参考示例（自然语言部分，不含 [PARSE] 行）
        sb.append("【参考示例】\n\n");
        sb.append("假设用户提供以下信息：\n");
        sb.append("- 目的地：日本\n");
        sb.append("- 目标：日本东京大学修士（硕士）\n");
        sb.append("- 当前年级：大三\n");
        sb.append("- 日语水平：N2\n");
        sb.append("- 计划入学时间：2026年4月\n\n");
        sb.append("请按以下格式生成：\n\n");

        sb.append("## 日本东京大学修士留学规划（2025-2026）\n\n");
        sb.append("### 第一阶段：信息收集与目标确定\n\n");
        sb.append("全面了解东大修士申请要求、招生简章、时间节点，确定研究方向与目标导师。\n\n");
        sb.append("1. **研究教授信息** | 登录东大官网，查阅各研究室主页，整理目标教授的研究方向、论文列表及招生偏好，筛选3-5位匹配导师。\n\n");
        sb.append("2. **确认申请要求** | 查阅东大修士募集要项，明确语言成绩（托业/托福/日语）、研究计划书、研究科内诺等硬性要求，整理材料清单。\n\n");
        sb.append("3. **评估自身背景** | 整理现有GPA、获奖经历、科研项目、论文发表等背景，评估与东大要求的差距，明确提升方向。\n\n");
        sb.append("### 第二阶段：语言能力提升\n\n");
        sb.append("集中提升日语和英语成绩，达到东大各学科的出愿标准，争取高分。\n\n");
        sb.append("1. **日语N1冲刺** | 制定每日日语学习计划，重点突破阅读和听力，辅以写作训练；目标在2025年7月或12月JLPT中取得N1证书。\n\n");
        sb.append("2. **英语成绩备考** | 东大多数文科专业要求托业730+或托福80+，建议备考托业，目标800分以上；提前准备证件照和报名，避开旺季抢位。\n\n");
        sb.append("### 第三阶段：研究计划书撰写\n\n");
        sb.append("围绕目标研究室方向，撰写高质量研究计划书，反复修改完善。\n\n");
        sb.append("1. **确定研究课题** | 阅读目标教授近期论文3-5篇，结合自身兴趣，确定2-3个研究课题方向，初步撰写研究动机和意义。\n\n");
        sb.append("2. **撰写研究计划书** | 按东大格式要求撰写，涵盖研究背景、先行研究、研究目的、研究方法、预期成果、参考文献，字数控制在3000-5000字。\n\n");
        sb.append("3. **联系目标导师** | 通过邮件附上研究计划书草稿联系教授，邮件内容包括自我介绍、研究兴趣、为何选择该研究室；等待回复并根据反馈修改。\n\n");
        sb.append("### 第四阶段：材料准备与出愿\n\n");
        sb.append("按时间节点整理全部出愿材料，在规定时间内完成出愿手续。\n\n");
        sb.append("1. **整理成绩单等材料** | 联系本科学校教务处开具官方成绩单（日文或英文版）、毕业证明、学位证明；提前公证，确保材料真实有效。\n\n");
        sb.append("2. **完成在线出愿** | 登录东大申请系统，填写个人信息、教育经历，上传研究计划书、语言成绩单、照片等材料；检查无误后提交并缴纳检定料。\n\n");
        sb.append("3. **邮寄出愿材料** | 将纸质材料按东大要求装订密封，通过国际快递（EMS/DHL）邮寄，保留快递单号跟踪。\n\n");
        sb.append("### 第五阶段：等待与行前准备\n\n");
        sb.append("等待录取结果，做好赴日行前准备，办理在留资格和签证。\n\n");
        sb.append("1. **等待录取通知** | 关注东大出愿结果公布时间（通常为2-3月），保持邮箱畅通；若收到补交材料通知及时处理。\n\n");
        sb.append("2. **申请在留资格** | 收到录取通知后，配合学校申请在留资格认定证明书（COE），准备收入证明、经费支付书等材料；等待入管局审批。\n\n");
        sb.append("3. **办理留学签证** | 在留资格获批后，前往日本驻华大使馆/领事馆递交签证申请材料，准备护照、签证申请表、录取通知书、在留资格证明、照片等。\n\n");
        sb.append("4. **行前准备** | 预订机票、准备行李（药品、转换插头、少量日元现金）、开通手机漫游、加入国民健康保险、了解宿舍申请流程。\n\n");
        sb.append("请严格按照上述格式，根据用户提供的信息生成专属规划方案。");

        return sb.toString();
    }

    /**
     * 从 AI 返回的 Markdown 纯文本中解析出结构化数据。
     * 格式约定：
     * - 规划总标题：## 标题（在内容开头）
     * - 阶段标题：### 标题（独占一行）
     * - 阶段描述：阶段标题后第一段非任务文字
     * - 任务：数字序号（1.），格式为 1. **任务标题** | 任务描述
     */
    public static SaveGeneratedRequest.ParsedContent parseFromMarkdown(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("AI 返回的内容为空");
        }

        String trimmed = content.trim();

        // 1. 提取总标题：第一个 ## 到第一个 ### 之前的内容
        // 兼容 "##标题" 和 "## 标题" 两种格式
        String titleLine = getString(trimmed);

        SaveGeneratedRequest.ParsedContent result = new SaveGeneratedRequest.ParsedContent();
        result.setTitle(titleLine);

        // 2. 提取所有阶段：以 "\n###" 为分割点（兼容有无空格）
        // 兼容 "###标题" 和 "### 标题" 两种格式
        String[] splitParts = trimmed.split("(?=\\n###)");
        List<SaveGeneratedRequest.PhaseDto> phases = new ArrayList<>();

        for (int i = 1; i < splitParts.length; i++) {
            String section = splitParts[i].startsWith("\n")
                    ? splitParts[i].substring(1) : splitParts[i];

            SaveGeneratedRequest.PhaseDto phase = new SaveGeneratedRequest.PhaseDto();

            // 阶段标题：### 标题（到第一个换行）
            int hEnd = section.indexOf('\n');
            String rawTitle = hEnd < 0 ? section.trim() : section.substring(0, hEnd);
            phase.setTitle(rawTitle.replaceAll("^###\\s*", "").trim());

            // 阶段正文：去掉标题行（及可能的空行）后的内容
            String phaseBody = hEnd < 0 ? "" : section.substring(hEnd);

            // 阶段描述：第一行非空且不以数字序号开头的文字
            phase.setDescription("");
            if (!phaseBody.isBlank()) {
                for (String line : phaseBody.split("\n")) {
                    String t = line.trim();
                    if (!t.isEmpty() && !t.matches("\\d+\\.\\s.*")) {
                        phase.setDescription(t);
                        break;
                    }
                }
            }

            // 提取任务：1. **标题** | 描述（兼容 "1. **" 和 "1.**" 两种格式）
            List<SaveGeneratedRequest.TaskDto> tasks = new ArrayList<>();
            Matcher taskMatcher = Pattern.compile(
                    "^\\d+\\.\\s*\\*\\*(.+?)\\*\\*\\s*\\|\\s*(.+)$",
                    Pattern.MULTILINE
            ).matcher(phaseBody);
            while (taskMatcher.find()) {
                SaveGeneratedRequest.TaskDto task = new SaveGeneratedRequest.TaskDto();
                task.setTitle(taskMatcher.group(1).trim());
                task.setDescription(taskMatcher.group(2).trim().replaceAll("\\s+", " "));
                tasks.add(task);
            }
            phase.setTasks(tasks);
            phases.add(phase);
        }

        if (phases.isEmpty()) {
            throw new BusinessException("AI 返回内容缺少阶段（### 标题），无法解析");
        }

        result.setPhases(phases);
        return result;
    }

    private static String getString(String trimmed) {
        int titleStart = trimmed.indexOf("##");
        if (titleStart < 0) {
            throw new BusinessException("AI 返回内容缺少规划标题（## 标题）");
        }
        // 找第一个 ### 的位置（兼容有无空格）
        int titleEnd = trimmed.indexOf("\n###");
        if (titleEnd < 0) {
            titleEnd = trimmed.indexOf("\n### ");
        }
        if (titleEnd < 0) {
            // 尝试没有换行的 ###
            titleEnd = trimmed.indexOf("###");
            if (titleEnd <= titleStart) {
                titleEnd = trimmed.length();
            }
        }
        String titleLine = trimmed.substring(titleStart + 2, titleEnd).trim();
        if (titleLine.isBlank()) {
            throw new BusinessException("AI 返回内容缺少规划标题（## 标题）");
        }
        return titleLine;
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

    private static String nullSafe(Object value) {
        return value != null ? value.toString() : "未指定";
    }
}
