package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.UpdateResult;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import se.kry.codetest.model.Service;
import se.kry.codetest.model.Service.Status;
import se.kry.codetest.model.ServiceDTO;

public class DBService {

  private final DBConnector connector;

  public DBService(DBConnector connector) {
    this.connector = connector;
  }

  public Future<List<ServiceDTO>> getAllServices() {
    return getAllServices(null);
  }

  public Future<List<ServiceDTO>> getAllServices(String user) {
    return connector.query(
        "SELECT rowid, name, url, status, created FROM service "
            + (user != null ? "WHERE user = 'ALL' OR user = '" + user + "'" : "")
            + " ;")
        .map(
            resultSet -> resultSet.getRows(true).stream()
                .map(object -> new Service(object.getInteger("rowid"),
                    object.getString("name"), object.getString("url"),
                    Status.from(object.getString("status")),
                    Instant.ofEpochMilli(object.getLong("created"))))
                .map(ServiceDTO::from)
                .collect(Collectors.toList())
        );
  }

  public Future<UpdateResult> insertService(String user, String name, String url, Service.Status status) {
    return connector.updateQuery(
        "INSERT INTO service (user, name, url, status, created) VALUES (?, ?, ?, ?, ?);",
        new JsonArray().add(user).add(name).add(url).add(status.name()).add(Instant.now()));
  }

  public Future<UpdateResult> deleteService(Integer serviceId) {
    return connector.updateQuery("DELETE FROM service WHERE rowid = ?;",
        new JsonArray().add(serviceId));
  }

  public Future<UpdateResult> updateStatus(Integer serviceId, Status status) {
    return connector.updateQuery("UPDATE service SET status = ? WHERE rowid = ?;",
        new JsonArray().add(status.name()).add(serviceId));
  }

}
