package org.curator.core.crawler.impl;

import java.net.URL;

public interface HarvestInstruction {
    URL getUrl();

    String getId();

}
