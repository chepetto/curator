package org.curator.core.status;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.curator.common.service.CustomDateDeserializer;
import org.curator.common.service.CustomDateSerializer;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         21:18, 25.07.12
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class NodeStatus {

    private String nodeName;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date timestamp = new Date();

    private List<ServiceStatus> serviceStatusList = new LinkedList<ServiceStatus>();

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public List<ServiceStatus> getServiceStatusList() {
        return serviceStatusList;
    }

    public void setServiceStatusList(List<ServiceStatus> serviceStatusList) {
        this.serviceStatusList = serviceStatusList;
    }

    @Override
    public String toString() {
        return "NodeStatus{" +
                "nodeName='" + nodeName + '\'' +
                ", timestamp=" + timestamp +
                ", serviceStatusList=" + serviceStatusList +
                '}';
    }
}
