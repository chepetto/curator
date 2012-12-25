package org.curator.core.crawler.impl;

public class CrawlerResult {

    public static final int OK = 0;
    public static final int ERROR = 1;

    private int status;
    private HarvestInstruction instruction;
    private String response;
    private String contentType;

    public HarvestInstruction getInstruction() {
        return instruction;
    }

    public void setInstruction(HarvestInstruction instruction) {
        this.instruction = instruction;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
