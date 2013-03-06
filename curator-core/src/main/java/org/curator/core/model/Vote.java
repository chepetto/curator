package org.curator.core.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "Vote")
@Table(name = "Vote",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {Vote.FIELD_ARTICLE_ID, Vote.FIELD_USER_ID}
        )
)
@NamedQueries({
        @NamedQuery(name = Vote.QUERY_BY_ID, query = "SELECT a FROM Vote a where a.id=:ID"),
        @NamedQuery(name = Vote.QUERY_BY_ARTICLE_AND_USER, query = "SELECT a FROM Vote a where a.articleId=:ARTICLE_ID and a.userId=:USER_ID")
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Vote implements Serializable {

    public static final String QUERY_BY_ID = "Vote.QUERY_BY_ID";
    public static final String QUERY_BY_ARTICLE_AND_USER = "Vote.QUERY_BY_ARTICLE_AND_USER";
    public static final String FIELD_ARTICLE_ID = "articleId";
    public static final String FIELD_USER_ID = "userId";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Basic
    @Index(name = "articleIdx")
    @Column(name = Vote.FIELD_ARTICLE_ID, nullable = false)
    private long articleId;

    @Basic
    @Index(name = "userIdx")
    @Column(name = Vote.FIELD_USER_ID, nullable = false)
    private String userId;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getArticleId() {
        return articleId;
    }

    public void setArticleId(long articleId) {
        this.articleId = articleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
