package com.benm.app.resources;

import com.benm.app.BookmarkAPI;
import com.benm.app.Bookmark;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;

@Path( "bookmark" )
@Produces( MediaType.APPLICATION_JSON )
public class BookmarkEndpoint {
  @Context
  ServletContext context;

  private BookmarkAPI api = null;

  private String dbfile() {
    return context != null
      ? context.getInitParameter( "dbfile" )
      : "";
  }

  private BookmarkAPI api() {
    if ( this.api == null ) {
      this.api = new BookmarkAPI( this.dbfile() );
    }

    return this.api;
  }

  @GET
	@Path( "{id : \\d+}" )
	public Response get_path(
    @PathParam( "id" ) String id
  ) {
    return Response.ok(
      this.api().get_bookmark( Integer.parseInt( id ) )
    ).build();
	}

  @POST
  @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
  public Response bookmark_form(
    @FormParam( "name" ) String name,
    @FormParam( "url"  ) String url
  ) {
    return Response.ok(
      this.api().insert_bookmark( name, url )
    ).build();
  }

  @POST
  @Consumes( MediaType.APPLICATION_JSON )
  public Response bookmark_json( Bookmark bookmark ) {
    return Response.ok(
      this.api().insert_bookmark( bookmark.name, bookmark.url )
    ).build();
  }

  @PUT
  @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
  public Response put_form(
    @FormParam( "id"   ) int id,
    @FormParam( "name" ) String name,
    @FormParam( "url"  ) String url
  ) {
    return Response.ok(
      this.api().update_bookmark( id, name, url )
    ).build();
  }

  @PUT
  @Consumes( MediaType.APPLICATION_JSON )
  public Response put_json( Bookmark bookmark ) {
    return Response.ok(
      this.api().update_bookmark( bookmark.id, bookmark.name, bookmark.url )
    ).build();
  }

  @DELETE
  @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
  public Response delete_form(
    @FormParam( "id" ) int id
  ) {
    return Response.ok(
      this.api().delete_bookmark( id )
    ).build();
  }

  @DELETE
  @Consumes( MediaType.APPLICATION_JSON )
  public Response delete_json( Bookmark bookmark ) {
    return Response.ok(
      this.api().delete_bookmark( bookmark.id )
    ).build();
  }

  @DELETE
	@Path( "{id : \\d+}" )
	public Response delete_path(
    @PathParam( "id" ) String id
  ) {
    return Response.ok(
      this.api().delete_bookmark( Integer.parseInt( id ) )
    ).build();
	}
}
