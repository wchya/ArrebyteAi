package cn.arrebyte.ai.mcpserver.config;

import cn.arrebyte.ai.mcpserver.tool.CityToolService;
import cn.arrebyte.ai.mcpserver.tool.PdfQueryTool;
import cn.arrebyte.ai.mcpserver.tool.SkillTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wch
 * @description
 * @date 2026/6/15 16:32
 */
@Configuration
public class CityInfToolCallBackProviderConfig {

    @Bean
    public ToolCallbackProvider cityToolCallbackProvider(CityToolService cityToolService){
        return MethodToolCallbackProvider.builder().toolObjects(cityToolService).build();
    }
    @Bean
    public ToolCallbackProvider pdfTools(PdfQueryTool pdfQueryTool){
        return MethodToolCallbackProvider.builder().toolObjects(pdfQueryTool).build();
    }

    @Bean
    public ToolCallbackProvider skillTools(SkillTool skillTool){
        return MethodToolCallbackProvider.builder().toolObjects(skillTool).build();
    }
}
