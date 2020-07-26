package se.kry.codetest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vertx.core.Vertx;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.kry.codetest.model.Service.Status;

@ExtendWith(VertxExtension.class)
public class TestDBService extends TestBase {

  private DBService dbService;

  @BeforeEach
  void init(Vertx vertx) throws Exception {
    initDb();
    dbService = new DBService(new DBConnector(vertx, TEST_DB));
  }

  @Test
  @DisplayName("Select all on empty db should return 0")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getAllServices_should_return_only_one_service(Vertx vertx, VertxTestContext testContext) {
    dbService.getAllServices().setHandler(res -> testContext.verify(() -> {
          assertEquals(0, res.result().size());
          testContext.completeNow();
        })
    );
  }

  @Test
  @DisplayName("Should insert a new service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void should_inser_new_service(Vertx vertx, VertxTestContext testContext) throws Exception {
    dbService.insertService("1", "google", "http://www.google.com", Status.UNKNOWN)
        .setHandler(res -> testContext.verify(() -> {
              assertTrue(res.succeeded());
              assertEquals(1, res.result().getUpdated());
              testContext.completeNow();
            })
        );
  }

  @Test
  @DisplayName("Delete service should delete a record if it exists in db")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deleteService_should_delete_a_service_if_it_exists(Vertx vertx, VertxTestContext testContext)
      throws Exception {
    dbService.insertService("user1", "google", "http://google.com", Status.UNKNOWN);
    dbService.deleteService(1).setHandler(res -> testContext.verify(() -> {
          assertTrue(res.succeeded());
          UpdateResult updateResult = res.result();
          assertEquals(1, updateResult.getUpdated());
          testContext.completeNow();
        })
    );
  }

  @Test
  @DisplayName("Delete service should not delete a record if it does not exist in db")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deleteService_should_not_delete_a_service_if_it_does_not_exist(Vertx vertx,
      VertxTestContext testContext)
      throws Exception {
    dbService.deleteService(1).setHandler(res -> testContext.verify(() -> {
          assertTrue(res.succeeded());
          UpdateResult updateResult = res.result();
          assertEquals(0, updateResult.getUpdated());
          testContext.completeNow();
        })
    );
  }

  @Test
  @DisplayName("Updating status should update the record in db")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void should_update_status(Vertx vertx, VertxTestContext testContext)
      throws Exception {
    dbService.insertService("user1", "google", "http://google.com", Status.UNKNOWN);
    dbService.updateStatus(1, Status.FAIL);

    dbService.getAllServices().setHandler(res -> testContext.verify(() -> {
          assertEquals(1, res.result().size());
          assertEquals(res.result().get(0).getStatus(), Status.FAIL.name());
          testContext.completeNow();
        })
    );
  }



}