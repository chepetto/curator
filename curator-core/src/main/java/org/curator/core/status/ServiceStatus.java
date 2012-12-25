package org.curator.core.status;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         21:05, 25.07.12
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServiceStatus {

    public enum Status {
        OK,
        ERROR
    }

    private String name;
    private String errorMessage;
    private Long duration;
    private Status status;


    private ServiceStatus() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static ServiceStatus ok(String name, long duration) {
        ServiceStatus serviceStatus = new ServiceStatus();
        serviceStatus.setName(name);
        serviceStatus.setDuration(duration);
        serviceStatus.setStatus(Status.OK);

        return serviceStatus;
    }

    public static ServiceStatus notAvailable(String name, long duration, Throwable t) {
        ServiceStatus serviceStatus = new ServiceStatus();
        serviceStatus.setName(name);
        serviceStatus.setDuration(duration);
        serviceStatus.setErrorMessage(t.getMessage());
        serviceStatus.setStatus(Status.ERROR);

        return serviceStatus;
    }

    @Override
    public String toString() {
        return "ServiceStatus{" +
                "name='" + name + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", duration=" + duration +
                ", status=" + status +
                '}';
    }
}
