package org.curator.core;

import org.apache.log4j.Logger;
import org.curator.common.configuration.Configuration;
import org.curator.common.configuration.ConfigurationProperty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

@Startup
@Singleton
@ApplicationScoped
public class StartUp implements Extension {

    private static final Logger LOGGER = Logger.getLogger(StartUp.class);

    @ConfigurationProperty(value = Configuration.VERSION, defaultValue = "<UNKNOWN>")
    private String version = null;

    @Inject
    private SeedHandler seedHandler;
    @Inject
    private FeedHandler feedHandler;

    private boolean initialized = false;

    public StartUp() {
        //
    }

    public String getVersion() {
        return version;
    }

    @PostConstruct
    public void onInit() {

        synchronized (Startup.class) {
            if (!initialized) {
                initialized = false;

                if (Configuration.getBooleanValue("retrieve.seeds.onstartup")) {
                    LOGGER.info("Trigger retrieve seeds/feeds");
                    //seedHandler.watchSeeds();
                    feedHandler.watchFeeds();
                }
            }
        }

    }

    @PreDestroy
    public void preDestroy() throws Exception {
        LOGGER.info("Destroying...");
    }

}
