package org.curator.core.request;

import org.apache.log4j.Logger;
import org.curator.common.configuration.CuratorInterceptors;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.exceptions.CuratorStatus;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Field;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         11:01, 29.02.12
 */
@Interceptor
@CuratorInterceptors
public class RequestPropertyInterceptor {

    private static final Logger _log = Logger.getLogger(RequestPropertyInterceptor.class);

    @Inject
    private RequestPropertyHolder requestPropertyHolder;

    @AroundInvoke
    public Object inject(InvocationContext invocationContext) throws Exception {
        try {
            _inject(invocationContext.getMethod().getDeclaringClass(), invocationContext.getTarget());
        } catch (Throwable t) {
            _log.fatal("Configuration for class " + invocationContext.getTarget().getClass().getName() + "failed: " + t.getMessage());
            throw new CuratorException(CuratorStatus.REQUEST_ERROR, "Configuration for class " + invocationContext.getTarget().getClass().getName() + "failed: " + t.getMessage());
        }
        return invocationContext.proceed();
    }

    public void _inject(Class targetClass, Object targetObject) throws Exception {
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(RequestProperty.class)) {
                RequestProperty property = field.getAnnotation(RequestProperty.class);
                Object attribute = requestPropertyHolder.getAttribute(property.value());
                if (property.mandatory() && (attribute == null))
                    throw new CuratorException(CuratorStatus.REQUEST_ERROR, "Request property not found: " + property.value());
                try {
                    field.setAccessible(true);
                    field.set(targetObject, attribute);
                } catch (Throwable e) {
                    if (property.mandatory()) {
                        throw new CuratorException(CuratorStatus.REQUEST_ERROR, "Request property not accessible: " + property.value());
                    }
                }
            }
        }
    }
}
