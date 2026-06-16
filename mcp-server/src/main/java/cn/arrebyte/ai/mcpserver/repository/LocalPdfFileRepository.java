package cn.arrebyte.ai.mcpserver.repository;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author wch
 * @description
 * @date 2026/6/15 17:37
 */
@Component
public class LocalPdfFileRepository implements PdfFileCenterRepository, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @jakarta.annotation.Resource
    private VectorStore vectorStore;

    private final Properties chatFiles = new Properties();

    @Override
    public boolean saveFile(String chatId, Resource resource) {
        String filename = resource.getFilename();
        File target = new File(Objects.requireNonNull(filename));
        if (!target.exists()) {
            try {
                Files.copy(resource.getInputStream(), target.toPath());
            } catch (IOException e) {
                logger.error("Failed to save PDF resource.", e);
                return false;
            }
        }
        chatFiles.put(chatId, filename);
        try {
            //写入向量数据库
            writeToVectorStore(chatId, resource);
            return true;
        } catch (Exception e) {
            chatFiles.remove(chatId);
            logger.error("Failed to write PDF to vector store. chatId={}", chatId, e);
            throw new IllegalStateException("PDF 向量化失败: " + e.getMessage(), e);
        }
    }

    private void writeToVectorStore(String chatId, Resource resource) {
        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(resource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build());
        List<Document> documents = pagePdfDocumentReader.read();
        for (Document document : documents) {
            document.getMetadata().put("chatId", chatId);
        }
        vectorStore.add(documents);
    }

    @Override
    public Resource getFileSource(String chatId) {
        if (chatFiles.isEmpty()){
            return null;
        }
        return new  FileSystemResource(chatFiles.getProperty(chatId));
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }
    public void init(){
        FileSystemResource pdfResource = new FileSystemResource("chat-pdf.properties");
        if (pdfResource.exists()) {
            try {
                // 加载映射关系
                chatFiles.load(new BufferedReader(new InputStreamReader(pdfResource.getInputStream(), StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        //加载向量库
        FileSystemResource vectorResource = new FileSystemResource("chat-pdf.json");
        if (vectorResource.exists()) {
            SimpleVectorStore simpleVectorStore = (SimpleVectorStore) vectorStore;
            simpleVectorStore.load(vectorResource);
        }
    }
    @PreDestroy
    private void persistent() {
        try {
            chatFiles.store(new FileWriter("chat-pdf.properties"), LocalDateTime.now().toString());
            SimpleVectorStore simpleVectorStore = (SimpleVectorStore) vectorStore;
            simpleVectorStore.save(new File("chat-pdf.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
