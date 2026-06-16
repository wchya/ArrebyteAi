package cn.arrebyte.ai.mcpserver.tool;

import cn.arrebyte.ai.mcpserver.repository.PdfFileCenterRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PdfQueryTool {
    public static final Logger log = LoggerFactory.getLogger(PdfQueryTool.class);

    private final VectorStore vectorStore;

    private final PdfFileCenterRepository fileRepository;

    @Tool(name = "queryPdf", description = "从上传的pdf文档中查询相关信息，支持自然语言查询")
    public String queryPdf(
            @ToolParam(description = "会话ID，标识上传的PDF文件") String chatId,
            @ToolParam(description = "要查询的问题") String prompt) {

        log.info("PdfQueryTool queryPdf,chatId => {},prompt => {}", chatId, prompt);

        try {
            //1.检查是否有对应的文件
            Resource pdfResource = fileRepository.getFileSource(chatId);
            if (pdfResource == null || !pdfResource.exists()) {
                return "未找到会话 " + chatId + " 对应的PDF文件，请先上传文件。";
            }

            //2.从向量库检索相关文档片段
            List<Document> documentList = null;
            double[] thresholds = {0.7,0.6, 0.5, 0.4, 0.3}; // 从高到低尝试

            for (double threshold : thresholds) {
                SearchRequest searchRequest = SearchRequest.builder()
                        .query(prompt)
                        .topK(10)
                        .similarityThreshold(threshold)
                        .filterExpression("chatId == '" + chatId + "'")
                        .build();

                documentList = vectorStore.similaritySearch(searchRequest);
                log.info("阈值 {} 查询结果数: {}", threshold, documentList.size());

                if (!CollectionUtils.isEmpty(documentList) && documentList.size() >= 3) {
                    log.info("使用阈值 {} 找到 {} 个结果", threshold, documentList.size());
                    break;
                }
            }

            //如果动态阈值还没结果，强制返回topK（不设阈值）
            if (CollectionUtils.isEmpty(documentList)) {
                log.info("阈值查询无结果，强制返回topK");
                SearchRequest searchRequest = SearchRequest.builder()
                        .query(prompt)
                        .topK(5)
                        .filterExpression("chatId == '" + chatId + "'")
                        .build();
                documentList = vectorStore.similaritySearch(searchRequest);
            }

            // 如果还是没有结果
            if (CollectionUtils.isEmpty(documentList)) {
                return String.format("在PDF中没有找到与问题相关的内容,会话ID为:%s,查询内容为：%s。", chatId, prompt);
            }

            //3.整理搜索结果
            String result = documentList.stream()
                    .filter(Objects::nonNull)
                    .filter(document -> {
                        Map<String, Object> metadata = document.getMetadata();
                        if (metadata.containsKey("chatId")) {
                            return metadata.get("chatId").equals(chatId);
                        }
                        return false;
                    })
                    .map(document -> String.format("【相关片段】 \n%s \n(相似度：%.2f)",
                            document.getText(), document.getScore())
                    ).collect(Collectors.joining("\n\n"));

            return String.format("根据PDF文档，找到以下相关信息：\n\n %s", result);

        } catch (Exception e) {
            log.error("查询PDF失败", e);
            return "查询PDF时发生错误: " + e.getMessage();
        }
    }

    /**
     * 工具2: 获取PDF文档摘要
     */
    @Tool(name = "summarizePdf",
            description = "获取上传PDF文档的内容摘要")
    public String summarizePdf(
            @ToolParam(description = "会话ID，标识上传的PDF文件") String chatId) {

        log.info("生成PDF摘要: chatId={}", chatId);

        try {
            // 获取所有文档片段
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("主要内容摘要")
                    .topK(10)
                    .build();
            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            if (documents.isEmpty()) {
                return "无法生成摘要，文档内容为空。";
            }

            // 简单合并前几个片段作为摘要
            String summary = documents.stream()
                    .filter(Objects::nonNull)
                    .filter(document -> {
                        Map<String, Object> metadata = document.getMetadata();
                        if (metadata.containsKey("chatId")) {
                            return metadata.get("chatId").equals(chatId);
                        }
                        return false;
                    })
                    .map(Document::getText)
                    .limit(3)  // 取前3个片段
                    .collect(Collectors.joining("\n"));

            String result = "文档摘要：\n" + summary;
            log.info("生成摘要成功: {}", result);
            return result;
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            return "生成摘要失败: " + e.getMessage();
        }
    }
}
