package cn.arrebyte.ai.mcpserver.skill;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillDefinition {

    private String name;

    private String description;

    private String content;

    private String path;
}
