package org.curator.core.interfaces;

import org.curator.common.exceptions.CuratorException;
import org.curator.core.model.Feed;
import org.curator.core.status.FeedsStatus;

import javax.ejb.Local;
import java.util.Collection;
import java.util.List;

@Local
public interface FeedManager {

    Feed getById(long feedId);

    List<Feed> getList(int firstResult, int maxResults);

    boolean add(Feed feed) throws CuratorException;

    Feed getByUrl(String url);

    Collection<Feed> getOutdatedFeeds();

    void update(Feed feed);

    Feed setStatus(long feedId, boolean activate);

    void forceHarvest(long feedId);

    FeedsStatus getStatusOfAll();
}
