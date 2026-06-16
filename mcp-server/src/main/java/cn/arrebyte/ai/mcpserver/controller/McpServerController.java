package cn.arrebyte.ai.mcpserver.controller;

import cn.arrebyte.ai.mcpserver.repository.LocalPdfFileRepository;
import cn.arrebyte.ai.mcpserver.tool.PdfQueryTool;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author wch
 * @description
 * @date 2026/6/16 16:20
 */
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class McpServerController {
    public static final Logger log = LoggerFactory.getLogger(PdfQueryTool.class);

    private final LocalPdfFileRepository pdfRepository;

    /**
     * 上传PDF文件
     * @param file PDF文件
     * @return 会话ID，用于后续查询
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        File tempFile = null;
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("请选择要上传的 PDF 文件");
            }

            // 1. 检查文件类型
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            boolean isPdf = "application/pdf".equalsIgnoreCase(contentType)
                    || (StringUtils.hasText(originalFilename) && originalFilename.toLowerCase().endsWith(".pdf"));
            if (!isPdf) {
                return ResponseEntity.badRequest().body("请上传PDF文件");
            }

            // 2. 生成会话ID
            String chatId = UUID.randomUUID().toString();

            // 3. 保存到临时文件
            tempFile = File.createTempFile("pdf-", ".pdf");
            file.transferTo(tempFile);

            // 4. 保存到仓库（会自动处理向量化）
            boolean saved = pdfRepository.saveFile(chatId, new FileSystemResource(tempFile));

            if (saved) {
                log.info("PDF上传成功: chatId={}, filename={}", chatId, file.getOriginalFilename());
                return ResponseEntity.ok(chatId);
            } else {
                return ResponseEntity.internalServerError().body("文件保存失败");
            }

        } catch (IOException e) {
            log.error("PDF上传失败", e);
            return ResponseEntity.internalServerError().body("上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("PDF入库失败", e);
            return ResponseEntity.internalServerError().body("PDF入库失败: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
            }
        }
    }

    /**
     * 获取上传的文件信息
     */
    @GetMapping("/info/{chatId}")
    public ResponseEntity<String> getFileInfo(@PathVariable String chatId) {
        FileSystemResource resource = (FileSystemResource) pdfRepository.getFileSource(chatId);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("文件: " + resource.getFilename());
    }
}
