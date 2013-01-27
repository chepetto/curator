package org.curator.core.interfaces;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Feed;
import org.curator.core.status.FeedStatus;

import javax.ejb.Local;
import java.util.Collection;
import java.util.List;

@Local
public interface FeedManager {

    Feed getById(long articleId);

    List<Feed> getList(int firstResult, int maxResults);

    void add(Feed feed) throws CuratorException;

    Feed getByUrl(String url);

    Collection<Feed> getOutdatedFeeds();

    void update(Feed feed);

    Feed setStatus(long feedId, boolean activate);

    void forceHarvest(long feedId);

    FeedStatus getStatus();
}
