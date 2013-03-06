package org.curator.core.model;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.exceptions.CuratorStatus;
import org.curator.common.model.Comment;
import org.curator.common.model.Content;
import org.curator.common.service.CustomDateDeserializer;
import org.curator.common.service.CustomDateSerializer;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("serial")
@Entity(name = "Article")
@Table(name = "Article"
        //uniqueConstraints = @UniqueConstraint(columnNames = {"version", "foreignId"})
)
@NamedQueries({
        @NamedQuery(name = Article.QUERY_BY_ID, query = "SELECT a FROM Article a where a.id=:ID"),
        @NamedQuery(name = Article.QUERY_BY_URL, query = "SELECT a FROM Article a where LOWER(a.url)=LOWER(:URL)"),
        @NamedQuery(name = Article.QUERY_ALL, query = "SELECT a FROM Article a order by a.date desc"),
        @NamedQuery(name = Article.QUERY_LIVE, query = "SELECT a FROM Article a WHERE a.date<:FIRST_DATE AND a.date>:LAST_DATE and a.quality>0.1 ORDER BY a.quality desc"),
        @NamedQuery(name = Article.QUERY_FEATURED, query = "SELECT a FROM Article a where a.featured=true AND a.featuredTime<=:FIRST_DATE AND a.featuredTime>=:LAST_DATE order by a.featuredTime desc"),
        @NamedQuery(name = Article.QUERY_REDIRECT_URL_BY_ID, query = "SELECT a.url FROM Article a where a.id=:ID"),
        @NamedQuery(name = Article.UPDATE_INC_VIEWS, query = "UPDATE Article a SET a.views = a.views+1 where a.id=:ID"),
        @NamedQuery(name = Article.QUERY_CLEANUP, query = "SELECT a FROM Article a WHERE a.date>:A_DAY_AGO AND (a.ratingsCount=0 OR a.ratingsCount IS NULL)")
})
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Article implements Serializable {

    public static final String QUERY_BY_ID = "Article.QUERY_BY_ID";
    public static final String QUERY_BY_URL = "Article.QUERY_BY_URL";
    public static final String QUERY_ALL = "Article.QUERY_ALL";
    public static final String QUERY_FEATURED = "Article.QUERY_FEATURED";
    public static final String QUERY_REDIRECT_URL_BY_ID = "Article.QUERY_REDIRECT_URL_BY_ID";
    public static final String UPDATE_INC_VIEWS = "Article.UPDATE_INC_VIEWS";
    public static final String QUERY_CLEANUP = "Article.QUERY_CLEANUP";
    public static final String QUERY_LIVE = "Article.QUERY_LIVE";

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

    @Lob
    private String text;

    @Column(nullable = false)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Basic
    @Column(nullable = false)
    private Integer views = 0;

    @Basic
    @Column(nullable = false)
    private Double quality = 0d;

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


    // -- Tags -- ------------------------------------------------------------------------------------------------------

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_tag_mapping",
            joinColumns = {@JoinColumn(name = "articleId")},
            inverseJoinColumns = {@JoinColumn(name = "tagId")}
    )
    private Set<Tag> tags = new HashSet<Tag>();


    // -- Review -- ----------------------------------------------------------------------------------------------------

    @Basic
    private boolean featured;

    @Basic
    private String customTitle;

    @Lob
    private String customTextRendered;

    @Lob
    private String customTextMarkup;

    @Column
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date featuredTime;


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

    /**
     * @throws CuratorException if an assignment is illegal
     */
    @SuppressWarnings({"ConstantConditions"})
    public void validateFields() throws CuratorException {

        final List<String> missingFields = new LinkedList<String>();

        try {
            // -- Check fields
            for (Field field : getClass().getDeclaredFields()) {
                String fieldName = field.getName();
                Column column = field.getAnnotation(Column.class);
                try {
                    if (column != null) {
                        Object value = getFieldValue(field);
                        // -- Null-check for inconsistencies
                        boolean isEmpty = value == null || (value instanceof String && StringUtils.isBlank((String) value));
                        if (!column.nullable() && isEmpty)
                            if (!(missingFields.contains(fieldName))) missingFields.add(fieldName);

                        // -- Length
                        if (value instanceof String) {
                            String _value = StringUtils.trim((String) value);
                            int maxLength = column.length() / 2;
                            if ((!field.isAnnotationPresent(Lob.class)) && _value.length() > maxLength) {
                                throw new CuratorException(CuratorStatus.PARAMETER_TOO_LONG,
                                        fieldName + " is too long. Maximal length is " + maxLength);
                            }
                        }
                    }

                } catch (NoSuchMethodException e) {
                    // ignore
                }
            }
            if (!missingFields.isEmpty()) {
                throw new CuratorException(CuratorStatus.PARAMETER_MISSING,
                        "The following field(s) must be set: " + StringUtils.join(missingFields, ", "));
            }

        } catch (CuratorException e) {
            throw e;
        } catch (Throwable t) {
            throw new CuratorException("Article is invalid", t);
        }
    }

    private Object getFieldValue(Field field) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        field.setAccessible(true);
        return field.get(this);
    }

    private boolean isEmptyField(Field field) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object value = getFieldValue(field);

        return (value == null
                || (value instanceof String && StringUtils.isBlank((String) value)));
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

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
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

    public Double getQuality() {
        return quality;
    }

    public void setQuality(Double quality) {
        this.quality = quality;
    }

    public boolean setFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public Date getFeaturedTime() {
        return featuredTime;
    }

    public void setFeaturedTime(Date featuredTime) {
        this.featuredTime = featuredTime;
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
