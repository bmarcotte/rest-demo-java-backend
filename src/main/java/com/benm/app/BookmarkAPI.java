package com.benm.app;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class BookmarkAPI {
  private Connection connection = null;
          String     dbfile;
  private String     dsn;
  private HashMap<String,PreparedStatement>
                     statement_cache = null;

  public BookmarkAPI ( String dbfile ) {
      this.dbfile = dbfile;
  }

  public String get_dsn() {
    if ( this.dsn == null && this.dbfile != null ) {
      this.dsn = "jdbc:sqlite::resource:" + this.dbfile;
    }

    return this.dsn;
  }

  public Connection open_connection() {
    if ( this.connection == null ) {
      String driver_class = "org.sqlite.JDBC";
      try {
        Class.forName( driver_class );
      }
      catch ( ClassNotFoundException ex ) {
        System.err.println( ex.getMessage() );
        return null;
      }

      String dsn = this.get_dsn();
      if ( dsn == null ) {
        System.err.println( "Couldn't generate a dsn.  Was dbfile set?" );
        return null;
      }

      try {
        this.connection = DriverManager.getConnection( dsn );
      }
      catch ( SQLException ex ) {
        System.err.println( ex.getMessage() );
      }
    }

    return this.connection;
  }

  public PreparedStatement get_statement( String sql ) {
    if ( this.statement_cache == null ) {
      this.statement_cache = new HashMap<String, PreparedStatement>();
    }

    if ( ! this.statement_cache.containsKey( sql ) ) {
      Connection conn = this.open_connection();
      if ( conn == null ) {
        return null;
      }
      try {
        PreparedStatement statement = conn.prepareStatement( sql );
        if ( statement != null ) {
          this.statement_cache.put( sql, statement );
        }
      }
      catch ( SQLException ex ) {
        System.err.println( ex.getMessage() );
        return null;
      }
    }

    return this.statement_cache.get( sql );
  }

  public String get_all_bookmarks() {
    PreparedStatement statement = this.get_statement(
      "SELECT * FROM bookmarks"
    );
    if ( statement == null ) {
      return null;
    }

    JsonArrayBuilder bookmarks = Json.createArrayBuilder();
    try {
      ResultSet result_set = statement.executeQuery();
      while ( result_set.next() ) {
        String name = result_set.getString( "name" );
        String url  = result_set.getString( "url"  );
        bookmarks.add(
          Json.createObjectBuilder()
            .add( "id",   result_set.getInt( "id" ) )
            .add( "name", name == null ? "" : name  )
            .add( "url",  url  == null ? "" : url   )
        );
      }
      result_set.close();
    }
    catch ( SQLException ex ) {
      System.err.println( ex.getMessage() );
      return null;
    }

    JsonObjectBuilder json_builder = Json.createObjectBuilder();
    return json_builder.add( "bookmarks", bookmarks ).build().toString();
  }

  public String get_bookmark( int id ) {
    PreparedStatement statement = this.get_statement(
      "SELECT * FROM bookmarks WHERE id = ?"
    );
    if ( statement == null ) {
      return null;
    }

    JsonObjectBuilder bookmark = Json.createObjectBuilder();

    try {
      statement.setInt( 1, id );

      ResultSet result_set = statement.executeQuery();
      while ( result_set.next() ) {
        String name = result_set.getString( "name" );
        String url  = result_set.getString( "url"  );
        bookmark.add( "id",   result_set.getInt( "id" ) )
                .add( "name", name == null ? "" : name  )
                .add( "url",  url  == null ? "" : url   );
      }
      result_set.close();
    }
    catch ( SQLException ex ) {
      System.err.println( ex.getMessage() );
      return null;
    }

    JsonObjectBuilder json_builder = Json.createObjectBuilder();
    return json_builder.add( "bookmark", bookmark ).build().toString();
  }

  private interface StatementBinder {
    public void bind( PreparedStatement statement )
      throws SQLException;
  }

  private String do_executeUpdate( String sql, StatementBinder binder ) {
    PreparedStatement statement = this.get_statement( sql );
    if ( statement == null ) {
      return null;
    }

    int rows_affected = 0;
    try {
      binder.bind( statement );
      rows_affected = statement.executeUpdate();
    }
    catch ( SQLException ex ) {
      System.err.println( ex.getMessage() );
    }

    return Json.createObjectBuilder()
          .add( "rows_affected", rows_affected )
          .build()
          .toString();
  }

  public String delete_bookmark( int id ) {
    return this.do_executeUpdate(
      "DELETE FROM bookmarks WHERE id = ?",
      ( statement ) -> {
        statement.setInt( 1, id );
      }
    );
  }

  public String insert_bookmark( String name, String url ) {
    return this.do_executeUpdate(
      "INSERT INTO bookmarks ( name, url ) VALUES ( ?, ? )",
      ( statement ) -> {
        statement.setString( 1, name );
        statement.setString( 2, url  );
      }
    );
  }

  public String update_bookmark( int id, String name, String url ) {
    return this.do_executeUpdate(
      "UPDATE bookmarks SET name = ?, url = ? WHERE id = ?",
      ( statement ) -> {
        statement.setString( 1, name );
        statement.setString( 2, url  );
        statement.setInt(    3, id   );
      }
    );
  }

  public void close_connection() {
    if ( this.statement_cache != null ) {
      for ( HashMap.Entry<String, PreparedStatement> entry : this.statement_cache.entrySet() ) {
        String            sql       = entry.getKey();
        PreparedStatement statement = entry.getValue();

        if ( statement != null ) {
          try {
            statement.close();
          }
          catch ( SQLException ex ) {
            System.err.println( ex.getMessage() );
          }
        }

        this.statement_cache.remove( sql );
      }
    }

    if ( this.connection != null ) {
      try {
        this.connection.close();
      }
      catch ( SQLException ex ) {
        System.err.println( ex.getMessage() );
      }
    }
  }
}
