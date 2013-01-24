package org.curator.core.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.curator.common.configuration.Configuration;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.MetricProvider;
import org.curator.common.model.MetricResult;
import org.curator.core.constraint.ConstraintViolation;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.simple.*;
import org.curator.core.eval.Evaluation;
import org.curator.core.eval.Evaluator;
import org.curator.core.extract.TopicExtractor;
import org.curator.core.interfaces.ArticleManager;
import org.hibernate.Hibernate;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.net.URL;
import java.util.*;

//@LocalBean
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class ArticleManagerBean implements ArticleManager {

    private static final Logger LOGGER = Logger.getLogger(ArticleManagerBean.class);

    @Inject
    private ArticleFreshnessCriterion articleFreshnessCriterion;
    @Inject
    private ArticleReadingEaseCriterion articleReadingEaseCriterion;
    //    @Inject
//    private IncomingLinksCriterion incomingLinksCriterion;
    @Inject
    private InverseOutgoingLinksCriterion inverseOutgoingLinksCriterion;
    @Inject
    private RecurringUserCriterion recurringUserCriterion;
    @Inject
    private ThesaurusCriterion thesaurusCriterion;
    @Inject
    private ViewCountCriterion viewCountCriterion;

    @Inject
    private TopicExtractor topicExtractor;

    @Inject
    private Evaluator evaluator;


    private List<MetricProvider> metricProviders;

    @PersistenceContext(unitName = "primary")
    private EntityManager em;
    private int maxResults;

    @PostConstruct
    public void onInit() {

        maxResults = Configuration.getIntValue("query.max.results", 1000);

        metricProviders = new ArrayList<MetricProvider>();

        metricProviders.add(articleFreshnessCriterion);
        metricProviders.add(articleReadingEaseCriterion);
//        metricProviders.add(incomingLinksCriterion);
        metricProviders.add(inverseOutgoingLinksCriterion);
        metricProviders.add(recurringUserCriterion);
        metricProviders.add(thesaurusCriterion);
        metricProviders.add(viewCountCriterion);

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addArticle(Article article) throws CuratorException {

        if (article == null) {
            throw new IllegalArgumentException("article is null");
        }
        if (StringUtils.isBlank(article.getUrl())) {
            throw new IllegalArgumentException("url is null");
        }

        Article old = getByUrl(article.getUrl());
        if (old != null) {
            LOGGER.trace(String.format("article already exists %s", article.getUrl()));
            return;
        }

        LOGGER.trace(String.format("add article %s", article.getUrl()));

        // -- Calculation -- -------------------------------------------------------------------------------------------

        for (MetricProvider metric : metricProviders) {
            metric.pushMetricResults(article);
        }

        // extract relevant terms
        //article.setTopics(topicExtractor.extract(article));

        // -- Persistence -- -------------------------------------------------------------------------------------------

        Set<MetricResult> results = article.getMetricResults();
        article.setMetrics(new HashSet<MetricResult>(0));
        em.persist(article);
        em.flush();
        em.refresh(article);

        for (MetricResult result : results) {
            result.setArticle(article);
            result.setArticelId(article.getId());
            em.persist(result);
        }
        em.flush();

        article.setMetrics(results);
        try {
            Evaluation result = evaluator.evaluate(article, Goal.CHALLENGING_TEXT);
            article.setQuality(result.quality());
            em.merge(article);

        } catch (ConstraintViolation e) {
            LOGGER.error("rejected " + article.getUrl());
        }
        em.flush();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Article getById(long articleId) {
        try {
            Query query = em.createNamedQuery(Article.QUERY_BY_ID);
            query.setParameter("ID", articleId);
            Article article = (Article) query.getSingleResult();
            Hibernate.initialize(article.getMetricResults());
            Hibernate.initialize(article.getTopics());
            em.detach(article);

            return article;
        } catch (Throwable t) {
            throw new CuratorRollbackException("getById failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Article getByUrl(String url) {
        try {
            Query query = em.createNamedQuery(Article.QUERY_BY_URL);
            query.setParameter("URL", url);
            List list = query.getResultList();
            if (list == null || list.isEmpty()) {
                return null;
            }
            Article article = (Article) list.get(0);
            Hibernate.initialize(article.getMetricResults());
            em.detach(article);

            return article;
        } catch (Throwable t) {
            throw new CuratorRollbackException("getById failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Article publish(long articleId, String customText) {
        try {
            Query query = em.createNamedQuery(Article.QUERY_BY_ID);
            query.setParameter("ID", articleId);
            Article article = (Article) query.getSingleResult();

            if (article.isPublished()) {
                throw new CuratorException("Already published");
            }

            article.setPublished(true);
            article.setPublishedTime(new Date());

            if (StringUtils.isBlank(customText)) {
                article.setCustomText(StringUtils.trim(customText));
            }

            em.merge(article);
            em.flush();

            Hibernate.initialize(article.getMetricResults());
            Hibernate.initialize(article.getTopics());
            //em.detach(article);
            //article.setTopics(null);

            return article;
        } catch (Throwable t) {
            throw new CuratorRollbackException("getById failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Article rate(long articleId, int rating) throws CuratorException {
        try {
            Query query = em.createNamedQuery(Article.QUERY_BY_ID);
            query.setParameter("ID", articleId);
            Article article = (Article) query.getSingleResult();

//            long yesterday = System.currentTimeMillis() - 1000 * 60 * 60 * 24;
//            boolean laterThanYesterday = yesterday > article.getDate().getTime();
//            if (laterThanYesterday) {
//                throw new CuratorException("Time frame to rate is expired");
//            }

            article.setRatingsCount(article.getRatingsCount() + 1);
            article.setRatingsSum(article.getRatingsSum() + rating);

            em.merge(article);
            em.flush();
            em.refresh(article);

            em.detach(article);
            article.setTopics(null);
            article.setMetrics(null);

            return article;

        } catch (Throwable t) {
            throw new CuratorRollbackException("rate failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Article> getPublished(Date firstDate, Date lastDate) {
        try {
            Query query = em.createNamedQuery(Article.QUERY_PUBLISHED);
            query.setParameter("FIRST_DATE", firstDate);
            query.setParameter("LAST_DATE", lastDate);
            query.setMaxResults(maxResults);

            @SuppressWarnings("unchecked")
            List<Article> articles = query.getResultList();
            for (Article article : articles) {
                em.detach(article);
                article.setMetrics(null);
                article.setTopics(null);
            }

            return articles;
        } catch (Throwable t) {
            throw new CuratorRollbackException("getPublished failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public URL redirect(long articleId) {
        try {

            Query update = em.createNamedQuery(Article.UPDATE_INC_VIEWS);
            update.setParameter("ID", articleId);
            update.executeUpdate();

            Query query = em.createNamedQuery(Article.QUERY_REDIRECT_URL_BY_ID);
            query.setParameter("ID", articleId);
            String url = (String) query.getSingleResult();

            return new URL(url);
        } catch (Throwable t) {
            throw new CuratorRollbackException("redirect failed", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cleanup() {
        try {

            Query cleanup = em.createNamedQuery(Article.QUERY_CLEANUP);
            cleanup.setParameter("A_DAY_AGO", new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24));

            List<Article> list = (List<Article>) cleanup.getResultList();

            for (Article article : list) {
                em.remove(article);
                em.flush();
            }

        } catch (Throwable t) {
            throw new CuratorRollbackException("cannot delete unrated articles", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Article> getList(int firstResult, int maxResults) {
        try {
            _verifyLimits(firstResult, maxResults);

            Query query = em.createNamedQuery(Article.QUERY_ALL);
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);

            @SuppressWarnings("unchecked")
            List<Article> articles = query.getResultList();
            for (Article article : articles) {
                em.detach(article);
                article.setMetrics(null);
                article.setTopics(null);
            }
            return articles;
        } catch (Throwable t) {
            throw new CuratorRollbackException("getList failed: " + t.getMessage(), t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Article> getSuggest(int firstResult, int maxResults, Date firstDate, Date lastDate) {
        try {
            _verifyLimits(firstResult, maxResults);

            if (lastDate == null) {
                throw new IllegalArgumentException("lastDate is null");
            }

            Query query = em.createNamedQuery(Article.QUERY_SUGGEST);
            query.setParameter("FIRST_DATE", firstDate);
            query.setParameter("LAST_DATE", lastDate);
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);

            @SuppressWarnings("unchecked")
            List<Article> articles = query.getResultList();
            for (Article article : articles) {
                em.detach(article);
                article.setMetrics(null);
                article.setTopics(null);
            }
            return articles;
        } catch (Throwable t) {
            throw new CuratorRollbackException("getSuggest failed: " + t.getMessage(), t);
        }
    }

    private void _verifyLimits(int firstResult, int customMaxResults) {
        if (customMaxResults == 0) {
            throw new IllegalArgumentException("maxResults is 0");
        }
        if (customMaxResults > maxResults) {
            throw new IllegalArgumentException("maxResults exceeds upper limit " + maxResults);
        }
        if (firstResult < 0) {
            throw new IllegalArgumentException("firstResult < 0");
        }
    }
}
