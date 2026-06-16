package cn.arrebyte.ai.mcpclient.controller;

import cn.arrebyte.ai.mcpclient.entity.PdfUploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {
    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    private final RestClient restClient;

    public PdfController(@Value("${app.server.base-url:http://localhost:8021}") String serverBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(serverBaseUrl)
                .build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PdfUploadResponse upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new PdfUploadResponse(false, null, null, "请选择要上传的 PDF 文件");
        }

        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            String contentType = file.getContentType();
            fileHeaders.setContentType(MediaType.parseMediaType(
                    contentType == null || contentType.isBlank() ? MediaType.APPLICATION_PDF_VALUE : contentType
            ));
            fileHeaders.setContentDisposition(ContentDisposition.formData()
                    .name("file")
                    .filename(file.getOriginalFilename())
                    .build());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new HttpEntity<>(resource, fileHeaders));

            String chatId = restClient.post()
                    .uri("/api/pdf/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return new PdfUploadResponse(
                    true,
                    chatId,
                    file.getOriginalFilename(),
                    "PDF 上传成功，当前会话已绑定到该文档。"
            );
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("代理上传 PDF 失败, status={}, body={}", e.getStatusCode(), responseBody);
            return new PdfUploadResponse(
                    false,
                    null,
                    file.getOriginalFilename(),
                    responseBody == null || responseBody.isBlank() ? "PDF 上传失败：" + e.getMessage() : responseBody
            );
        } catch (Exception e) {
            log.error("代理上传 PDF 失败", e);
            return new PdfUploadResponse(
                    false,
                    null,
                    file.getOriginalFilename(),
                    "PDF 上传失败：" + e.getMessage()
            );
        }
    }
}
