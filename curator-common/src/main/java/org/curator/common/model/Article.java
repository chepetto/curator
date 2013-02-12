package org.curator.common.model;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.service.CustomDateDeserializer;
import org.curator.common.service.CustomDateSerializer;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
@Entity(name = "Article")
@Table(name = "Article")
@NamedQueries({
        @NamedQuery(name = Article.QUERY_BY_ID, query = "SELECT a FROM Article a where a.id=:ID"),
        @NamedQuery(name = Article.QUERY_BY_URL, query = "SELECT a FROM Article a where LOWER(a.url)=LOWER(:URL)"),
        @NamedQuery(name = Article.QUERY_ALL, query = "SELECT a FROM Article a"),
        @NamedQuery(name = Article.QUERY_REVIEW, query = "SELECT a FROM Article a WHERE a.date<:FIRST_DATE AND a.date>:LAST_DATE and a.quality>0.1 ORDER BY a.date DESC, a.ratingsCount desc"),
        @NamedQuery(name = Article.QUERY_PUBLISHED, query = "SELECT a FROM Article a where a.published=true AND a.publishedTime<=:FIRST_DATE AND a.publishedTime>=:LAST_DATE order by a.publishedTime desc"),
        @NamedQuery(name = Article.QUERY_REDIRECT_URL_BY_ID, query = "SELECT a.url FROM Article a where a.id=:ID"),
        @NamedQuery(name = Article.UPDATE_INC_VIEWS, query = "UPDATE Article a SET a.views = a.views+1 where a.id=:ID"),
        @NamedQuery(name = Article.QUERY_CLEANUP, query = "SELECT a FROM Article a WHERE a.date>:A_DAY_AGO AND (a.ratingsCount=0 OR a.ratingsCount IS NULL)")
})
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Article implements Serializable {

    public static final String QUERY_BY_ID = "Article.QUERY_BY_ID";
    public static final String QUERY_BY_URL = "Article.QUERY_BY_URL";
    public static final String QUERY_ALL = "Article.QUERY_ALL";
    public static final String QUERY_PUBLISHED = "Article.QUERY_PUBLISHED";
    public static final String QUERY_REDIRECT_URL_BY_ID = "Article.QUERY_REDIRECT_URL_BY_ID";
    public static final String UPDATE_INC_VIEWS = "Article.UPDATE_INC_VIEWS";
    public static final String QUERY_CLEANUP = "Article.QUERY_CLEANUP";
    public static final String QUERY_REVIEW = "Article.QUERY_REVIEW";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Basic
    @Index(name = "urlIdx")
    @Column(nullable = false, unique = true)
    private String url;

    @Basic
    private Locale locale = Locale.GERMAN;

    @Basic
    @Column(nullable = false, length = 1024)
    // todo length
    private String title;

    @Basic
    @Column(length = 128)
    private String author;

    @Basic
    @Lob
    private String text;

    @Column(nullable = false)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Basic
    private Integer views;

    @Basic
    private Double quality;

    @Basic
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    // -- User Feedback -- ---------------------------------------------------------------------------------------------

    @Basic
    private int ratingsCount;

    @Basic
    private int ratingsSum;


    // -- Metrics -- ---------------------------------------------------------------------------------------------------

    @OneToMany(
            fetch = FetchType.LAZY,
            targetEntity = MetricResult.class,
            cascade = CascadeType.ALL
    )
    @JoinColumn(name = "id")
    private Set<MetricResult> metrics = new HashSet<MetricResult>(10);


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_topic_mapping",
            joinColumns = {@JoinColumn(name = "articleId")},
            inverseJoinColumns = {@JoinColumn(name = "topicId")}
    )
    private Set<Topic> topics = new HashSet<Topic>();


    // -- Review -- ----------------------------------------------------------------------------------------------------

    @Basic
    private boolean published;

    @Basic
    private String customTitle;

    @Basic
    private String customTextRendered;

    @Basic
    private String customTextMarkup;

    @Column
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date publishedTime;


    // -- Series -- ----------------------------------------------------------------------------------------------------

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "special_id",
            nullable = true)
    private Special special;

    @Basic
    @Column(name = "special_id",
            unique = false,
            nullable = true,
            insertable = false,
            updatable = false)
    private Long specialId;

    // -- Transient -- -------------------------------------------------------------------------------------------------

    @Transient
    private String description;

    @Transient
    private Content content;

    @Transient
    private List<Comment> comments;

    @Transient
    private List<String> tags;

    public Article() {
        // default
    }

    // -- METRICS -- ---------------------------------------------------------------------------------------------------

    public void addMetricResult(MetricName name, Double result) throws CuratorException {
        if (result != null) {

            MetricResult r = new MetricResult();
            r.setMetricName(name);
            r.setResult(result);
            r.setArticle(this);

            if (metrics.contains(r)) {
                throw new CuratorException(String.format("MetricResult %s already added", name));
            }

            metrics.add(r);
        }
    }

    public Double getMetricResult(MetricName name) {
        for (MetricResult r : metrics) {
            if (r.getMetricName() == name) {
                return r.getResult();
            }
        }
        return null;
    }

    public boolean hasMetricResult(MetricName name) {
        for (MetricResult r : metrics) {
            if (r.getMetricName() == name) {
                return true;
            }
        }
        return false;
    }

    @PrePersist
    public void onPersist() {
        StringBuilder builder = new StringBuilder(2000);
//        builder.append(StringUtils.defaultIfBlank(description, ""));
        if (content != null) {
            if (StringUtils.isBlank(content.getText())) {
                if (!StringUtils.isBlank(content.getHtml())) {
                    // todo render
                    builder.append(content.getHtml());
                }
            } else {
                builder.append(content.getText().trim());
            }
        }
        text = builder.toString();
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    // -- GETTER/SETTER -- ---------------------------------------------------------------------------------------------

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<MetricResult> getMetricResults() {
        return metrics;
    }

    public void setMetrics(Set<MetricResult> metrics) {
        this.metrics = metrics;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int compareTo(Article a) {
        return getUrl().compareTo(a.getUrl());
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }

    public Double getQuality() {
        return quality;
    }

    public void setQuality(Double quality) {
        this.quality = quality;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Date getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(Date publishedTime) {
        this.publishedTime = publishedTime;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getCustomTextRendered() {
        return customTextRendered;
    }

    public void setCustomTextRendered(String customText) {
        this.customTextRendered = customText;
    }

    public String getCustomTextMarkup() {
        return customTextMarkup;
    }

    public void setCustomTextMarkup(String customTextMarkup) {
        this.customTextMarkup = customTextMarkup;
    }

    public Special getSpecial() {
        return special;
    }

    public void setSpecial(Special special) {
        this.special = special;
    }

    public Long getSpecialId() {
        return specialId;
    }

    public void setSpecialId(Long specialId) {
        this.specialId = specialId;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(int voteCount) {
        this.ratingsCount = voteCount;
    }

    public int getRatingsSum() {
        return ratingsSum;
    }

    public void setRatingsSum(int voteSum) {
        this.ratingsSum = voteSum;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }
}
