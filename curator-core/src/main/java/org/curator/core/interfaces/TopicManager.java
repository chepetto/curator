package org.curator.core.interfaces;

import org.curator.common.model.Article;
import org.curator.common.model.Topic;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;
import java.util.Set;

@Local
public interface TopicManager {

    Topic getByValue(String name);

    void buildRelations(Set<String> relatedTopics);

    List<Topic> getList();

    Topic addNewTopic(Topic topic);
}
