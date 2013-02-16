package org.curator.core.services;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.exceptions.CuratorStatus;
import org.curator.common.service.CustomDateDeserializer;
import org.curator.common.service.CustomDateSerializer;

import java.util.Date;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Response {

    private Object result;

    private int statusCode;
    private String status;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date timestamp = new Date();

    private String errorMessage;

    private Response() {
        this.result = null;
        this.errorMessage = null;
        this.setStatus(CuratorStatus.OK);
    }

    private Response(Object result) {
        this.result = result;
        this.errorMessage = null;
        this.setStatus(CuratorStatus.OK);
    }

    private Response(Throwable e) {
        this.result = null;
        this.errorMessage = e.getMessage();
        if (e instanceof CuratorException) {
            this.setStatus(((CuratorException) e).getStatus());
        } else {
            this.setStatus(CuratorStatus.ERROR);
        }
    }

    private void setStatus(CuratorStatus status) {
        this.statusCode = status.getStatusCode();
        this.status = status.toString();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Object getResult() {
        return result;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static Response ok() {
        return new Response();
    }

    public static Response ok(Object result) {
        return new Response(result);
    }

    public static Response error(Throwable t) {
        return new Response(t);
    }
}
