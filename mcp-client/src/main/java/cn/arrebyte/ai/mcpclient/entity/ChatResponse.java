package cn.arrebyte.ai.mcpclient.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
    private boolean success;
    private String message;
    private String errorCode;  // 可选
}
