package com.benm.app.resources;

import com.benm.app.BookmarkAPI;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path( "bookmarks" )
@Produces( MediaType.APPLICATION_JSON )
public class BookmarksEndpoint {
  @Context
  ServletContext context;

  private String dbfile() {
    return context != null
      ? context.getInitParameter( "dbfile" )
      : "";
  }

  @GET
  public Response get_all_bookmarks() {
    BookmarkAPI api = new BookmarkAPI( dbfile() );

    return Response.ok(
      api.get_all_bookmarks()
    ).build();
  }
}
