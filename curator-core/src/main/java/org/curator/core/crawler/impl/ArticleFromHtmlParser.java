package org.curator.core.crawler.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.Content;
import org.curator.common.model.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@LocalBean
@Stateless
public class ArticleFromHtmlParser {

    private static final Logger LOGGER = Logger.getLogger(ArticleFromHtmlParser.class);

    @SuppressWarnings("unchecked")
    public List<Article> parse(final CrawlerResult result) throws CuratorException {

        final List<Article> articles = new LinkedList<Article>();

        ComplexHarvestInstruction instruction = (ComplexHarvestInstruction) result.getInstruction();

        Document document = Jsoup.parse(result.getResponse());

        String selArticle = instruction.getArticle();
        if (selArticle == null) {
            articles.add(getArticle(document, instruction));
        } else {
            if (instruction.getArticleInstance() != null) {
                throw new CuratorException("Should not happen.");
            }

            for (Element article : document.select(selArticle)) {
                try {
                    articles.add(getArticle(article, instruction));
                } catch (Throwable t) {
                    LOGGER.warn("Parsing article with errors. Source: " + result.getInstruction().getUrl() + " Message: " + t.getMessage());
                }
            }
        }

        LOGGER.info("Parsed " + articles.size() + " articles from " + result.getInstruction().getUrl());
        return articles;
    }

    private Article getArticle(Element context, ComplexHarvestInstruction instruction) throws CuratorException {
        Article article;
        if (instruction.getArticleInstance() == null) {
            article = new Article();
            MediaType mediaType = MediaType.fromText(instruction.getMediaType());
            article.setMediaType(mediaType);
        } else {
            // merge with data from feed
            article = instruction.getArticleInstance();
        }

        article.setUrl(_getUrl(context, instruction));

        if (!StringUtils.isBlank(instruction.getTitle())) {
            article.setTitle(context.select(instruction.getTitle()).text());
        }
        if (!StringUtils.isBlank(instruction.getViews())) {
            article.setViews(_getViews(context, instruction));
        }
//        if(!StringUtils.isBlank(instruction.getTags())) {
//            article.setTags(_getTags(context, instruction));
//        }
        if (!StringUtils.isBlank(instruction.getText())) {
            article.setContent(new Content(context.select(instruction.getText()).text()));
        }
//        if(!StringUtils.isBlank(instruction.getDate())) {
        // todo extract date
        article.setDate(new Date());
//        }


        return article;
    }

    private String _getUrl(Element context, ComplexHarvestInstruction instruction) throws CuratorException {
        String url = null;
        if (!StringUtils.isEmpty(instruction.getLink())) {
            Element link = context.select(instruction.getLink()).first();
            if (link == null) {
                throw new CuratorException("Link not found in context");
            }
            // try to extract url
            if ("a".equalsIgnoreCase(link.tagName())) {
                url = link.attr("href");
            } else {
                url = link.text();
            }
        }

        if (StringUtils.isBlank(url)) {
            url = instruction.getUrl().toString();
        } else {
            try {
                // form absolute url with context of instruction url
                url = new URL(instruction.getUrl(), url).toString();
            } catch (MalformedURLException e) {
                throw new CuratorException(String.format("Unable to form absolute url from %s in context %s", url, instruction.getUrl()), e);
            }
        }

        return url;
    }

    private Integer _getViews(Element context, ComplexHarvestInstruction instruction) {
        if (StringUtils.isBlank(instruction.getViews())) {
            return null;
        }
        Elements elements = context.select(instruction.getViews());
        if (elements.isEmpty()) {
            return null;
        }

        return NumberUtils.createInteger(elements.text());
    }

    private List<String> _getTags(Element context, ComplexHarvestInstruction instruction) {
        return null;
    }

}
