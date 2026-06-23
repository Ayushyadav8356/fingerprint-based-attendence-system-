package com.attendance.dto;

public class IotScanRequest {
    private Integer fingerprintId;
    private String rfidUid;

    public IotScanRequest() {}

    public IotScanRequest(Integer fingerprintId, String rfidUid) {
        this.fingerprintId = fingerprintId;
        this.rfidUid = rfidUid;
    }

    public Integer getFingerprintId() { return fingerprintId; }
    public void setFingerprintId(Integer fingerprintId) { this.fingerprintId = fingerprintId; }

    public String getRfidUid() { return rfidUid; }
    public void setRfidUid(String rfidUid) { this.rfidUid = rfidUid; }
}
