package org.curator.core.services;

import org.apache.commons.lang.time.FastDateFormat;
import org.curator.common.configuration.Configuration;
import org.curator.common.configuration.CuratorInterceptors;
import org.curator.common.model.Article;
import org.curator.common.service.CustomDateSerializer;
import org.curator.core.interfaces.ArticleManager;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.*;

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
            @QueryParam("custom") @DefaultValue("") String customText
    ) throws Exception {
        Article article = articleManager.publish(articleId, customText);
        return Response.ok(article);
    }

    // -- LIST -- ------------------------------------------------------------------------------------------------------

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list")
    public Response getList(

    ) throws Exception {
        return getList(0, maxResults);
    }

    @GET
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

    // -- BEST -- ------------------------------------------------------------------------------------------------------

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list/best")
    public Response getBest(
            @QueryParam("firstResult") int firstResult,
            @QueryParam("maxResults") int maxResults,
            @QueryParam("firstDate") long firstDate,
            @QueryParam("lastDate") long lastDate
    ) throws Exception {
        Map<String, Object> response = new HashMap<String, Object>(5);

        response.put("firstResult", firstResult);

        Date _lastDate;
        if(lastDate==0) {
            _lastDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        } else {
            _lastDate = new Date(lastDate);
        }
        response.put("lastDate", dateFormat.format(_lastDate));

        Date _firstDate;
        if(firstDate==0) {
            _firstDate = new Date();
        } else {
            _firstDate = new Date(firstDate);
        }
        response.put("firstDate", dateFormat.format(_firstDate));


        if(maxResults==0) {
            maxResults = this.maxResults;
        }

        List<Article> list = articleManager.getBest(firstResult, maxResults, _firstDate, _lastDate);
        response.put("maxResults", list.size());
        response.put("list", list);
        return Response.ok(response);
    }


    // -- SUGGEST -- ---------------------------------------------------------------------------------------------------

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list/suggest")
    public Response getSuggest(
            @QueryParam("firstResult") int firstResult,
            @QueryParam("maxResults") int maxResults,
            @QueryParam("firstDate") long firstDate,
            @QueryParam("lastDate") long lastDate
    ) throws Exception {
        Map<String, Object> response = new HashMap<String, Object>(5);

        response.put("firstResult", firstResult);

        Date _lastDate;
        if(lastDate==0) {
            _lastDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5);
        } else {
            _lastDate = new Date(lastDate);
        }
        response.put("lastDate", dateFormat.format(_lastDate));

        Date _firstDate;
        if(firstDate==0) {
            _firstDate = new Date();
        } else {
            _firstDate = new Date(firstDate);
        }
        response.put("firstDate", dateFormat.format(_firstDate));


        if(maxResults==0) {
            maxResults = this.maxResults;
        }

        List<Article> list = articleManager.getSuggest(firstResult, maxResults, _firstDate, _lastDate);
        response.put("maxResults", list.size());
        response.put("list", list);
        return Response.ok(response);
    }

    // -- PUBLISHED -- -------------------------------------------------------------------------------------------------

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list/published")
    public Response getPublished(

    ) throws Exception {
        Date now = new Date();
        Date oneWeekAgo = new Date(now.getTime() - 1000*60*60*24*7);
        return _getPublished(now, oneWeekAgo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list/published/firstDate:{firstDate}/lastDate:{lastDate}")
    public Response getPublished(
            @PathParam("firstDate") long firstDate,
            @PathParam("lastDate") long lastDate
    ) throws Exception {
        return _getPublished(new Date(firstDate), new Date(lastDate));
    }

    private Response _getPublished(Date firstDate, Date lastDate) throws Exception {

        Map<String, Object> response = new HashMap<String, Object>(5);
        response.put("firstDate", dateFormat.format(firstDate));
        response.put("lastDate", dateFormat.format(lastDate));
        List<Article> list = articleManager.getPublished(firstDate, lastDate);
        response.put("maxResults", list.size());
        response.put("list", list);
        return Response.ok(response);
    }
}
