package org.curator.core.interfaces;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.MetricProvider;

import javax.ejb.Local;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.net.URL;
import java.util.Date;
import java.util.List;

@Local
public interface ArticleManager {

    Article getById(long articleId);

    List<Article> getList(int firstResult, int maxResults);

    List<Article> getBest(int firstResult, int maxResults, Date _firstDate, Date lastDate);

    List<Article> getSuggest(int firstResult, int maxResults, Date _firstDate, Date lastDate);

    void addArticle(Article article) throws CuratorException;

    Article getByUrl(String url);

    Article publish(long articleId, String customText);

    List<Article> getPublished(Date firstDate, Date lastDate);

    URL redirect(long articleId);

    void removeIfUnrated();
}
