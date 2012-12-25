package org.curator.core.crawler.impl;

import java.util.LinkedList;
import java.util.List;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Comment;
import org.apache.log4j.Logger;

import org.curator.core.crawler.AbstractFeedParser;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

//@Controller("commentHandler")
public class CommentFromFeedParser extends AbstractFeedParser<Comment> {

    private static final Logger LOGGER = Logger.getLogger(CommentFromFeedParser.class);

    @SuppressWarnings("unchecked")
    @Override
    public List<Comment> parse(CrawlerResult result) throws CuratorException {

        List<Comment> comments = new LinkedList<Comment>();

        for (SyndEntry entry : getEntries(result.getResponse())) {

            Comment c = new Comment();
            c.setAuthor(entry.getAuthor());
            c.setPublished(entry.getPublishedDate());
            c.setTitle(entry.getTitle());

            final StringBuffer content = new StringBuffer(200);
            for (SyndContent sc : (List<SyndContent>) entry.getContents()) {
                content.append(sc.getValue());
            }
            c.setText(content.toString());

            comments.add(c);
        }

        return comments;
    }

}
