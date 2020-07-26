package se.kry.codetest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle extends TestBase {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) throws SQLException {
    initDb();
    DeploymentOptions options = new DeploymentOptions()
        .setConfig(new JsonObject()
            .put("db.path", TEST_DB)
        );

    vertx.deployVerticle(new MainVerticle(), options,
        testContext.succeeding(id -> testContext.completeNow()));

  }

  @Test
  @DisplayName("Start a web server on localhost responding to path /service on port 8080")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void start_http_server(Vertx vertx, VertxTestContext testContext) {
    WebClient.create(vertx)
        .get(8080, "::1", "/service/ALL")
        .send(response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
          JsonArray body = response.result().bodyAsJsonArray();
          assertEquals(1, body.size());
          testContext.completeNow();
        }));
  }

  @Test
  @DisplayName("Should delete a service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void shoudl_delete_the_service(Vertx vertx, VertxTestContext testContext) {
    WebClient.create(vertx)
        .delete(8080, "::1", "/service/1")
        .send(response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
        }));

    //check it has been deleted ( we should probably test it on the databse directly )
    WebClient.create(vertx)
        .get(8080, "::1", "/service/ALL")
        .send(response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
          JsonArray body = response.result().bodyAsJsonArray();
          assertEquals(0, body.size());
          testContext.completeNow();
        }));
  }

  @Test
  @DisplayName("Should add services")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void shoudl_add_service(Vertx vertx, VertxTestContext testContext) {
    JsonObject body1 = new JsonObject()
        .put("user", "user1")
        .put("name", "Google").put("url", "https://google.com/");
    JsonObject body2 = new JsonObject()
        .put("user", "user1")
        .put("name", "Yahoo").put("url", "https://yahoo.com/");

    WebClient.create(vertx)
        .post(8080, "::1", "/service")
        .sendJsonObject(body1, response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
        }));
    WebClient.create(vertx)
        .post(8080, "::1", "/service")
        .sendJsonObject(body2, response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
        }));

    WebClient.create(vertx)
        .get(8080, "::1", "/service/user1")
        .send(response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
          JsonArray rbody = response.result().bodyAsJsonArray();
          // 1 + 2 (user=all + user=user1)
          assertEquals(3, rbody.size());
          testContext.completeNow();
        }));
  }


}
