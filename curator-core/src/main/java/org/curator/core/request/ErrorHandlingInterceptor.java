package org.curator.core.request;

import org.apache.log4j.Logger;
import org.curator.common.configuration.CuratorInterceptors;
import org.curator.common.exceptions.CuratorException;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Response;

@Interceptor
@CuratorInterceptors
public class ErrorHandlingInterceptor {

    private static final Logger _log = Logger.getLogger(ErrorHandlingInterceptor.class);

    @AroundInvoke
    public Object inject(InvocationContext invocationContext) throws Exception {
        try {
            return invocationContext.proceed();

        } catch (CuratorRequestException t) {
            throw t;
        } catch (CuratorException t) {
            throw new CuratorRequestException(Response.Status.CONFLICT, t);
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            _log.error(t);
            throw new CuratorRequestException(invocationContext.getMethod().getName(), t);
        }
    }

}
