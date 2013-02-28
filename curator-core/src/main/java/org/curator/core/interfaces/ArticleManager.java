package org.curator.core.interfaces;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;

import javax.ejb.Local;
import java.net.URL;
import java.util.Date;
import java.util.List;

@Local
public interface ArticleManager {

    Article getById(long articleId);

    List<Article> getList(int firstResult, int maxResults);

    List<Article> getReview(int firstResult, int maxResults, Date _firstDate, Date lastDate);

    Article addArticleInternal(Article article) throws CuratorException;

    Article getByUrl(String url);

    Article publish(long articleId, String customText, String customTitle);

    void vote(long articleId, int rating, String remoteAddr) throws CuratorException;

    List<Article> getPublished(Date firstDate, Date lastDate);

    URL redirect(long articleId);

    void cleanup();

    Article addArticle(Article article) throws CuratorException;
}
