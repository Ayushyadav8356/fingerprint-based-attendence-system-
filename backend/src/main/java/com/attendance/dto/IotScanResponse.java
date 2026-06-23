package com.attendance.dto;

public class IotScanResponse {
    private String status; // 'success' or 'error'
    private String name;
    private String message;

    public IotScanResponse() {}

    public IotScanResponse(String status, String name, String message) {
        this.status = status;
        this.name = name;
        this.message = message;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
