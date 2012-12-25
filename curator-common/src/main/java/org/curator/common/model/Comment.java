package org.curator.common.model;

import java.util.Date;

@SuppressWarnings("serial")
public class Comment extends Corpus {

    private Long id;
    private String author;
    private Date published;
    private String title;
    private String text;

    public String author() {
        return author;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date publishedDate() {
        return published;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublished(Date published) {
        this.published = published;
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

}
