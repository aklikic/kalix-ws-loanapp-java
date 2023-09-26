package io.kx.loanapp;

import io.kx.Main;
import kalix.javasdk.client.ComponentClient;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * This is a skeleton for implementing integration tests for a Kalix application built with the Java SDK.
 *
 * This test will initiate a Kalix Proxy using testcontainers and therefore it's required to have Docker installed
 * on your machine. This test will also start your Spring Boot application.
 *
 * Since this is an integration tests, it interacts with the application using a WebClient
 * (already configured and provided automatically through injection).
 */
@SpringBootTest(classes = Main.class)
public class IntegrationTest extends KalixIntegrationTestKitSupport {
  private static Logger logger = LoggerFactory.getLogger(IntegrationTest.class);
  @Autowired
  private ComponentClient clint;

  @Test
  public void loanAppHappyPath() throws Exception {
    var loanAppId = UUID.randomUUID().toString();
    var submitRequest = new LoanAppApi.SubmitRequest(
            "clientId",
            5000,
            2000,
            36);
    logger.info("Sending submit...");
    LoanAppApi.EmptyResponse emptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::submit).params(submitRequest).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertEquals(LoanAppApi.EmptyResponse.of(),emptyRes);

    logger.info("Sending get...");
    LoanAppApi.GetResponse getRes =
           componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);

    assertEquals(LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW,getRes.state().status());

    logger.info("Sending approve...");
    emptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::approve).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertEquals(LoanAppApi.EmptyResponse.of(),emptyRes);

    logger.info("Sending get...");
    getRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);

    assertEquals(LoanAppDomain.LoanAppDomainStatus.STATUS_APPROVED,getRes.state().status());
  }
}