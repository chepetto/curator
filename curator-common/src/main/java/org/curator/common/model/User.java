package org.curator.common.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "User")
@Table(name = "User")
@NamedQueries({
        @NamedQuery(name = User.QUERY_BY_ID, query = "SELECT a FROM User a where a.id=:ID"),
        @NamedQuery(name = User.QUERY_BY_USERNAME, query = "SELECT a FROM User a where LOWER(a.username)=LOWER(:USERNAME)"),
        @NamedQuery(name = User.QUERY_ALL, query = "SELECT a FROM User a")
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User implements Serializable {

    public static final String QUERY_BY_ID = "User.QUERY_BY_ID";
    public static final String QUERY_BY_USERNAME = "User.QUERY_BY_USERNAME";
    public static final String QUERY_ALL = "User.QUERY_ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Basic
    @Index(name = "usernameIdx")
    @Column(nullable = false, unique = true)
    private String username;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_topic_mapping",
            joinColumns = {@JoinColumn(name = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "topicId")}
    )
    private Set<Topic> topics = new HashSet<Topic>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Set<Topic> topics) {
        this.topics = topics;
    }
}
