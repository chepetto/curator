package org.curator.common.model;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.curator.common.service.CustomDateDeserializer;
import org.curator.common.service.CustomDateSerializer;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Feed only for now, cause complex Seeds are more complicated to store
 */
@Entity(name = "Feed")
@Table(name = "Feed")
@NamedQueries({
        @NamedQuery(name = Feed.QUERY_BY_ID, query = "SELECT a FROM Feed a where a.id=:ID"),
        @NamedQuery(name = Feed.QUERY_BY_URL, query = "SELECT a FROM Feed a where LOWER(a.url)=LOWER(:URL)"),
        @NamedQuery(name = Feed.QUERY_ALL, query = "SELECT a FROM Feed a"),
        @NamedQuery(name = Feed.QUERY_OUTDATED_FEEDS, query = "SELECT a FROM Feed a WHERE a.active=true AND (a.lastHarvestTime IS NULL OR a.lastHarvestTime<:TIMEOUT) AND a.harvestRequired=false")
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Feed implements Serializable {

    public static final String QUERY_BY_ID = "Feed.QUERY_BY_ID";
    public static final String QUERY_BY_URL = "Feed.QUERY_BY_URL";
    public static final String QUERY_ALL = "Feed.QUERY_ALL";
    public static final String QUERY_OUTDATED_FEEDS = "Feed.QUERY_OUTDATED_FEEDS";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Basic
    @Index(name = "feedUrlIdx")
    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(nullable = true)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastHarvestTime;

    @Column(nullable = true)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastArticleTime;

    @Basic
    private int articlesCount;

    // -- STATUS -- ----------------------------------------------------------------------------------------------------

    @Basic
    private boolean active;

    @Basic
    private boolean harvestRequired;

    @Basic
    private boolean reviewRequired;

    public Feed() {
        // default
    }

    @PrePersist
    public void onPrePersist() {
        creationTime = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getLastHarvestTime() {
        return lastHarvestTime;
    }

    public void setLastHarvestTime(Date lastHarvestTime) {
        this.lastHarvestTime = lastHarvestTime;
    }

    public Date getLastArticleTime() {
        return lastArticleTime;
    }

    public void setLastArticleTime(Date lastArticleTime) {
        this.lastArticleTime = lastArticleTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isReviewRequired() {
        return reviewRequired;
    }

    public void setReviewRequired(boolean reviewRequired) {
        this.reviewRequired = reviewRequired;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isHarvestRequired() {
        return harvestRequired;
    }

    public void setHarvestRequired(boolean harvestRequired) {
        this.harvestRequired = harvestRequired;
    }

    public int getArticlesCount() {
        return articlesCount;
    }

    public void setArticlesCount(int articlesCount) {
        this.articlesCount = articlesCount;
    }
}
