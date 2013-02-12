package org.curator.common.model;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.curator.common.service.CustomDateDeserializer;
import org.curator.common.service.CustomDateSerializer;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
@Entity(name = "Special")
@Table(name = "Special")
@NamedQueries({
        @NamedQuery(name = Special.QUERY_BY_ID, query = "SELECT a FROM Article a where a.id=:ID")
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Special implements Serializable {

    public static final String QUERY_BY_ID = "Special.QUERY_BY_ID";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Basic
    @Index(name = "urlIdx")
    @Column(nullable = false, unique = true)
    private String url;

    @Basic
    @Column(nullable = false, length = 512)
    private String title;

    @Basic
    @Column(nullable = false, length = 1024)
    private String description;

    @Column(nullable = false)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Basic
    private boolean active;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "special_id")
    private List<Article> articles = new LinkedList<Article>();

    public Special() {
        // default
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
