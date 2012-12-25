package org.curator.core.interfaces;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.Feed;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.net.URL;
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
}
