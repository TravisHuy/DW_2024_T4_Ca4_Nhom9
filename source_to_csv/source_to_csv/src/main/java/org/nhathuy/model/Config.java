package org.nhathuy.model;

import java.time.LocalDateTime;

public class Config {
    public static final String AUTO = "GET_DATA_AUTO";
    private int id;
    private String phase;
    private String source;
    private String sourceName;
    private String area;
    private String pathToSave;
    private String fileNameFormat;
    private String fileType;
    private LocalDateTime timeGetData;
    private int interval;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String description;
    private int status;

    public Config() {
        super();
    }

    public Config(int id, String phase, String source, String sourceName, String area, String pathToSave, String fileNameFormat, String fileType, LocalDateTime timeGetData, int interval, LocalDateTime createDate, LocalDateTime updateDate, String description, int status) {
        super();
        this.id = id;
        this.phase = phase;
        this.source = source;
        this.sourceName = sourceName;
        this.area = area;
        this.pathToSave = pathToSave;
        this.fileNameFormat = fileNameFormat;
        this.fileType = fileType;
        this.timeGetData = timeGetData;
        this.interval = interval;
        this.createDate = createDate;
        this.updateDate = updateDate;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPathToSave() {
        return pathToSave;
    }

    public void setPathToSave(String pathToSave) {
        this.pathToSave = pathToSave;
    }

    public String getFileNameFormat() {
        return fileNameFormat;
    }

    public void setFileNameFormat(String fileNameFormat) {
        this.fileNameFormat = fileNameFormat;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getTimeGetData() {
        return timeGetData;
    }

    public void setTimeGetData(LocalDateTime timeGetData) {
        this.timeGetData = timeGetData;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Config{" +
                "id=" + id +
                ", phase='" + phase + '\'' +
                ", source='" + source + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", area='" + area + '\'' +
                ", pathToSave='" + pathToSave + '\'' +
                ", fileNameFormat='" + fileNameFormat + '\'' +
                ", fileType='" + fileType + '\'' +
                ", timeGetData=" + timeGetData +
                ", interval=" + interval +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
