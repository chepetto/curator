package org.curator.core.dao;

import org.apache.log4j.Logger;
import org.curator.common.model.*;
import org.curator.core.interfaces.TopicManager;
import org.hibernate.Hibernate;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@LocalBean
@Stateless
@TransactionAttribute(TransactionAttributeType.NEVER)
public class TopicManagerBean implements TopicManager {

    private static final Logger LOGGER = Logger.getLogger(TopicManagerBean.class);

    @PersistenceContext(unitName = "primary")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void buildRelations(Set<String> relatedTopics) {

        if(relatedTopics==null) {
            throw new IllegalArgumentException("topics-collection is null");
        }

        if(relatedTopics.isEmpty()) {
            return;
        }


        buildTermRelations(relatedTopics);
//        buildTopicRelations(relatedTopics);
    }

    public void buildTermRelations(Set<String> relatedTopics) {
        try {

            // sort
            List<String> terms = new LinkedList<String>();
            terms.addAll(relatedTopics);
            Collections.sort(terms, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });



            for(int i=0; i<terms.size(); i++) {
                String topicA = terms.get(i);

                for(int j=i+1; j<terms.size(); j++) {
                    String topicB = terms.get(j);

                    Query query = em.createNamedQuery(Related.QUERY_BY_VALUES);
                    query.setParameter("A",topicA);
                    query.setParameter("B",topicB);

                    List<Related> list = query.getResultList();
                    if(list.isEmpty()) {
                        LOGGER.trace(String.format("create relation %s<->%s", topicA, topicB));

                        Related related = new Related();
                        related.setA(topicA);
                        related.setB(topicB);
                        related.setFrequency(1);
                        em.persist(related);

                    } else {
                        LOGGER.trace(String.format(" already exists %s<->%s", topicA, topicB));

                        Related related = list.get(0);
                        related.setFrequency(related.getFrequency()+1);
                        em.merge(related);
                    }


                }
            }

            em.flush();

        } catch (Throwable t) {
            LOGGER.error("build relations failed: "+relatedTopics, t);
        }
    }


    public void buildTopicRelations(Set<String> relatedTopics) {
        try {

            // resolve
            LOGGER.trace(String.format("resolve %s terms",relatedTopics.size()));
            List<Topic> resolved = new ArrayList<Topic>(relatedTopics.size());
            for(String unresolved:relatedTopics) {
                LOGGER.trace("resolve term as topic " + unresolved);
                Topic t = getByValue(unresolved);
                if(t==null) {
                    LOGGER.trace("new topic "+unresolved);
                    t = new Topic(unresolved);
                    em.persist(t);
                    em.flush();
                    em.refresh(t);
                }
                resolved.add(t);
            }



            for(int i=0; i<resolved.size(); i++) {
                Topic topicA = resolved.get(i);
                Hibernate.initialize(topicA.getRelatedTo());

                for(int j=i+1; j<resolved.size(); j++) {
                    Topic topicB = resolved.get(j);

                    if(topicA.getRelatedTo().contains(topicB)) {
                        LOGGER.trace(String.format("relation already exists %s<->%s", topicA, topicB));
                        continue;
                    }

                    LOGGER.trace(String.format("create relation %s<->%s", topicA, topicB));

                    topicA.getRelatedTo().add(topicB);
                }

                em.merge(topicA);
            }

            em.flush();

        } catch (Throwable t) {
            LOGGER.error("build relations failed: "+relatedTopics, t);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Topic getByValue(String name) {
        Query query = em.createNamedQuery(Topic.QUERY_BY_VALUE);
        query.setParameter("VAL", name);
        query.setMaxResults(1);
        List list = query.getResultList();
        if(list.isEmpty()) {
            return null;
        }
        Topic t = (Topic) list.get(0);

        Hibernate.initialize(t.getRelatedTo());
        Hibernate.initialize(t.getRelatedFrom());

        HashSet<Topic> relatives = new HashSet<Topic>();
        relatives.addAll(t.getRelatedTo());
        relatives.addAll(t.getRelatedFrom());

        t.setRelatives(relatives);

        return t;

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Topic> getList() {
        Query query = em.createNamedQuery(Topic.QUERY_ALL);
        //noinspection unchecked
        return query.getResultList();

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Topic addNewTopic(Topic topic) {
        em.persist(topic);
        em.refresh(topic);
        return topic;
    }

}
