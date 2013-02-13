package org.curator.common.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;

@Entity(name = "MetricResult")
@Table(name = "MetricResult",
        uniqueConstraints = @UniqueConstraint(columnNames = {"metric", "articleId"})
)
@IdClass(MetricResultId.class)
@JsonIgnoreProperties(ignoreUnknown = true)
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MetricResult {

    @Id
    @Column(name = "metric")
    private MetricName metricName;

    @Id
    @Column(name = "articleId")
    @JsonIgnore
    private long articelId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false,
            targetEntity = Article.class,
            cascade = {}
    )
    @JoinColumn(
            name = "articleId",
            insertable = false,
            updatable = false,
            nullable = false
    )
    @JsonIgnore
    private Article article;

    @Basic
    private double result;

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

//    public long getId() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }


    public MetricName getMetricName() {
        return metricName;
    }

    public void setMetricName(MetricName metricName) {
        this.metricName = metricName;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public long getArticelId() {
        return articelId;
    }

    public void setArticelId(long articelId) {
        this.articelId = articelId;
    }
}
