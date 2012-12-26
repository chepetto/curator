package org.curator.common.cache;

import org.apache.log4j.Logger;
import org.curator.common.configuration.CuratorInterceptors;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         10:08, 09.03.12
 */
@Interceptor
@CuratorInterceptors
public class MethodCacheInterceptor {

    private static final Logger _log = Logger.getLogger(MethodCacheInterceptor.class);

    @Inject
    private CuratorCacheBean<Object> cache;

    @AroundInvoke
    public Object injectCaching(InvocationContext invocationContext) throws Exception {
        if (invocationContext.getMethod().isAnnotationPresent(MethodCache.class)) {
            int hashCode = 0;
            for (Object param : invocationContext.getParameters()) {
                if (param != null) {
                     hashCode = 31 * hashCode + param.hashCode();
                }
            }
            String key = invocationContext.getTarget().getClass().getSimpleName() + "_" + invocationContext.getMethod().getName() + "_" + hashCode;
            if (cache.contains(key, CacheName.METHODS)) {
                if (_log.isTraceEnabled()) {
                    _log.trace("Found cache entry for " + key);
                }
                return cache.get(key, CacheName.METHODS);
            } else {
                Object returnObject =  invocationContext.proceed();
                if (_log.isTraceEnabled()) {
                    _log.trace("Add entry for " + key + " into cache");
                }
                cache.put(key, returnObject, CacheName.METHODS);
                return returnObject;
            }
        } else {
            return invocationContext.proceed();
        }

    }
}
