package org.curator.core.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Tag")
@Table(name = "Tag")
@NamedQueries({
        @NamedQuery(name = Tag.QUERY_BY_ID, query = "SELECT a FROM Tag a where a.id=:ID"),
        @NamedQuery(name = Tag.QUERY_BY_VALUE, query = "SELECT a FROM Tag a where LOWER(a.value)=LOWER(:VAL)"),
        @NamedQuery(name = Tag.QUERY_ALL, query = "SELECT a FROM Tag a")
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Tag implements Serializable {

    public static final String QUERY_BY_ID = "Tag.QUERY_BY_ID";
    public static final String QUERY_BY_VALUE = "Tag.QUERY_BY_VALUE";
    public static final String QUERY_ALL = "Tag.QUERY_ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_tag_mapping",
            joinColumns = {@JoinColumn(name = "tagId")},
            inverseJoinColumns = {@JoinColumn(name = "articleId")}
    )
    private Set<Article> articles = new HashSet<Article>();

    @Basic
    @Index(name = "valueIdx")
    @Column(nullable = false, unique = true)
    private String value;

    public Tag() {
        //
    }

    public Tag(String value) {
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

}
