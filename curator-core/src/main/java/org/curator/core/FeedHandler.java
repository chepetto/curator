package org.curator.core;

import org.apache.log4j.Logger;
import org.curator.common.configuration.Configuration;
import org.curator.common.model.Feed;
import org.curator.core.crawler.Harvester;
import org.curator.core.crawler.impl.FeedHarvestInstruction;
import org.curator.core.interfaces.ArticleManager;
import org.curator.core.interfaces.FeedManager;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import java.io.FileInputStream;
import java.util.*;

@LocalBean
@Singleton
//@ApplicationScoped
//@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class FeedHandler {

    private static final Logger LOGGER = Logger.getLogger(FeedHandler.class);

    @Inject
    private Harvester harvester;
    @Inject
    private ArticleManager articleMgr;

    private boolean available = true;

    @Resource(shareable = false)
    private TimerService timerService;

    @Inject
    private FeedManager feedManager;

    @PostConstruct
    public void onInit() {
        List<Feed> feeds = feedManager.getList(0, 1000);
        Set<String> existingUrls = new HashSet<String>(feeds.size());
        for (Feed feed : feeds) {
            existingUrls.add(feed.getUrl());
        }

        Collection<String> otherUrls = getFeedsFromFile();

        for (String newUrl : otherUrls) {

            if (existingUrls.contains(newUrl)) {
                continue;
            }

            try {
                Feed feed = new Feed();
                feed.setUrl(newUrl);
                feed.setActive(true);

                feedManager.add(feed);

            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
    }

    //@Schedule(persistent=false, minute="*", second="0", hour="*")
    public void watchFeeds() {
        try {
            if (available) {
                timerService.createSingleActionTimer(new Date(), new TimerConfig());
            }
        } catch (Throwable t) {
            LOGGER.error("Cannot import feeds: " + t.getMessage());
        }
    }

    @Timeout
    public void importFeeds() {

        boolean retrieve = Configuration.getBooleanValue(Configuration.RETRIEVE_SEEDS);

        if (retrieve && available) {

            LOGGER.trace("import feeds");

            available = false;

            _callFeedImports();

            available = true;
        }

    }

    private Collection<String> getFeedsFromFile() {

        List<String> feeds = new LinkedList<String>();

        final String SEEDS_FILE = "/home/damoeb/dev/curator/curator-core/src/main/resources/seeds/feeds.xml";

        SAXBuilder builder = new SAXBuilder(false);
        try {
            LOGGER.info(String.format("Loading seed file from %s", SEEDS_FILE));
            Document dom = builder.build(new FileInputStream(SEEDS_FILE));

            XPath path = XPath.newInstance("//feed/@url");

            List<?> list = path.selectNodes(dom);
            for (Object obj : list) {
                Attribute attr = (Attribute) obj;
                feeds.add(attr.getValue().trim());
            }

        } catch (Exception e) {
            LOGGER.fatal(String.format("Cannot load seed file %s. %s", SEEDS_FILE, e.getMessage()));
            LOGGER.debug(e);
        }

        return feeds;
    }


    private void _callFeedImports() {

        articleMgr.cleanup();

        Collection<Feed> outdated = feedManager.getOutdatedFeeds();
        if (!outdated.isEmpty()) {
            LOGGER.info("update " + outdated.size() + " feeds");
            for (Feed feed : outdated) {
                harvester.schedule(new FeedHarvestInstruction(feed));
            }
        }
    }
}
