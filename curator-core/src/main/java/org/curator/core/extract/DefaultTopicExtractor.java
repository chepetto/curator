package org.curator.core.extract;

import org.apache.log4j.Logger;
import org.curator.common.configuration.Configuration;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.*;
import org.curator.core.analysis.TermFrequencyUtils;
import org.curator.core.interfaces.TopicManager;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@LocalBean
@Stateless
public class DefaultTopicExtractor implements TopicExtractor {

    private static final Logger LOGGER = Logger.getLogger(DefaultTopicExtractor.class);

    @Inject
    private TermFrequencyUtils termFrequencyUtils;

    @Inject
    private TopicManager topicBean;
    private static final String KEY_MIN_TFIDF_SCORE = "score.min.tfidf";
    private double minTfidfScore;

    @PostConstruct
    public void onInit() {

        minTfidfScore = Configuration.getDoubleValue(KEY_MIN_TFIDF_SCORE, 20d);

        LOGGER.info(KEY_MIN_TFIDF_SCORE+": "+minTfidfScore);

    }

    @Override
    public Set<Topic> extract(Article article) {

        final Set<Topic> extracted = new HashSet<Topic>(20);
        try {

            // get words
            SortedSet<TermFrequencyUtils.Term> terms = termFrequencyUtils.tfidf(article);

            if(terms!=null) {
                for(TermFrequencyUtils.Term term : terms) {

                    if(!(term.getScore()> minTfidfScore && extracted.size()<10)) {
                        break;
                    }

                    final String value = term.getValue();

                    Topic topic = topicBean.getByValue(value);
                    if(topic == null) {
                        Topic newTopic = new Topic(value);
                        topicBean.addNewTopic(newTopic);

                        extracted.add(newTopic);

                    } else {
                        extracted.add(topic);
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.warn("Cannot extract topic of article "+article.getUrl()+": "+t.getMessage());
        }
        return extracted;
    }
}
