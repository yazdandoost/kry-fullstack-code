package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.UpdateResult;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBConnector {

  public static final String DB_PATH = "services.db";
  private final SQLClient client;
  final static ExecutorService executorService = Executors.newFixedThreadPool(1);

  public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS service (" +
      " user VARCHAR(30) NOT NULL," +
      " name VARCHAR(255) NOT NULL," +
      " url VARCHAR(255) NOT NULL, " +
      " status VARCHAR(10) NOT NULL DEFAULT 'UNKNOWN'," +
      " created TIMESTAMP NOT NULL " +
      " );";


  public DBConnector(Vertx vertx, String dbPath) {
    JsonObject config = new JsonObject()
        .put("url", "jdbc:sqlite:" + dbPath)
        .put("driver_class", "org.sqlite.JDBC")
        .put("max_pool_size", 30);

    client = JDBCClient.createShared(vertx, config);
  }

  public Future<ResultSet> query(String query) {
    return query(query, new JsonArray());
  }

  public Future<ResultSet> query(String query, JsonArray params) {
    if (query == null || query.isEmpty()) {
      return Future.failedFuture("Query is null or empty");
    }
    if (!query.endsWith(";")) {
      query = query + ";";
    }

    Future<ResultSet> queryResultFuture = Future.future();

    String finalQuery = query;
    executorService.submit(() -> client.queryWithParams(finalQuery, params, result -> {
      if (result.failed()) {
        queryResultFuture.fail(result.cause());
      } else {
        queryResultFuture.complete(result.result());
      }
    }));
    return queryResultFuture;
  }

  public Future<UpdateResult> updateQuery(String query, JsonArray params) {
    if (query == null || query.isEmpty()) {
      return Future.failedFuture("Query is null or empty");
    }
    if (!query.endsWith(";")) {
      query = query + ";";
    }
    Future<UpdateResult> updateResultFuture = Future.future();
    String finalQuery = query;
    executorService.submit(() -> client.updateWithParams(finalQuery, params, result -> {
      if (result.failed()) {
        updateResultFuture.fail(result.cause());
      } else {
        updateResultFuture.complete(result.result());
      }
    }));
    return updateResultFuture;
  }

}
