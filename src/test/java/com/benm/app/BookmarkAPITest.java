package com.benm.app;

import java.io.StringReader;
import javax.json.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class BookmarkAPITest {
  private String      testdb = "data/bookmarks.db";
  private BookmarkAPI testapi = null;

  private BookmarkAPI api() {
    if ( testapi == null ) {
      testapi = new BookmarkAPI( testdb );
    }

    return testapi;
  }

  @Test
  public void constructor_with_null() {
    Object obj = this.assertNoException( () -> {
      return new BookmarkAPI( null );
    } );

    assertNotNull( obj );
    assertTrue( obj instanceof BookmarkAPI );
    assertNull( ((BookmarkAPI) obj).dbfile );
  }

  @Test
  public void constructor_with_filename() {
    Object obj = this.assertNoException( () -> {
      return this.api();
    } );

    assertNotNull( obj );
    assertTrue( obj instanceof BookmarkAPI );
    assertEquals( this.testdb, ((BookmarkAPI) obj).dbfile );
  }

  @Test
  public void get_dsn_with_filename() {
    String expected = "jdbc:sqlite::resource:" + this.testdb;
    assertEquals( expected, this.api().get_dsn() );
  }

  @Test
  public void get_dsn_with_null() {
    BookmarkAPI obj = new BookmarkAPI( null );
    assertNotNull( obj );
    assertNull( obj.get_dsn() );
  }

  @Test
  public void open_connection() {
    Object obj = this.assertNoException( () -> {
      return this.api().open_connection();
    } );
    assertNotNull( obj );
    assertTrue( obj instanceof java.sql.Connection );
  }

  @Test
  public void get_statement() {
    Object obj = this.assertNoException( () -> {
      return this.api().get_statement( "SELECT 1" );
    } );
    assertNotNull( obj );
    assertTrue( obj instanceof java.sql.PreparedStatement );
  }

  @Test
  public void get_all_bookmarks() {
    Object obj = this.assertNoException( () -> {
      return this.api().get_all_bookmarks();
    } );
    assertNotNull( obj );
    assertTrue( obj instanceof String );

    String json_string  = (String) obj;
    JsonObject json_obj = this.json_from_string( json_string );
    assertNotNull( json_obj );
    assertTrue( json_obj instanceof JsonObject );

    JsonArray bookmarks = json_obj.getJsonArray( "bookmarks" );
    assertNotNull( bookmarks );
    assertTrue( bookmarks.size() > 0 );
  }

  @Test
  public void get_bookmark() {
    this.assertNotBookmark( 0 );
    this.assertBookmark(    1, "Google", "https://www.google.com" );
    this.assertNotBookmark( 2 );
  }

  @Test
  public void insert_update_delete_bookmarks() {
    this.assertNumBookmarks( 1 );

    String insert_name = "Insert";
    String insert_url  = "http://insert.com";
    this.assertRowsAffected( () -> {
      return this.api().insert_bookmark( insert_name, insert_url );
    }, 1 );
    this.assertNumBookmarks( 2 );
    this.assertBookmark( 2, insert_name, insert_url );

    String update_name = "Update";
    String update_url  = "http://update.com";
    this.assertRowsAffected( () -> {
      return this.api().update_bookmark( 1, update_name, update_url );
    }, 1 );
    this.assertNumBookmarks( 2 );
    this.assertBookmark( 1, update_name, update_url );

    this.assertRowsAffected( () -> {
      return this.api().delete_bookmark( 1 );
    }, 1 );
    this.assertNumBookmarks( 1 );
    this.assertNotBookmark( 1 );
    this.assertBookmark( 2, insert_name, insert_url );
  }

  @Test
  public void close_connection() {
    this.assertNoException( () -> {
      this.api().close_connection();
      return null;
    } );
  }

  private static JsonObject json_from_string( String string ) {
      JsonReader reader = Json.createReader( new StringReader( string ) );
      JsonObject object = reader.readObject();
      reader.close();

      return object;
  }

  private interface ExceptionWrapper {
    public Object test_method()
      throws Exception;
  }

  private Object assertNoException( ExceptionWrapper wrapper ) {
    String message = null;
    Object result  = null;

    try {
      result = wrapper.test_method();
    }
    catch ( Exception error ) {
      message = error.getMessage();
    }
    assertNull( message );

    return result;
  }

  public void assertNumBookmarks( int expected ) {
    String json_string = this.api().get_all_bookmarks();
    assertNotNull( json_string );

    JsonObject json_obj = this.json_from_string( json_string );
    assertNotNull( json_obj );

    JsonArray bookmarks = json_obj.getJsonArray( "bookmarks" );
    assertNotNull( bookmarks );
    assertEquals( bookmarks.size(), expected );
  }

  public JsonObject assertGetBookmark( int index ) {
      Object obj = this.assertNoException( () -> {
        return this.api().get_bookmark( index );
      } );
      assertNotNull( obj );
      assertTrue( obj instanceof String );

      String json_string  = (String) obj;
      JsonObject json_obj = this.json_from_string( json_string );
      assertNotNull( json_obj );
      assertTrue( json_obj instanceof JsonObject );

      JsonObject bookmark = json_obj.getJsonObject( "bookmark" );
      assertNotNull( bookmark );

      return bookmark;
  }

  public void assertBookmark( int index, String expected_name, String expedted_url ) {
      JsonObject bookmark = this.assertGetBookmark( index );

      assertEquals( bookmark.getInt( "id" ), index );
      assertEquals( bookmark.getString( "name" ), expected_name );
      assertEquals( bookmark.getString( "url"  ), expedted_url  );
  }

  public void assertNotBookmark( int index ) {
      JsonObject bookmark = this.assertGetBookmark( index );

      assertFalse( bookmark.containsKey( "id"   ) );
      assertFalse( bookmark.containsKey( "name" ) );
      assertFalse( bookmark.containsKey( "url"  ) );
  }

  public void assertRowsAffected( ExceptionWrapper wrapper, int expected ) {
    Object obj = this.assertNoException( wrapper );
    assertNotNull( obj );
    assertTrue( obj instanceof String );

    JsonObject json_obj = this.json_from_string( (String) obj );
    assertNotNull( json_obj );
    assertTrue( json_obj instanceof JsonObject );

    assertTrue( json_obj.containsKey( "rows_affected" ) );
    assertEquals( json_obj.getInt( "rows_affected" ), expected );
  }
}
