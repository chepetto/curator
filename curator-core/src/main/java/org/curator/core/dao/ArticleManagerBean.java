package org.curator.core.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.curator.common.configuration.Configuration;
import org.curator.common.configuration.CuratorInterceptors;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.*;
import org.curator.core.constraint.ConstraintViolation;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.simple.*;
import org.curator.core.eval.Evaluation;
import org.curator.core.eval.Evaluator;
import org.curator.core.extract.TopicExtractor;
import org.curator.core.interfaces.ArticleManager;
import org.curator.core.request.CuratorRequestException;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.hibernate.Hibernate;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

//@LocalBean
@Stateless
@CuratorInterceptors
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
//    @CatchAll
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Article addArticle(Article article) {

        try {

            if (article == null) {
                throw new IllegalArgumentException("article is null");
            }
            if (StringUtils.isBlank(article.getUrl())) {
                throw new IllegalArgumentException("url is null");
            }

            article.setPublished(false);
            article.setDate(new Date());
            article.setMediaType(MediaType.TEXT);
            article.setLocale(Locale.GERMAN);

            // todo download page and store text or add a text field

            return addArticleInternal(article);

        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("add article", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Article addArticleInternal(Article article) throws CuratorException {

        if (article == null) {
            throw new IllegalArgumentException("article is null");
        }
        if (StringUtils.isBlank(article.getUrl())) {
            throw new IllegalArgumentException("url is null");
        }

        article.validateFields();

        Article old = getByUrl(article.getUrl());
        if (old != null) {
            LOGGER.trace(String.format("article already exists %s", article.getUrl()));
            throw new CuratorException(String.format("already exists %s", article.getUrl()));
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

        return article;
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

        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("get article by id", t);
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

        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("get article by url", t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Article publish(long articleId, String customText, String customTitle) {
        try {

            if (StringUtils.isBlank(customText)) {
                throw new IllegalArgumentException("Custom text is empty");
            }
            if (StringUtils.isBlank(customTitle)) {
                throw new IllegalArgumentException("Custom title is empty");
            }

            Query query = em.createNamedQuery(Article.QUERY_BY_ID);
            query.setParameter("ID", articleId);
            Article article = (Article) query.getSingleResult();

            if (article == null) {
                throw new IllegalArgumentException(String.format("Article with id %s does not exist.", articleId));
            }

            if (!article.isPublished()) {
                article.setPublished(true);
                article.setPublishedTime(new Date());
            }

            article.setCustomTitle(customTitle);

            article.setCustomTextRendered(_wikiMarkupToHtml(customText));
            article.setCustomTextMarkup(customText);

            em.merge(article);
            em.flush();

            Hibernate.initialize(article.getMetricResults());
            Hibernate.initialize(article.getTopics());
            //em.detach(article);
            //article.setTopics(null);

            return article;

        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("publish", t);
        }
    }

    private String _wikiMarkupToHtml(String text) {

        StringWriter writer = null;

        try {
            MarkupParser markupParser = new MarkupParser();
            markupParser.setMarkupLanguage(new MediaWikiLanguage());
            writer = new StringWriter(text.length() * 2);
            HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
            builder.setEmitAsDocument(false);
            builder.setXhtmlStrict(true);
            markupParser.setBuilder(builder);
            markupParser.parse(text);

            return writer.getBuffer().toString();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void vote(long articleId, int rating, String remoteAddr) {
        try {

            if (StringUtils.isBlank(remoteAddr)) {
                throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, "Cannot resolve remote address.");
            }

            Query query = em.createNamedQuery(Vote.QUERY_BY_ARTICLE_AND_USER);
            query.setParameter("ARTICLE_ID", articleId);
            query.setParameter("USER_ID", remoteAddr.trim());
            query.setMaxResults(1);
            boolean hasVoted = !query.getResultList().isEmpty();

            if (hasVoted) {
                throw new CuratorRequestException(Response.Status.CONFLICT, "You have already voted.");
            }

            // todo increase just with update statement
            query = em.createNamedQuery(Article.QUERY_BY_ID);
            query.setParameter("ID", articleId);
            Article article = (Article) query.getSingleResult();

            if (article == null) {
                throw new CuratorRequestException(Response.Status.NOT_FOUND, String.format("Article '%s' not found", articleId));
            }
//            long yesterday = System.currentTimeMillis() - 1000 * 60 * 60 * 24;
//            boolean laterThanYesterday = yesterday > article.getDate().getTime();
//            if (laterThanYesterday) {
//                throw new CuratorException("Time frame to vote is expired");
//            }

            article.setRatingsCount(article.getRatingsCount() + 1);
            article.setRatingsSum(article.getRatingsSum() + rating);

            em.merge(article);
            em.flush();


            Vote vote = new Vote();
            vote.setArticleId(articleId);
            vote.setUserId(remoteAddr);
            vote.setDate(new Date());
            em.persist(vote);

        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("vote", t);
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

        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("list published articles", t);
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

        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("redirect", t);
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

        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("cleanup", t);
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

        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("getList failed: " + t.getMessage(), t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Article> getReview(int firstResult, int maxResults, Date firstDate, Date lastDate) {
        try {
            _verifyLimits(firstResult, maxResults);

            if (lastDate == null) {
                throw new IllegalArgumentException("lastDate is null");
            }

            Query query = em.createNamedQuery(Article.QUERY_REVIEW);
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
        } catch (CuratorRequestException t) {
            throw t;
        } catch (IllegalArgumentException t) {
            throw new CuratorRequestException(Response.Status.PRECONDITION_FAILED, t);
        } catch (Throwable t) {
            throw new CuratorRequestException("getReview failed: " + t.getMessage(), t);
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
