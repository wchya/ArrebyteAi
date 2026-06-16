package cn.arrebyte.ai.mcpserver.tool;

import cn.arrebyte.ai.mcpserver.skill.SkillDefinition;
import cn.arrebyte.ai.mcpserver.skill.SkillLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillTool {

    private final SkillLoader skillLoader;

    @Tool(name = "listSkills",
          description = "列出所有可用的 Skills。当用户询问有什么技能、能做什么时调用此工具。")
    public String listSkills() {
        log.info("调用工具: listSkills");

        List<SkillDefinition> skills = skillLoader.getAllSkills();

        if (skills.isEmpty()) {
            return "当前没有可用的 Skills。请在 .opencode/skills/ 目录下创建 SKILL.md 文件。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("系统共有 ").append(skills.size()).append(" 个可用的 Skills：\n\n");

        for (SkillDefinition skill : skills) {
            sb.append("【").append(skill.getName()).append("】\n");
            sb.append("描述: ").append(skill.getDescription()).append("\n\n");
        }

        return sb.toString();
    }

    @Tool(name = "getSkill",
          description = "获取指定 Skill 的完整内容。当需要执行某个 Skill 时调用此工具获取详细指令。")
    public String getSkill(
            @ToolParam(description = "Skill 名称") String skillName) {

        log.info("调用工具: getSkill, skillName={}", skillName);

        SkillDefinition skill = skillLoader.getSkill(skillName);

        if (skill == null) {
            return "未找到 Skill: " + skillName + "。请使用 listSkills 查看所有可用 Skills。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【").append(skill.getName()).append("】\n\n");
        sb.append("描述: ").append(skill.getDescription()).append("\n\n");
        sb.append("--- Skill 指令 ---\n\n");
        sb.append(skill.getContent());

        return sb.toString();
    }

    @Tool(name = "findSkill",
          description = "根据关键词查找匹配的 Skills。当不确定 Skill 名称时使用。")
    public String findSkill(
            @ToolParam(description = "搜索关键词") String keyword) {

        log.info("调用工具: findSkill, keyword={}", keyword);

        List<SkillDefinition> skills = skillLoader.findMatchingSkills(keyword);

        if (skills.isEmpty()) {
            return "没有找到匹配的 Skills。关键词: " + keyword;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(skills.size()).append(" 个匹配的 Skills：\n\n");

        for (SkillDefinition skill : skills) {
            sb.append("• ").append(skill.getName())
              .append(": ").append(skill.getDescription()).append("\n");
        }

        return sb.toString();
    }

    @Tool(name = "reloadSkills",
          description = "重新加载所有 Skills。当添加或修改了 SKILL.md 文件后调用。")
    public String reloadSkills() {
        log.info("调用工具: reloadSkills");

        skillLoader.reload();
        int count = skillLoader.getAllSkills().size();

        return "已重新加载 Skills，当前共有 " + count + " 个可用 Skills。";
    }
}
