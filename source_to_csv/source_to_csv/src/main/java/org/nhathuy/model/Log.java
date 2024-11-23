package org.nhathuy.model;

import java.time.LocalDateTime;

public class Log {
    private int id;
    private LocalDateTime trackingDateTime;
    private String source;
    private int connectStatus;
    private String destination;
    private String phase;
    private String result;
    private String detail;
    private boolean isDelete;

    public Log() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Log(int id, LocalDateTime trackingDateTime, String source, int connectStatus, String destination,
               String phase, String result, String detail, boolean isDelete) {
        super();
        this.id = id;
        this.trackingDateTime = trackingDateTime;
        this.source = source;
        this.connectStatus = connectStatus;
        this.destination = destination;
        this.phase = phase;
        this.result = result;
        this.detail = detail;
        this.isDelete = isDelete;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTrackingDateTime() {
        return trackingDateTime;
    }

    public void setTrackingDateTime(LocalDateTime trackingDateTime) {
        this.trackingDateTime = trackingDateTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(int connectStatus) {
        this.connectStatus = connectStatus;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    public String toString() {
        return "Log [id=" + id + ", trackingDateTime=" + trackingDateTime + ", source=" + source + ", connectStatus="
                + connectStatus + ", destination=" + destination + ", phase=" + phase + ", result=" + result
                + ", detail=" + detail + ", isDelete=" + isDelete + "]";
    }
}
