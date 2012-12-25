package org.curator.common.exceptions;

public class CuratorException extends Exception {

    private CuratorStatus status;

    public CuratorException(String message) {
        super(message);
    }

    public CuratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CuratorException(CuratorStatus status, String message) {
        super(message);
        this.status = status;
    }

    public CuratorException(CuratorStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public CuratorException(CuratorStatus status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public CuratorStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getStatus());
        stringBuilder.append(": ");
        stringBuilder.append(this.getMessage());
        return stringBuilder.toString();
    }
}
