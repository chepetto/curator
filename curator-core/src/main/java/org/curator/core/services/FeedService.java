package org.curator.core.services;

import org.curator.common.configuration.CuratorInterceptors;
import org.curator.common.model.Article;
import org.curator.common.model.Feed;
import org.curator.core.interfaces.FeedManager;
import org.curator.core.interfaces.TopicManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CuratorInterceptors
@Path("/feed")
public class FeedService {

    @Inject
    private FeedManager feedManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list")
    public Response list() throws Exception {
        Map<String, Object> response = new HashMap<String, Object>(5);
        response.put("firstResult", 0);
        List<Feed> list = feedManager.getList(0, 1000);
        response.put("maxResults", list.size());
        response.put("list", list);
        return Response.ok(response);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/status/{feedId}/{activate}")
    public Response setStatus(@PathParam("feedId") long feedId, @PathParam("activate") boolean activate) throws Exception {
        return Response.ok(feedManager.setStatus(feedId, activate));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/harvest/{feedId}")
    public Response forceHarvest(
            @PathParam("feedId") long feedId
    ) throws Exception {
        feedManager.forceHarvest(feedId);
        return Response.ok();
    }

}
