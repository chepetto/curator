package org.curator.core.crawler.impl;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.Feed;
import org.curator.core.crawler.Harvester;
import org.curator.core.interfaces.ArticleManager;
import org.curator.core.interfaces.FeedManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;


@SuppressWarnings("UnusedDeclaration")
@LocalBean
@Stateless
public class DefaultHarvester implements Harvester {

    private static transient final Logger LOGGER = Logger.getLogger(DefaultHarvester.class);

    @Inject
    private HarvestPolicyManager harvestPolicyManager;
    @Inject
    private ArticleFromFeedParser articleFromFeedParser;
    @Inject
    private ArticleFromHtmlParser articleFromHtmlParser;
    @Inject
    private ArticleManager articleManager;
    @Inject
    private FeedManager feedManager;

    private ExecutorService executor;

    @Inject
    private HttpClientBean client;

    private ConcurrentLinkedQueue<Future<CrawlerResult>> futures;

    private Thread crawlerResultListener;

    private boolean running;

    @PostConstruct
    public void onInit() {

        futures = new ConcurrentLinkedQueue<Future<CrawlerResult>>();

        if (isRunning()) {
            throw new IllegalStateException("Already running");
        }

        running = true;
        executor = Executors.newFixedThreadPool(1);

//        int corePoolSize = 2;
//        int maximumPoolSize = 4;
//        long keepAliveTime = 40;
//        TimeUnit unit = TimeUnit.SECONDS;
//        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
//        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
//            @Override
//            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//                LOGGER.info("rejected crawler job");
//            }
//        };
//        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);

        crawlerResultListener = getCrawlerResultListenerThread();
        crawlerResultListener.start();
    }

    private Thread getCrawlerResultListenerThread() {
        return new Thread() {

            @Override
            public void run() {

                while (isRunning()) {
                    Iterator<Future<CrawlerResult>> lit = futures.iterator();
                    while (lit.hasNext()) {
                        Future<CrawlerResult> future = lit.next();
                        if (future.isDone() || future.isCancelled()) {
                            lit.remove();
                            LOGGER.trace(String.format("Harvester jobs queued: %s", futures.size()));
                            try {
                                CrawlerResult result = future.get();

                                postProcessResult(result);

                            } catch (Throwable t) {
                                LOGGER.error(t);
                            }
                        }
                    }
                }
            }
        };
    }

    private void postProcessResult(CrawlerResult result) throws CuratorException, MalformedURLException {

        if (result.getInstruction() instanceof FeedHarvestInstruction) {

            // -- Feeds -- ---------------------------------------------------------------------------------------------


            Feed feed = ((FeedHarvestInstruction) result.getInstruction()).getFeed();

            feed.setLastHarvestTime(new Date());

            if (CrawlerResult.OK == result.getStatus()) {
                try {
                    List<Article> articles = articleFromFeedParser.parse(result);
                    _addArticles(articles);
                    feed.setLastArticleTime(articles.get(0).getDate());
                    feed.setReviewRequired(false);
                    feed.setArticlesCount(feed.getArticlesCount() + articles.size());

                } catch (Throwable t) {
                    LOGGER.error("Cannot extract articles: " + t.getMessage());
                    feed.setReviewRequired(true);
                    feed.setActive(false);
                }
            } else {
                feed.setReviewRequired(true);
                feed.setActive(false);
            }

//            _getFeedManager(null).update(feed);
            feedManager.update(feed);

        } else {

            // -- Complex Instruction -- -------------------------------------------------------------------------------
            if (CrawlerResult.OK == result.getStatus()) {

                ComplexHarvestInstruction instruction = (ComplexHarvestInstruction) result.getInstruction();

                if (ComplexHarvestInstruction.TYPE_RSS.equalsIgnoreCase(instruction.getContentType())) {

                    List<Article> articles = articleFromFeedParser.parse(result);

                    if (instruction.isFollowUrl()) {

                        // next response will be html
                        // todo type should be deferred from http request
                        instruction.setContentType(ComplexHarvestInstruction.TYPE_HTML);

                        for (Article article : articles) {

                            ComplexHarvestInstruction clone = new ComplexHarvestInstruction(instruction);
                            clone.setArticleInstance(article);
                            clone.setUrl(new URL(article.getUrl()));

                            schedule(clone);
                        }

                    } else {
                        _addArticles(articles);
                    }

                } else {

                    postProcessHtmlResult(result);
                }
            }
        }
    }

//    @Inject
//    private FeedManager _getFeedManager(FeedManager feedManager) {
//        return feedManager;
//    }


