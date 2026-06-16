package cn.arrebyte.ai.mcpserver.repository;

import org.springframework.core.io.Resource;

/**
 * @author wch
 * @description
 * @date 2026/6/15 17:29
 */
public interface PdfFileCenterRepository {

    boolean saveFile(String chatId, Resource resource);

    Resource getFileSource(String chatId);
}
