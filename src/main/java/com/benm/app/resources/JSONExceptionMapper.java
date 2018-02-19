package com.benm.app.resources;

import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JSONExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Context
    HttpServletRequest request;

    @Override
    public Response toResponse( WebApplicationException exception ) {
      Response   response = exception.getResponse();
      StatusType status   = response.getStatusInfo();
      String     json     = Json.createObjectBuilder()
        .add( "code",    status.getStatusCode()    )
        .add( "message", exception.getMessage() )
        .add( "method",  request.getMethod() )
        .add( "reason",  status.getReasonPhrase() )
        .add( "status",  "ERROR" )
        .add( "uri",     request.getRequestURI() )
        .build()
        .toString();

      return Response.status( status )
        .entity( json )
        .type( MediaType.APPLICATION_JSON )
        .build();
    }
}
