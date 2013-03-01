package org.curator.core.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Feed;
import org.curator.core.interfaces.FeedManager;
import org.curator.core.request.CuratorRequestException;
import org.curator.core.status.FeedsStatus;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

//@LocalBean
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class FeedManagerBean implements FeedManager {

    private static final Logger LOGGER = Logger.getLogger(FeedManagerBean.class);

    @PersistenceContext(unitName = "primary")
    private EntityManager em;

    @PostConstruct
    public void onInit() {

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean add(Feed feed) throws CuratorException {

        if (feed == null) {
            throw new IllegalArgumentException("feed is null");
        }
        if (StringUtils.isBlank(feed.getUrl())) {
            throw new IllegalArgumentException("url is null");
        }

        Feed old = getByUrl(feed.getUrl());
        if (old != null) {
            LOGGER.trace(String.format("article already exists %s", feed.getUrl()));
            return false;
        }

        em.persist(feed);

        LOGGER.trace(String.format("add article %s", feed.getUrl()));

        em.flush();

        return true;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Feed getById(long feedId) {
        try {
            Query query = em.createNamedQuery(Feed.QUERY_BY_ID);
            query.setParameter("ID", feedId);
            Feed feed = (Feed) query.getSingleResult();
            em.detach(feed);

            return feed;
        } catch (Throwable t) {
            throw new CuratorRequestException("getById failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Feed getByUrl(String feedUrl) {
        try {
            Query query = em.createNamedQuery(Feed.QUERY_BY_URL);
            URL url = new URL(feedUrl);
            query.setParameter("URL", url.toString());
            List list = query.getResultList();
            if (list == null || list.isEmpty()) {
                return null;
            }
            Feed feed = (Feed) list.get(0);
            em.detach(feed);

            return feed;
        } catch (Throwable t) {
            throw new CuratorRequestException("getByUrl failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Collection<Feed> getOutdatedFeeds() {
        try {
            Query query = em.createNamedQuery(Feed.QUERY_OUTDATED_FEEDS);
            query.setParameter("TIMEOUT", new Date(System.currentTimeMillis() - 30 * 60 * 1000 * 5));
            //noinspection unchecked
            return query.getResultList();
        } catch (Throwable t) {
            throw new CuratorRequestException("getOutdatedFeeds failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void update(Feed feed) {
        try {
            em.merge(feed);
            em.flush();
        } catch (Throwable t) {
            throw new CuratorRequestException("update failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Feed setStatus(long feedId, boolean activate) {
        try {
            Query query = em.createNamedQuery(Feed.QUERY_BY_ID);
            query.setParameter("ID", feedId);
            Feed feed = (Feed) query.getSingleResult();
            feed.setActive(activate);

            em.merge(feed);
            em.flush();
            em.detach(feed);

            return feed;
        } catch (Throwable t) {
            throw new CuratorRequestException("setStatus failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void forceHarvest(long feedId) {

        try {
            Query query = em.createNamedQuery(Feed.QUERY_BY_ID);
            query.setParameter("ID", feedId);
            Feed feed = (Feed) query.getSingleResult();
            feed.setHarvestRequired(true);
            em.merge(feed);
            em.flush();

        } catch (Throwable t) {
            throw new CuratorRequestException("forceHarvest failed", t);
        }
    }

    @Override
    public FeedsStatus getStatusOfAll() {
        try {
            FeedsStatus status = new FeedsStatus();

            Query query = em.createNamedQuery(Feed.QUERY_COUNT);
            long count = (Long) query.getSingleResult();

            status.setTotalFeedCount(count);
            return status;
        } catch (Throwable t) {
            throw new CuratorRequestException("forceHarvest failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Feed> getList(int firstResult, int maxResults) {
        try {
            _verifyLimits(firstResult, maxResults);

            Query query = em.createNamedQuery(Feed.QUERY_ALL);
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);

            @SuppressWarnings("unchecked")
            List<Feed> feeds = query.getResultList();
            for (Feed feed : feeds) {
                em.detach(feed);
            }
            return feeds;
        } catch (Throwable t) {
            throw new CuratorRequestException("getById failed", t);
        }
    }

    private void _verifyLimits(int firstResult, int maxResults) {
        if (maxResults == 0) {
            throw new IllegalArgumentException("maxResults is 0");
        }
        if (firstResult < 0) {
            throw new IllegalArgumentException("firstResult < 0");
        }
    }
}
