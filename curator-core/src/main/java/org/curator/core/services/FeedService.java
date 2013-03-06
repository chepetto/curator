package org.curator.core.services;

import org.curator.common.cache.MethodCache;
import org.curator.common.configuration.CuratorInterceptors;
import org.curator.core.interfaces.FeedManager;
import org.curator.core.model.Feed;
import org.curator.core.status.FeedsStatus;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CuratorInterceptors
@Path("/feed")
public class FeedService {

    @Inject
    private FeedManager feedManager;

    @GET
    @MethodCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/status/all")
    public Response getStatusAll() throws Exception {
        FeedsStatus status = feedManager.getStatusOfAll();
        return Response.ok(status);
    }

    @GET
    @MethodCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/status/{feedId}")
    public Response getStatus(@PathParam("feedId") long feedId) throws Exception {
        return Response.ok(feedManager.getById(feedId));
    }

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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/create/")
    public Response createFeed(
            @QueryParam("url") @DefaultValue("") String feedUrl
    ) throws Exception {

        Feed f = new Feed(new URI(feedUrl));
        f.setActive(false);

        return Response.ok(feedManager.add(f));
    }
}
