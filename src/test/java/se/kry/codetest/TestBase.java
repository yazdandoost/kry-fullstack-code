package se.kry.codetest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;

public abstract class TestBase {

  Logger log = LoggerFactory.getLogger(TestMainVerticle.class);

  public static final String TEST_DB = "services-test.db";

  protected void initDb() throws SQLException {
    DriverManager.registerDriver(new JDBC());
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + TEST_DB)) {
      conn.createStatement().execute("DROP TABLE IF EXISTS service; ");
      conn.createStatement().execute(DBConnector.CREATE_TABLE);
    } catch (Exception e) {
      log.error(e.getMessage());
    }

  }
}
