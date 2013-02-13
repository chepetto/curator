package org.curator.common.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Topic")
@Table(name = "Topic")
@NamedQueries({
        @NamedQuery(name = Topic.QUERY_BY_ID, query = "SELECT a FROM Topic a where a.id=:ID"),
        @NamedQuery(name = Topic.QUERY_BY_VALUE, query = "SELECT a FROM Topic a where LOWER(a.value)=LOWER(:VAL)"),
        @NamedQuery(name = Topic.QUERY_ALL, query = "SELECT a FROM Topic a")
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Topic implements Serializable {

    public static final String QUERY_BY_ID = "Topic.QUERY_BY_ID";
    public static final String QUERY_BY_VALUE = "Topic.QUERY_BY_VALUE";
    public static final String QUERY_ALL = "Topic.QUERY_ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_topic_mapping",
            joinColumns = {@JoinColumn(name = "topicId")},
            inverseJoinColumns = {@JoinColumn(name = "articleId")}
    )
    private Set<Article> articles = new HashSet<Article>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "related_topics",
            joinColumns = {@JoinColumn(name = "topicId")},
            inverseJoinColumns = {@JoinColumn(name = "relatedId")}
    )
    private Set<Topic> relatedTo = new HashSet<Topic>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "related_topics",
            joinColumns = {@JoinColumn(name = "relatedId")},
            inverseJoinColumns = {@JoinColumn(name = "topicId")}
    )
    private Set<Topic> relatedFrom = new HashSet<Topic>();

    @Transient
    private Set<Topic> relatives;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_topic_mapping",
            joinColumns = {@JoinColumn(name = "topicId")},
            inverseJoinColumns = {@JoinColumn(name = "userId")}
    )
    private Set<User> users = new HashSet<User>();
    @Basic
    @Index(name = "valueIdx")
    @Column(nullable = false, unique = true)
    private String value;

    public Topic() {
        //
    }

    public Topic(String value) {
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

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Topic> getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(Set<Topic> relatedTo) {
        this.relatedTo = relatedTo;
    }

    public Set<Topic> getRelatedFrom() {
        return relatedFrom;
    }

    public void setRelatedFrom(Set<Topic> relatedFrom) {
        this.relatedFrom = relatedFrom;
    }

    public Set<Topic> getRelatives() {
        return relatives;
    }

    public void setRelatives(Set<Topic> relatives) {
        this.relatives = relatives;
    }

    @Override
    public String toString() {
        return String.format("Topic{id:%s v:%s}", id, value);
    }
}
