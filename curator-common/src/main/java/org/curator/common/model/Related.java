package org.curator.common.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Related")
@Table(name = "Related")
@NamedQueries({
        @NamedQuery(name = Related.QUERY_BY_ID, query = "SELECT a FROM Related a where a.id=:ID"),
        @NamedQuery(name = Related.QUERY_BY_VALUES, query = "SELECT a FROM Related a where LOWER(a.a)=LOWER(:A) AND LOWER(a.b)=LOWER(:B)"),
        @NamedQuery(name = Related.QUERY_ALL, query = "SELECT a FROM Related a")
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Related implements Serializable {

    public static final String QUERY_BY_ID = "Related.QUERY_BY_ID";
    public static final String QUERY_BY_VALUES = "Related.QUERY_BY_VALUES";
    public static final String QUERY_ALL = "Related.QUERY_ALL";

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private long id;

    @Id
    @Basic
    private String a;

    @Id
    @Basic
    private String b;

    @Basic
    private int frequency;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Related related = (Related) o;

        if (a != null ? !a.equals(related.a) : related.a != null) return false;
        if (b != null ? !b.equals(related.b) : related.b != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }
}

