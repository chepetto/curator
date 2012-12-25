package org.curator.core.dao;

import org.curator.common.exceptions.CuratorStatus;

import javax.persistence.RollbackException;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         12:06, 12.07.12
 */
public class CuratorRollbackException extends RollbackException {

    private CuratorStatus status;

    public CuratorRollbackException(CuratorStatus status, String message) {
        super(message);
        this.status = status;
    }

    public CuratorRollbackException(CuratorStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public CuratorRollbackException(CuratorStatus status, Throwable cause) {
        super(cause);
        this.status = status;
    }


    public CuratorRollbackException(String message, Throwable cause) {
        super(message, cause);
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