    private void postProcessHtmlResult(CrawlerResult result) throws CuratorException, MalformedURLException {

        ComplexHarvestInstruction instruction = (ComplexHarvestInstruction) result.getInstruction();
        Queue<String> steps = instruction.getPath();
        if (steps == null || steps.isEmpty()) {
            _addArticles(articleFromHtmlParser.parse(result));

        } else {
            String selector = instruction.getPath().poll();
            Elements elements = Jsoup.parse(result.getResponse()).select(selector);

            if (elements == null || elements.isEmpty()) {
                throw new CuratorException(String.format("Selector '%s' returns no results on %s (%s) ", selector, instruction.getId(), instruction.getUrl()));
            }

            for (Element element : elements) {
                String nextRelativeUrl = element.attr("href");
                URL newAbsoluteUrl = new URL(instruction.getUrl(), nextRelativeUrl);

                // todo !updatable skip

                ComplexHarvestInstruction clone = new ComplexHarvestInstruction(instruction);
                clone.setUrl(newAbsoluteUrl);
                clone.validate();

                schedule(clone);
            }
        }
    }

    private void _addArticles(List<Article> list) {
        for (Article article : list) {
            try {
                articleManager.addArticle(article);
            } catch (Throwable t) {
                LOGGER.error("Cannot add article: " + t.getMessage());
            }
        }
    }

    /**
     * Send http get request to url found in instruction using http client
     *
     * @param instruction a job description
     * @return the request result
     */
    protected CrawlerResult harvest(final HarvestInstruction instruction) {

        GetMethod method = null;

        CrawlerResult result = new CrawlerResult();
        InputStream stream = null;
        try {

            if (instruction == null) {
                throw new IllegalArgumentException("instruction is null");
            }

            if (instruction.getUrl() == null) {
                throw new IllegalArgumentException("url is null");
            }

            final String url = instruction.getUrl().toURI().toASCIIString();

            LOGGER.info("Requesting url " + url);

            method = new GetMethod(url);
            int status = client.getHttpClient().executeMethod(method);

            if (isBadHttpStatus(status)) {
                LOGGER.warn("Receiving status " + status + " for url " + instruction.getUrl());
                throw new CuratorException(String.format("Bad http status %s for %s.", status, instruction.getUrl()));
            }

            LOGGER.debug("Receiving status " + status + " for url " + instruction.getUrl());

            result.setStatus(CrawlerResult.OK);
            result.setInstruction(instruction);
            // todo get content type
            result.setContentType("");

            result.setResponse(_readResponse(method));

            LOGGER.trace(String.format("Fetched %s (%s)", instruction.getUrl(), instruction.getId()));

            Thread.sleep(200);

            return result;

        } catch (Throwable t) {
            if (method != null) {
                method.abort();
            }
            LOGGER.error("Failed to retrieve " + instruction.getUrl() + ": " + t.getMessage());
            result.setStatus(CrawlerResult.ERROR);
            result.setResponse(t.getMessage());
            return result;

        } finally {

            try {
                if (method != null) {
                    method.releaseConnection();
                }
            } catch (Throwable t) {
                // nothing
            }

            try {
                if (method != null) {
                    method.releaseConnection();
                }
            } catch (Throwable t) {
                // nothing
            }
        }
    }

    private String _readResponse(GetMethod method) throws IOException {
        InputStream stream = null;
        Scanner scanner = null;
        try {
            stream = method.getResponseBodyAsStream();

            // we could only pass the stream, though then the connection will be probably be closed
            StringBuilder response = new StringBuilder(2000);

            // todo use this one
//            String responseCharSet = method.getResponseCharSet();
//            int responseContentLength = (int) method.getResponseContentLength();
//            StringBuilder response = new StringBuilder(Math.max(Constants.DEFAULT_RESPONSE_CONTENT_LENGTH, responseContentLength));

            scanner = new Scanner(new InputStreamReader(new BufferedInputStream(stream)));
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }

            return response.toString();

        } finally {
            if (scanner != null) {
                scanner.close();
            }
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // nothing
            }
        }
    }

    private boolean isBadHttpStatus(final int status) {
        boolean result = false;
        if (status == 404) {
            result = true;
        }
        return result;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void shutdown() {
        if (crawlerResultListener != null) {
            crawlerResultListener.interrupt();
        }
        running = false;
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * Place retrieval request
     *
     * @param instruction a job description
     * @return the result
     */
    @Override
    public Future<CrawlerResult> submit(final HarvestInstruction instruction) {

        if (instruction == null) {
            throw new IllegalArgumentException("instruction is null");
        }

        LOGGER.trace("submit " + instruction.getUrl());
        return executor.submit(new Callable<CrawlerResult>() {
            @Override
            public CrawlerResult call() throws Exception {
                return harvest(instruction);
            }
        });
    }

    /**
     * Place retrieval request and let crawler forward results to post-processors
     *
     * @param instruction a job description
     */
    @Override
    public void schedule(final HarvestInstruction instruction) {

        if (instruction == null) {
            throw new IllegalArgumentException("instruction is null");
        }

        boolean approved = harvestPolicyManager.requestRetrieval(instruction);
        if (approved) {

            LOGGER.trace(String.format("schedule %s (%s queued)", instruction.getUrl(), futures.size()));
            Future<CrawlerResult> future = executor.submit(new Callable<CrawlerResult>() {
                @Override
                public CrawlerResult call() {
                    return harvest(instruction);
                }
            });

            futures.add(future);
        }

    }
}
