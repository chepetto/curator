package org.curator.core.crawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.curator.common.exceptions.CuratorException;
import org.curator.core.crawler.impl.CrawlerResult;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public abstract class AbstractFeedParser<OUT> {

    protected abstract List<OUT> parse(CrawlerResult result) throws CuratorException;

    @SuppressWarnings("unchecked")
    protected List<SyndEntry> getEntries(final String data) throws CuratorException {
        XmlReader reader = null;
        InputStream in = null;
        try {

            in = new ByteArrayInputStream(data.getBytes());
            reader = new XmlReader(in);

            final SyndFeedInput input = new SyndFeedInput();
            input.setXmlHealerOn(true);
            final SyndFeed feed = input.build(reader);
            feed.setEncoding("UTF-8");
            return (List<SyndEntry>) feed.getEntries();

        } catch (Exception e) {
            throw new CuratorException("Cannot load feed-entries.", e);

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // nothing
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // nothing
                }
            }
        }
    }
}
