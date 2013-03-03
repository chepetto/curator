package org.curator.core.services;

import org.apache.commons.lang.time.FastDateFormat;
import org.curator.common.cache.MethodCache;
import org.curator.common.configuration.Configuration;
import org.curator.common.configuration.CuratorInterceptors;
import org.curator.common.model.Article;
import org.curator.core.interfaces.ArticleManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CuratorInterceptors
@Path("/article")
public class ArticleService {

    @Inject
    private ArticleManager articleManager;

    private int maxResults;
    private FastDateFormat dateFormat;

    @PostConstruct
    public void onInit() {
        maxResults = Configuration.getIntValue("query.max.results", 1000);
        dateFormat = FastDateFormat.getInstance(Configuration.getStringValue(Configuration.REST_TIME_PATTERN, "yyyy-MM-dd HH:mm"));
    }

    @GET
    @MethodCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    public Response getById(
            @PathParam("id") long articleId
    ) throws Exception {
        return Response.ok(articleManager.getById(articleId));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/publish/{id}")
    public Response publish(
            @PathParam("id") long articleId,
            @QueryParam("text") @DefaultValue("") String customText,
            @QueryParam("title") @DefaultValue("") String customTitle
    ) throws Exception {
        Article article = articleManager.publish(articleId, customText, customTitle);
        return Response.ok(article);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/vote/{id}")
    public Response rate(
            @PathParam("id") long articleId,
            @QueryParam("rating") int rating,
            @Context HttpServletRequest request
    ) throws Exception {
        // todo test
        articleManager.vote(articleId, rating, request.getRemoteAddr());
        return Response.ok();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            Article article
    ) throws Exception {
        article = articleManager.addArticle(article);
        return Response.ok(article);
    }

    // -- LIST -- ------------------------------------------------------------------------------------------------------

    @GET
    @MethodCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list")
    public Response getList(

    ) throws Exception {
        return getList(0, maxResults);
    }

    @GET
    @MethodCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list/first:{firstResult}/max:{maxResults}")
    public Response getList(
            @PathParam("firstResult") int firstResult,
            @PathParam("maxResults") int maxResults
    ) throws Exception {
        Map<String, Object> response = new HashMap<String, Object>(5);
        response.put("firstResult", firstResult);
        List<Article> list = articleManager.getList(firstResult, maxResults);
        response.put("maxResults", list.size());
        response.put("list", list);
        return Response.ok(response);
    }

    // -- LIVE -- ------------------------------------------------------------------------------------------------------

    @GET
    @MethodCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list/live")
    public Response getLive(
            @QueryParam("firstResult") int firstResult,
            @QueryParam("maxResults") int maxResults,
            @QueryParam("firstDate") long firstDate,
            @QueryParam("lastDate") long lastDate
    ) throws Exception {
        Map<String, Object> response = new HashMap<String, Object>(5);

        response.put("firstResult", firstResult);

        Date _lastDate;
        if (lastDate == 0) {
            _lastDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3);
        } else {
            _lastDate = new Date(lastDate);
        }
        response.put("lastDate", dateFormat.format(_lastDate));

        Date _firstDate;
        if (firstDate == 0) {
            _firstDate = new Date();
        } else {
            _firstDate = new Date(firstDate);
        }
        response.put("firstDate", dateFormat.format(_firstDate));


        if (maxResults == 0) {
            maxResults = this.maxResults;
        }

        List<Article> list = articleManager.getLive(firstResult, maxResults, _firstDate, _lastDate);
        response.put("maxResults", list.size());
        response.put("list", list);
        return Response.ok(response);
    }

    // -- FEATURED -- --------------------------------------------------------------------------------------------------

    @GET
    @MethodCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list/featured")
    public Response getFeatured(

    ) throws Exception {
        Date now = new Date();
        Date oneWeekAgo = new Date(now.getTime() - 1000 * 60 * 60 * 24 * 7);
        return _getFeatured(now, oneWeekAgo);
    }

    @GET
    @MethodCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list/featured/firstDate:{firstDate}/lastDate:{lastDate}")
    public Response getFeatured(
            @PathParam("firstDate") long firstDate,
            @PathParam("lastDate") long lastDate
    ) throws Exception {
        return _getFeatured(new Date(firstDate), new Date(lastDate));
    }

    private Response _getFeatured(Date firstDate, Date lastDate) throws Exception {

        Map<String, Object> response = new HashMap<String, Object>(5);
        response.put("firstDate", dateFormat.format(firstDate));
        response.put("lastDate", dateFormat.format(lastDate));
        List<Article> list = articleManager.getFeatured(firstDate, lastDate);
        response.put("maxResults", list.size());
        response.put("list", list);
        return Response.ok(response);
    }
}
