package org.curator.core.services;

import org.curator.common.configuration.CuratorInterceptors;
import org.curator.core.interfaces.ArticleManager;
import org.curator.core.interfaces.TopicManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@CuratorInterceptors
@Path("/topic")
public class TopicService {

    @Inject
    private TopicManager topicManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/s/")
    public Response get(@QueryParam("v") String value) throws Exception {
        return Response.ok(topicManager.getByValue(value));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/list")
    public Response list() throws Exception {
        return Response.ok(topicManager.getList());
    }

}
