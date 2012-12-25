package org.curator.core.crawler.impl;

import javax.persistence.*;
import java.net.URL;

@Entity(name="Request")
@Table(name="Request")
@NamedQueries({
        @NamedQuery(
                name = Request.BY_URL,
                query= "SELECT r from Request r where r.url=:URL"

        )
})
public class Request {

    public static final String BY_URL = "BY_URL";

    @Id
    private URL url;

    private Long timestamp;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
