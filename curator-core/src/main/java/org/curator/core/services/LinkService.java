package org.curator.core.services;

import org.curator.common.cache.MethodCache;
import org.curator.common.configuration.CuratorInterceptors;
import org.curator.core.interfaces.ArticleManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@CuratorInterceptors
@Path("/link")
public class LinkService {

    @Inject
    private ArticleManager articleManager;

    @GET
    @MethodCache
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/{id}")
    public Response redirect(@PathParam("id") long articleId) throws Exception {
        return Response.temporaryRedirect(articleManager.redirect(articleId).toURI()).build();
    }
}
