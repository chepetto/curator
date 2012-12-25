package org.curator.core.crawler.impl;

import org.apache.commons.lang.StringUtils;
import org.curator.common.model.Article;
import org.springframework.beans.BeanUtils;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

public class ComplexHarvestInstruction implements HarvestInstruction {

    public static final String TYPE_RSS = "rss";
    public static final String TYPE_HTML = "html";

    private Article articleInstance;

    // madatory
    private String id;
    private URL url;
    private String contentType;
    private String mediaType;
    private String[] roots;
    private boolean updateable;

    // optional
    private Queue<String> path;
    private boolean followUrl;
    private String article;
    private String link;
    private String views;
    private String date;
    private String duration;
    private String title;
    private String text;
    private String tags;

    public ComplexHarvestInstruction(ComplexHarvestInstruction source) {
        BeanUtils.copyProperties(source, this);
        if(source.getPath()!=null) {
            path = new LinkedList<String>();
            path.addAll(source.getPath());
        }
    }

    public ComplexHarvestInstruction() {
        //
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Queue<String> getPath() {
        return path;
    }

    public void setPath(Queue<String> path) {
        this.path = path;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public void setUpdateable(boolean updateable) {
        this.updateable = updateable;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isFollowUrl() {
        return followUrl;
    }

    public void setFollowUrl(boolean followUrl) {
        this.followUrl = followUrl;
    }

    public String[] getRoots() {
        return roots;
    }

    public void setRoots(String[] roots) {
        this.roots = roots;
    }

    public Article getArticleInstance() {
        return articleInstance;
    }

    public void setArticleInstance(Article articleInstance) {
        this.articleInstance = articleInstance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void validate() {
        if(StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("id is null");
        }
        if(StringUtils.isEmpty(contentType)) {
            throw new IllegalArgumentException("contentType is null");
        }
        if(url == null) {
            throw new IllegalArgumentException("url is null");
        }
        if(!TYPE_HTML.equalsIgnoreCase(contentType) && !TYPE_RSS.equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("contentType '"+ contentType +"' is not supported");
        }
        if(TYPE_HTML.equalsIgnoreCase(contentType)) {
            if(StringUtils.isEmpty(text)) {
                throw new IllegalArgumentException("text is null");
            }
            if(StringUtils.isEmpty(title)) {
                throw new IllegalArgumentException("title is null");
            }
        }
    }
}
