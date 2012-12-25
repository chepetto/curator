package org.curator.core.extract;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.Topic;

import java.util.Set;

public interface TopicExtractor {
    Set<Topic> extract(Article article);
}
