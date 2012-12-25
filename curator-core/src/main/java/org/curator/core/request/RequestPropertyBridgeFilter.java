package org.curator.core.request;

import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 *         09:16, 10.05.12
 */
@WebFilter(filterName = "AuthenticationFilter")
public class RequestPropertyBridgeFilter implements Filter {

    private static final Logger _log = Logger.getLogger(RequestPropertyBridgeFilter.class);

    @Inject
    private RequestPropertyHolder requestPropertyHolder;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        _log.info("Initialized");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        Long requestCounter = (Long) httpServletRequest.getSession().getAttribute(Property.REQUEST_COUNTER);
        if (requestCounter == null) {
            requestCounter = 0L;
        }
        if (_log.isTraceEnabled()) {
            _log.trace(httpServletRequest.getSession().getId() + "-" + requestCounter +
                    ": Incoming request from " + httpServletRequest.getRemoteHost() +
                    " (" + httpServletRequest.getRemoteAddr() + ")");
            _log.trace(httpServletRequest.getSession().getId() + "-" + requestCounter +
                    ": Calling " + httpServletRequest.getMethod() + " " + httpServletRequest.getRequestURI() +
                    " (" + httpServletRequest.getParameterMap() + ")");
        }

        httpServletRequest.getSession().setAttribute(Property.REQUEST_COUNTER, ++requestCounter);
        httpServletRequest.getSession().setAttribute(Property.JSESSION, httpServletRequest.getSession().getId());
        httpServletRequest.getSession().setAttribute(Property.ADDRESS_LOCAL, httpServletRequest.getLocalAddr());
        httpServletRequest.getSession().setAttribute(Property.ADDRESS_REMOTE, httpServletRequest.getRemoteAddr());
        httpServletRequest.getSession().setAttribute(Property.HOST_LOCAL, httpServletRequest.getLocalName());
        httpServletRequest.getSession().setAttribute(Property.HOST_REMOTE, httpServletRequest.getRemoteHost());
        httpServletRequest.getSession().setAttribute(Property.LOCALE, httpServletRequest.getLocale());
        httpServletRequest.getSession().setAttribute(Property.LOCALES, httpServletRequest.getLocales());

        HttpSession session = httpServletRequest.getSession();
        Enumeration<String> attributes = session.getAttributeNames();

        Map<String, Object> attributeMap = new HashMap<String, Object>();
        while (attributes.hasMoreElements()) {
            String name = attributes.nextElement();
            attributeMap.put(name, session.getAttribute(name));
        }
        requestPropertyHolder.setAttributes(attributeMap);
        filterChain.doFilter(servletRequest, servletResponse);
        requestPropertyHolder.removeAttributes();
    }

    @Override
    public void destroy() {
        _log.info("Destroyed");
    }
}
