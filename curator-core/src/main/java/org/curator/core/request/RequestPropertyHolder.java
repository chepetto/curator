package org.curator.core.request;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         09:19, 10.05.12
 */
@Startup
@Singleton
@ApplicationScoped
public class RequestPropertyHolder {

    private ThreadLocal<Map<String, Object>> requestAttributesThreadLocal = new ThreadLocal<Map<String, Object>>();

    public void setAttributes(Map<String, Object> attributes) {
        this.requestAttributesThreadLocal.set(attributes);
    }

    public void setAttribute(String key, Object value) {
        requestAttributesThreadLocal.get().put(key, value);
    }

    public Object getAttribute(String name) {
        return this.requestAttributesThreadLocal.get().get(name);
    }

    public void removeAttributes() {
        this.requestAttributesThreadLocal.get().clear();
    }
}
