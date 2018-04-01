package io.openshift.booster;

import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static io.restassured.RestAssured.get;
import static org.hamcrest.core.Is.is;

/**
 * Test the {@link NameServiceVerticle}.
 */
public class NameServiceVerticleTest {


    private Vertx vertx;

    @Before
    public void setUp() throws InterruptedException {
        vertx = Vertx.vertx();
        CountDownLatch latch = new CountDownLatch(1);
        JsonObject config = new JsonObject().put("http.port", 8081).put("name", "neo");
        vertx.deployVerticle(NameServiceVerticle.class.getName(),
            new DeploymentOptions().setConfig(config),
            x -> latch.countDown());
        RestAssured.baseURI = "http://localhost:8081";
        latch.await();
    }

    @Test
    public void testName() {
        get("/api/name")
            .then()
            .statusCode(200)
            .body(is("neo"));
    }

    @After
    public void tearDown() {
        vertx.close();
    }

}