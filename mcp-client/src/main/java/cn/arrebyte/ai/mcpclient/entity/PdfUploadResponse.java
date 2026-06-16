package cn.arrebyte.ai.mcpclient.entity;

public class PdfUploadResponse {
    private boolean success;
    private String sessionId;
    private String filename;
    private String message;

    public PdfUploadResponse() {
    }

    public PdfUploadResponse(boolean success, String sessionId, String filename, String message) {
        this.success = success;
        this.sessionId = sessionId;
        this.filename = filename;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
