package org.curator.core.crawler.impl;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.curator.common.configuration.Configuration;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class HttpClientBean {

    private HttpClient httpClient;
    private boolean initialized = false;

    @PostConstruct
    public void onInit() {
        if(initialized) {
            throw new IllegalAccessError("onInit should not be called twice");
        }
        initialized = true;

        httpClient = new HttpClient();

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setMaxTotalConnections(Configuration.getIntValue(Configuration.HTTP_CLIENT_LIMIT_MAX_CONNECTIONS, 4));

        httpClient.setHttpConnectionManager(connectionManager);
        HttpClientParams params = httpClient.getParams();
        params.setSoTimeout(Configuration.getIntValue(Configuration.HTTP_CLIENT_SO_TIMEOUT, 20000));
        params.setHttpElementCharset("UTF-8");
        params.setUriCharset("UTF-8");
        params.setContentCharset("UTF-8");
        params.setConnectionManagerTimeout(Configuration.getIntValue(Configuration.HTTP_CLIENT_CONNECTION_MANAGER_TIMEOUT, 20000));
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
