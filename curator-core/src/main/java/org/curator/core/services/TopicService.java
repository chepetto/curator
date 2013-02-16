package org.curator.core.services;

import org.curator.common.configuration.CuratorInterceptors;
import org.curator.core.interfaces.TopicManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@CuratorInterceptors
@Path("/topic")
@Deprecated
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
