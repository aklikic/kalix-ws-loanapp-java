package io.kx.loanapp;

import io.kx.Main;
import io.kx.loanproc.*;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
@TestPropertySource(locations="classpath:test-application.properties")
public class TimerIntegrationTest extends KalixIntegrationTestKitSupport {
  private static Logger logger = LoggerFactory.getLogger(TimerIntegrationTest.class);
  @Test
  public void endToEndHappyPathWithDeclineByTimeout() throws Exception {
    var loanAppId = UUID.randomUUID().toString();
    var submitRequest = new LoanAppApi.SubmitRequest(
            "clientId",
            5000,
            2000,
            36);
    logger.info("Sending submit...");
    LoanAppApi.EmptyResponse appEmptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::submit).params(submitRequest).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertEquals(LoanAppApi.EmptyResponse.of(),appEmptyRes);

    Thread.sleep(2000);

    LoanProcViewModel.ViewRecordList viewResList =
            componentClient.forView().call(LoanProcByStatusView::getLoanProcByStatus).params(new LoanProcViewModel.ViewRequest(LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name())).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertTrue(!viewResList.list().stream().filter(vr -> vr.loanAppId().equals(loanAppId)).findFirst().isPresent());

    Thread.sleep(2000);

    viewResList = componentClient.forView().call(LoanProcByStatusView::getLoanProcByStatus).params(new LoanProcViewModel.ViewRequest(LoanProcDomain.LoanProcDomainStatus.STATUS_DECLINED.name())).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    logger.info("viewResList: {}",viewResList);
    assertTrue(viewResList.list().stream().filter(vr -> vr.loanAppId().equals(loanAppId)).findFirst().isPresent());

    logger.info("Sending get...");
    LoanAppApi.GetResponse getRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanAppDomain.LoanAppDomainStatus.STATUS_DECLINED,getRes.state().status());
  }

}