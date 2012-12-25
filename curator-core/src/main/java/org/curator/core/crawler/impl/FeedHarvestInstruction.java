package org.curator.core.crawler.impl;

import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Feed;
import org.curator.common.model.MediaType;

import java.net.MalformedURLException;
import java.net.URL;

public class FeedHarvestInstruction implements HarvestInstruction {

    private static final Logger LOGGER = Logger.getLogger(FeedHarvestInstruction.class);

    private Feed feed;

    public FeedHarvestInstruction(Feed feed) {
        this.feed = feed;
    }

    public FeedHarvestInstruction() {
        //
    }

    public Feed getFeed() {
        return feed;
    }

    @Override
    public URL getUrl() {
        try {
            return new URL(feed.getUrl());
        } catch (MalformedURLException e) {
            LOGGER.fatal(e);
            return null;
        }
    }

    @Override
    public String getId() {
        return getUrl().getHost();
    }

    @Override
    public String getMediaType() {
        return MediaType.TEXT.name();
    }
}
