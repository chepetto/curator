package org.curator.core.request;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class CuratorRequestException extends WebApplicationException {

    private Response.Status status;
    private String message;

    public CuratorRequestException(Response.Status status, String message) {
        super(status);
        this.status = status;
        this.message = message;
    }

    public CuratorRequestException(String message, Throwable throwable) {
        this(Response.Status.INTERNAL_SERVER_ERROR, throwable.getMessage());
    }

    public CuratorRequestException(Response.Status status, Throwable throwable) {
        this(status, throwable.getMessage());
    }

    public Response.Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

//    public CuratorRequestException(Response.Status status, String message) {
//        super(message);
//        this.status = status;
//    }
//
//    public CuratorRequestException(CuratorStatus status, String message, Throwable cause) {
//        super(message, cause);
//        this.status = status;
//    }
//
//    public CuratorRequestException(CuratorStatus status, Throwable cause) {
//        super(cause);
//        this.status = status;
//    }
//
//
//    public CuratorRequestException(String message, Throwable cause) {
//        super(message, cause);
//    }
//
//    public CuratorStatus getStatus() {
//        return status;
//    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getStatus());
        stringBuilder.append(": ");
        stringBuilder.append(this.getMessage());
        return stringBuilder.toString();
    }
}
