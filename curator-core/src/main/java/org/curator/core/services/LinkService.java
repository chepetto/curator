package org.curator.core.services;

import org.curator.common.configuration.CuratorInterceptors;
import org.curator.core.interfaces.ArticleManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;
import java.net.URL;

@CuratorInterceptors
@Path("/link")
public class LinkService {

    @Inject
    private ArticleManager articleManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    public Response redirect(@PathParam("id") long articleId) throws Exception {
        return Response.temporaryRedirect(articleManager.redirect(articleId).toURI()).build();
    }
}
