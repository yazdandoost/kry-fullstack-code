package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kry.codetest.model.Service.Status;

public class BackgroundPoller {

  Logger log = LoggerFactory.getLogger(BackgroundPoller.class);

  private DBService dbService;
  private final WebClient webClient;

  public BackgroundPoller(DBService dbService, WebClient webClient) {
    this.dbService = dbService;
    this.webClient = webClient;
  }

  public void pollServices() {
    log.info("Starting the poller");
    dbService.getAllServices().setHandler(
        req -> {
          if (req.succeeded()) {
            req.result().forEach(service -> checkService(service.getUrl())
                .map(status -> dbService.updateStatus(service.getId(), status)));
          } else {
            log.error("Could not load the services", req.cause());
          }
        }
    );
  }

  private Future<Status> checkService(final String url) {
    Future<Status> statusFuture = Future.future();
    try {
      webClient.getAbs(url).timeout(2000)
          .send(req -> statusFuture
              .complete(
                  req.succeeded() && req.result().statusCode() == 200 ? Status.OK : Status.FAIL));
    } catch (Exception e) {
      log.error("Error while calling the service url " + url + ": " + e.getMessage());
    }
    return statusFuture;
  }

}
