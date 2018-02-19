package com.benm.app;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath( "/rest" )
public class JerseyApp extends ResourceConfig {
  public JerseyApp() {
    packages( "com.benm.app.resources" );
  }
}
