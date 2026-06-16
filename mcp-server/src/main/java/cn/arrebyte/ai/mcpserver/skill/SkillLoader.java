package cn.arrebyte.ai.mcpserver.skill;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SkillLoader {

    /**
     解析：

     ┌────────────────────────────────┬────────────────────────────────────────────────────────┐
     │部分                            │含义                                                    │
     ├────────────────────────────────┼────────────────────────────────────────────────────────┤
     │^---\s*\n                       │开头的 --- 分隔符                                       │
     ├────────────────────────────────┼────────────────────────────────────────────────────────┤
     │([\s\S]*?)                      │捕获组1：YAML 元数据内容（非贪婪）                      │
     ├────────────────────────────────┼────────────────────────────────────────────────────────┤
     │\n---\s*\n                      │结束的 --- 分隔符                                       │ ▄
     ├────────────────────────────────┼────────────────────────────────────────────────────────┤ █
     │([\s\S]*)                       │捕获组2：正文内容                                       │ █
     ├────────────────────────────────┼────────────────────────────────────────────────────────┤ █
     │$                               │字符串结束                                              │ █
     └────────────────────────────────┴────────────────────────────────────────────────────────┘ █
     █
     使用时：                                                                                    █
     - matcher.group(1) → 获取 YAML 配置                                                         █
     - matcher.group(2) → 获取正文内容
     */
    private static final Pattern YAML_FRONT_MATTER = Pattern.compile(
            "^---\\s*\\n([\\s\\S]*?)\\n---\\s*\\n([\\s\\S]*)$"
    );

    @Value("${app.skills.path:.opencode/skills}")
    private String skillsPath;

    private final Map<String, SkillDefinition> skills = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadSkills() {
        Path skillsDir = findSkillsDirectory();

        if (skillsDir == null) {
            log.warn("Skills 目录不存在，尝试过的路径: .opencode/skills, springai-mcp-server/.opencode/skills");
            return;
        }

        try {
            Files.list(skillsDir)
                    .filter(Files::isDirectory)
                    .forEach(this::loadSkillFromDirectory);
        } catch (IOException e) {
            log.error("读取 Skills 目录失败", e);
        }

        log.info("已加载 {} 个 Skills from {}", skills.size(), skillsDir.toAbsolutePath());
    }

    private Path findSkillsDirectory() {
        String[] searchPaths = {
            skillsPath,
            "springai-mcp-server/.opencode/skills",
            ".opencode/skills"
        };

        for (String path : searchPaths) {
            Path skillsDir = Paths.get(path);
            if (Files.exists(skillsDir)) {
                log.debug("找到 Skills 目录: {}", skillsDir.toAbsolutePath());
                return skillsDir;
            }
        }
        return null;
    }

    private void loadSkillFromDirectory(Path skillDir) {
        Path skillFile = skillDir.resolve("SKILL.md");

        if (!Files.exists(skillFile)) {
            log.debug("目录中没有 SKILL.md: {}", skillDir);
            return;
        }

        try {
            String content = Files.readString(skillFile);
            SkillDefinition skill = parseSkill(content, skillFile.toString());

            if (skill != null && skill.getName() != null) {
                skills.put(skill.getName(), skill);
                log.info("加载 Skill: {} from {}", skill.getName(), skillFile);
            }
        } catch (IOException e) {
            log.error("读取 Skill 文件失败: {}", skillFile, e);
        }
    }

    private SkillDefinition parseSkill(String content, String path) {
        Matcher matcher = YAML_FRONT_MATTER.matcher(content);

        if (!matcher.matches()) {
            log.warn("Skill 文件格式错误，缺少 YAML front matter: {}", path);
            return null;
        }

        String yamlPart = matcher.group(1);
        String bodyContent = matcher.group(2);

        Map<String, String> metadata = parseYaml(yamlPart);

        return SkillDefinition.builder()
                .name(metadata.get("name"))
                .description(metadata.get("description"))
                .content(bodyContent.trim())
                .path(path)
                .build();
    }

    private Map<String, String> parseYaml(String yaml) {
        Map<String, String> result = new HashMap<>();

        String[] lines = yaml.split("\n");
        String currentKey = null;
        StringBuilder currentValue = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("  ") && currentKey != null) {
                currentValue.append(line.trim()).append(" ");
            } else if (line.contains(":")) {
                if (currentKey != null) {
                    result.put(currentKey, currentValue.toString().trim());
                }
                int colonIndex = line.indexOf(":");
                currentKey = line.substring(0, colonIndex).trim();
                currentValue = new StringBuilder();
                String value = line.substring(colonIndex + 1).trim();
                if (value.startsWith("|")) {
                } else if (!value.isEmpty()) {
                    currentValue.append(value);
                }
            }
        }

        if (currentKey != null) {
            result.put(currentKey, currentValue.toString().trim());
        }

        return result;
    }

    public SkillDefinition getSkill(String name) {
        return skills.get(name);
    }

    public List<SkillDefinition> getAllSkills() {
        return new ArrayList<>(skills.values());
    }

    public List<SkillDefinition> findMatchingSkills(String query) {
        String lowerQuery = query.toLowerCase();

        return skills.values().stream()
                .filter(skill -> {
                    if (skill.getName() != null && skill.getName().toLowerCase().contains(lowerQuery)) {
                        return true;
                    }
                    if (skill.getDescription() != null && skill.getDescription().toLowerCase().contains(lowerQuery)) {
                        return true;
                    }
                    return false;
                })
                .toList();
    }

    public void reload() {
        skills.clear();
        loadSkills();
    }
}
