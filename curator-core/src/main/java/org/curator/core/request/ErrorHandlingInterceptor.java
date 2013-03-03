package org.curator.core.request;

import org.apache.log4j.Logger;
import org.curator.common.configuration.CuratorInterceptors;
import org.curator.common.exceptions.CuratorException;
import org.curator.core.services.Response;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.lang.reflect.Method;

@Interceptor
@CuratorInterceptors
public class ErrorHandlingInterceptor {

    private static final Logger _log = Logger.getLogger(ErrorHandlingInterceptor.class);

    @AroundInvoke
    public Object handleErrors(InvocationContext invocationContext) throws Exception {

        Method method = invocationContext.getMethod();

        if (method.isAnnotationPresent(GET.class) ||
                method.isAnnotationPresent(POST.class) ||
                method.isAnnotationPresent(PUT.class) ||
                method.isAnnotationPresent(DELETE.class)
                ) {
            try {
                // todo
                return invocationContext.proceed();
            } catch (CuratorRequestException t) {
                return Response.ok(t);
            } catch (CuratorException t) {
                return Response.ok(new CuratorRequestException(javax.ws.rs.core.Response.Status.CONFLICT, t));
            } catch (IllegalArgumentException t) {
                return Response.ok(new CuratorRequestException(javax.ws.rs.core.Response.Status.PRECONDITION_FAILED, t));
            } catch (Throwable t) {
                if (t.getCause() instanceof CuratorRequestException) {
                    return Response.ok(new CuratorRequestException(javax.ws.rs.core.Response.Status.CONFLICT, t.getCause()));
                } else {
                    _log.error(t);
                    return Response.ok(new CuratorRequestException("Error in method " + invocationContext.getMethod().getName(), t));
                }
            }
        } else {
            return invocationContext.proceed();
        }
    }

}
