package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.api.validation.ParameterType;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.ext.web.api.validation.ValidationException.ErrorType;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.model.Service.Status;

public class MainVerticle extends AbstractVerticle {

  Logger log = LoggerFactory.getLogger(MainVerticle.class);

  private DBService dbService;
  private BackgroundPoller poller;

  @Override
  public void start(Future<Void> startFuture) {
    final String dbPath = config().getString("db.path", DBConnector.DB_PATH);
    dbService = new DBService(new DBConnector(vertx, dbPath));
    poller = new BackgroundPoller(dbService, WebClient.create(vertx));

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    dbService.insertService("ALL", "kry", "https://www.kry.se", Status.UNKNOWN);
    vertx.setPeriodic(1000 * 20, timerId -> poller.pollServices());
    setRoutes(router);
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            log.info("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private void setRoutes(Router router) {
    router.route("/*").handler(StaticHandler.create());

    router.get("/service" + "/:user")
        .handler(this::getAllServices)
        .failureHandler(this::failureHandler);

    router.post("/service")
        .handler(createUrlValidator())
        .handler(this::createService)
        .failureHandler(this::failureHandler);

    router.delete("/service" + "/:id")
        .handler(HTTPRequestValidationHandler.create().addPathParam("id", ParameterType.INT))
        .handler(this::deleteService)
        .failureHandler(this::failureHandler);

  }

  private HTTPRequestValidationHandler createUrlValidator() {
    return HTTPRequestValidationHandler.create()
        .addCustomValidatorFunction(rc -> {
          String url = rc.getBodyAsJson().getString("url");
          try {
            new URL(url);
          } catch (MalformedURLException e) {
            throw new ValidationException("Invalid Service url: " + url,
                ErrorType.WRONG_CONTENT_TYPE, e);
          }
        });
  }

  private void getAllServices(RoutingContext rc) {
    String user = rc.request().getParam("user");
    dbService.getAllServices(user).setHandler(ar -> {
      if (ar.failed()) {
        log.error("Failed while loading services", ar.cause());
        rc.fail(ar.cause());
      } else {
        rc.response().setStatusCode(200)
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(new JsonArray(ar.result()).encode());
      }
    });
  }

  private void createService(RoutingContext rc) {
    JsonObject jsonBody = rc.getBodyAsJson();
    Future<UpdateResult> resultFuture =
        dbService.insertService(jsonBody.getString("user"), jsonBody.getString("name"),
            jsonBody.getString("url"), Status.UNKNOWN);
    if (resultFuture.failed()) {
      log.error("Failed while creating a service", resultFuture.cause());
      rc.fail(resultFuture.cause());
      rc.response().setStatusMessage("ERROR");
    } else {
      rc.response().setStatusCode(200)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(new JsonArray(String.valueOf(resultFuture.result())).encode());
    }
  }

  private void deleteService(RoutingContext rc) {
    Integer serviceId = Integer.parseInt(rc.request().getParam("id"));
    log.debug("Trying to delete service with id: " + serviceId);
    Future<UpdateResult> resultFuture = dbService.deleteService(serviceId);
    if (resultFuture.failed()) {
      log.error("Failed while deleting a service", resultFuture.cause());
      rc.fail(resultFuture.cause());
    } else {
      rc.response().setStatusCode(200)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(new JsonArray(String.valueOf(resultFuture.result())).encode());
    }
  }

  private void failureHandler(RoutingContext rc) {
    log.error(rc.failure().getMessage());
    JsonObject body = new JsonObject().put("error", rc.failure().getMessage());
    rc.response().setStatusCode(400).end(Json.encodePrettily(body));
  }


}



