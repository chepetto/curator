package org.curator.core.crawler.impl;

import com.sun.syndication.feed.module.slash.Slash;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.Comment;
import org.curator.common.model.Content;
import org.curator.common.model.MediaType;
import org.curator.core.HtmlUtil;
import org.curator.core.crawler.AbstractFeedParser;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@LocalBean
@Stateless
public class ArticleFromFeedParser extends AbstractFeedParser<Article> {

    private static final Logger LOGGER = Logger.getLogger(ArticleFromFeedParser.class);

//    private final transient SAXBuilder builder = new SAXBuilder(false);

    @SuppressWarnings("unchecked")
    @Override
    public List<Article> parse(final CrawlerResult result) throws CuratorException {

        final List<Article> articles = new LinkedList<Article>();

        for (SyndEntry entry : getEntries(result.getResponse())) {
            try {
                final Article article = new Article();

                MediaType mediaType = MediaType.fromText(result.getInstruction().getMediaType());
                article.setMediaType(mediaType);

                if (_isUrl(entry.getUri())) {
                    article.setUrl(entry.getUri());
                } else {
                    article.setUrl(entry.getLink());
                }
                article.setTitle(entry.getTitle());
                article.setAuthor(entry.getAuthor());
                article.setDescription(entry.getDescription() == null ? null : entry.getDescription().getValue());

                article.setTags(_getTags(entry));

                article.setDate(entry.getPublishedDate());
                if (entry.getUpdatedDate() != null) {
                    article.setDate(entry.getUpdatedDate());
                }
                if (article.getDate() == null) {
                    article.setDate(new Date());
                }

                article.setContent(_getContent(entry));

                if (article.getContent() == null && article.getDescription() != null) {
                    article.setContent(new Content(article.getDescription()));
                }

//                article.setComments(_getComments(entry));
//                article.setTags(_getTags(entry));
//
                articles.add(article);

            } catch (Throwable t) {
                LOGGER.error(String.format("Cannot parse article. %s", t.getMessage()));
                LOGGER.debug(t);
            }
        }

        return articles;
    }

    private boolean _isUrl(String uri) {
        try {
            new URL(uri);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Try to read comment feed url, and parse it
     *
     * @param entry
     * @return
     */
    private List<Comment> _getComments(SyndEntry entry) {

        final Slash slash = (Slash) entry.getModule(Slash.URI);
        if (slash != null) {

            // todo get url, download, parse

//            try {
//                final Document dom = builder.build(new ByteArrayInputStream(data));
//                article.setFeedUriOfComments(getCommentsFeedByUri(dom, index));
//            } catch (Exception e) {
//                LOGGER.error(String.format("parseComments for article %s. %s", entry.getUrl(), e.getMessage()));
//                LOGGER.debug(e);
//            }
        }

        return null;
    }

    private List<String> _getTags(SyndEntry entry) {

        // todo via subject

        if (entry.getCategories() == null) {
            return null;
        }

        List<String> categories = new LinkedList<String>();
        for (SyndCategoryImpl cat : (List<SyndCategoryImpl>) entry.getCategories()) {
            categories.add(cat.getName());
        }

        return categories;
    }

    private Content _getContent(SyndEntry entry) throws CuratorException {
        final Content content = new Content();

        final StringBuilder bcontent = new StringBuilder(1024);
        for (SyndContent sc : (List<SyndContent>) entry.getContents()) {
            bcontent.append(sc.getValue());
            bcontent.append(" ");
        }

        String html = bcontent.toString();
        if (StringUtils.isBlank(html)) {
            return null;
        } else {

            content.setHtml(html);
            content.setText(HtmlUtil.render(content.getHtml()));

            return content;
        }

    }


//    private String getCommentsFeedByUri(Document dom, int index) throws CuratorException {
//        // some xpath magic
//        try {
//            XPath path = XPath.newInstance(String.format("//item[%s]/wfw:commentRss", String.valueOf(index + 1)));
//            path.addNamespace("wfw", "http://wellformedweb.org/CommentAPI/");
//
//            Object obj = path.selectSingleNode(dom);
//
//            if (obj != null) {
//                Element node = (Element) obj;
//                return node.getValue();
//            }
//
//            return null;
//
//        } catch (Exception e) {
//            LOGGER.debug(e);
//            throw new CuratorException("Cannot parse comment-rss url", e);
//        }
//    }
}
