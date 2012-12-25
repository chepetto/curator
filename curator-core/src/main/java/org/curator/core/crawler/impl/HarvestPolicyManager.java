package org.curator.core.crawler.impl;

import org.apache.zookeeper.server.SessionTracker;
import org.curator.common.model.Article;
import org.curator.common.model.Feed;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.net.URL;
import java.util.*;

@Stateless
public class HarvestPolicyManager {

    private Map<URL, Long> requests = new LinkedHashMap<URL, Long>(50, 2, false) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<URL, Long> eldest) {
            return size() > 500;
        }
    };

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    public boolean requestRetrieval(HarvestInstruction instruction) {

        if(instruction instanceof FeedHarvestInstruction) {
            return requestFeedRetrieval((FeedHarvestInstruction) instruction);
        }

        Query query = em.createNamedQuery(Article.QUERY_BY_URL);
        query.setParameter("URL", instruction.toString());

        if(!query.getResultList().isEmpty()) {
            return false;
        }


        // todo move to db

        if(requests.containsKey(instruction.getUrl())) {
            return false;
        }

        requests.put(instruction.getUrl(), System.currentTimeMillis());

        return true;
    }

    private boolean requestFeedRetrieval(FeedHarvestInstruction instruction) {

        Query query = em.createNamedQuery(Feed.QUERY_BY_URL);
        query.setParameter("URL", instruction.getUrl().toString());
        List list = query.getResultList();

        if(list.isEmpty()) {
            throw new IllegalArgumentException("Cannot resolve feed by url '"+instruction.getUrl().toString()+"'");
        }

        Feed feed = (Feed) list.get(0);

        if(feed.getLastHarvestTime() == null) {
            return true;
        }

        long timeout = System.currentTimeMillis() - 30 * 60 * 1000 * 5;

        return feed.getLastHarvestTime().getTime() <= timeout;

    }
}
