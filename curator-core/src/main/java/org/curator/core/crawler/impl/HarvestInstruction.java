package org.curator.core.crawler.impl;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.MediaType;

import java.net.URL;

public interface HarvestInstruction {
    URL getUrl();

    String getId();

    String getMediaType();
}
