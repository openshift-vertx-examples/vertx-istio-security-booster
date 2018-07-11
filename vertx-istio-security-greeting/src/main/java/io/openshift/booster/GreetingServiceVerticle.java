package io.openshift.booster;

import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.handler.StaticHandler;

/**
 * Greeting service using the Name Service
 */
public class GreetingServiceVerticle extends AbstractVerticle {

  private WebClient client;

  @Override
  public void start(Future<Void> future) {
    client = WebClient.create(vertx,
      new WebClientOptions()
        .setDefaultPort(8080)
        .setDefaultHost("vertx-istio-security-name")
        .setConnectTimeout(1000)
        .setIdleTimeout(1000)
    );

    Router router = Router.router(vertx);
    router.route().handler(rc -> {
      System.out.println(rc.request().method() + " " + rc.request().path());
      rc.next();
    });
    router.get("/api/greeting").handler(this::invoke);
    router.get("/health").handler(rc -> rc.response().end("ok"));
    router.get("/*").handler(StaticHandler.create());

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .rxListen(config().getInteger("http.port", 8080))
      .toCompletable()
      .subscribe(CompletableHelper.toObserver(future));
  }

  private void invoke(RoutingContext rc) {
    client.get("/api/name")
      .rxSend()
      .map(HttpResponse::bodyAsString)
      .map(name -> String.format("Hello, %s!", name))
      .subscribe(
        payload -> rc.response().end(payload),
        rc::fail
      );
  }
}
