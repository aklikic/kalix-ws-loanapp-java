package io.kx.loanapp;

import io.kx.Main;
import io.kx.loanproc.*;
import kalix.javasdk.client.ComponentClient;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;
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

  @Test
  public void loanProcHappyPath() throws Exception {
    var loanAppId = UUID.randomUUID().toString();
    var reviewerId = "99999";

    logger.info("Sending process...");
    LoanProcApi.EmptyResponse emptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::process).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);

    assertEquals(LoanProcApi.EmptyResponse.of(),emptyRes);

    logger.info("Sending get...");
    LoanProcApi.GetResponse getRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW, getRes.state().status());

    logger.info("Sending approve...");
    emptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::approve).params(new LoanProcApi.ApproveRequest(reviewerId)).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanProcApi.EmptyResponse.of(),emptyRes);

    logger.info("Sending get...");
    getRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);

    assertEquals(LoanProcDomain.LoanProcDomainStatus.STATUS_APPROVED,getRes.state().status());
  }
  @Test
  public void loanProcHappyPathWithView() throws Exception {
    var loanAppId = UUID.randomUUID().toString();
    var reviewerId = "99999";

    logger.info("Sending process...");
    LoanProcApi.EmptyResponse emptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::process).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);

    assertEquals(LoanProcApi.EmptyResponse.of(),emptyRes);

    logger.info("Sending get...");
    LoanProcApi.GetResponse getRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW, getRes.state().status());

    logger.info("Sending approve...");
    emptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::approve).params(new LoanProcApi.ApproveRequest(reviewerId)).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanProcApi.EmptyResponse.of(),emptyRes);

    logger.info("Sending get...");
    getRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanProcDomain.LoanProcDomainStatus.STATUS_APPROVED,getRes.state().status());

    Flux<LoanProcViewModel.ViewRecord> viewRecordFlux =
            componentClient.forView().call(LoanProcByStatusView::getLoanProcByStatus).params(new LoanProcViewModel.ViewRequest(LoanProcDomain.LoanProcDomainStatus.STATUS_APPROVED.name())).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);

    List<LoanProcViewModel.ViewRecord> viewResList = viewRecordFlux.collectList().block(timeout);

    assertTrue(!viewResList.stream().filter(vr -> vr.loanAppId().equals(loanAppId)).findFirst().isPresent());
  }
  @Test
  public void endToEndHappyPath() throws Exception {
    var loanAppId = UUID.randomUUID().toString();
    var reviewerId = "99999";
    var submitRequest = new LoanAppApi.SubmitRequest(
            "clientId",
            5000,
            2000,
            36);
    logger.info("Sending submit...");
    LoanAppApi.EmptyResponse appEmptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::submit).params(submitRequest).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertEquals(LoanAppApi.EmptyResponse.of(),appEmptyRes);

    Thread.sleep(2000);

    Flux<LoanProcViewModel.ViewRecord> viewRecordFlux =
            componentClient.forView().call(LoanProcByStatusView::getLoanProcByStatus).params(new LoanProcViewModel.ViewRequest(LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name())).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    List<LoanProcViewModel.ViewRecord> viewResList = viewRecordFlux.collectList().block(timeout);
    assertTrue(!viewResList.stream().filter(vr -> vr.loanAppId().equals(loanAppId)).findFirst().isPresent());

    LoanProcApi.EmptyResponse procEmptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::approve).params(new LoanProcApi.ApproveRequest(reviewerId)).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanProcApi.EmptyResponse.of(),procEmptyRes);

    Thread.sleep(2000);

    viewRecordFlux = componentClient.forView().call(LoanProcByStatusView::getLoanProcByStatus).params(new LoanProcViewModel.ViewRequest(LoanProcDomain.LoanProcDomainStatus.STATUS_APPROVED.name())).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    viewResList = viewRecordFlux.collectList().block(timeout);
    assertTrue(!viewResList.stream().filter(vr -> vr.loanAppId().equals(loanAppId)).findFirst().isPresent());

    logger.info("Sending get...");
    LoanAppApi.GetResponse getRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanAppDomain.LoanAppDomainStatus.STATUS_APPROVED,getRes.state().status());
  }
  @Test
  public void endToEndHappyPathWithDecline() throws Exception {
    var loanAppId = UUID.randomUUID().toString();
    var reviewerId = "99999";
    var declineReason = "some reason";
    var submitRequest = new LoanAppApi.SubmitRequest(
            "clientId",
            5000,
            2000,
            36);
    logger.info("Sending submit...");
    LoanAppApi.EmptyResponse appEmptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::submit).params(submitRequest).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertEquals(LoanAppApi.EmptyResponse.of(),appEmptyRes);

    Thread.sleep(2000);

    Flux<LoanProcViewModel.ViewRecord> viewRecordFlux =
            componentClient.forView().call(LoanProcByStatusView::getLoanProcByStatus).params(new LoanProcViewModel.ViewRequest(LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name())).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    List<LoanProcViewModel.ViewRecord> viewResList = viewRecordFlux.collectList().block(timeout);
    assertTrue(!viewResList.stream().filter(vr -> vr.loanAppId().equals(loanAppId)).findFirst().isPresent());

    LoanProcApi.EmptyResponse procEmptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanProcEntity::decline).params(new LoanProcApi.DeclineRequest(reviewerId,declineReason)).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanProcApi.EmptyResponse.of(),procEmptyRes);

    Thread.sleep(2000);

    viewRecordFlux = componentClient.forView().call(LoanProcByStatusView::getLoanProcByStatus).params(new LoanProcViewModel.ViewRequest(LoanProcDomain.LoanProcDomainStatus.STATUS_DECLINED.name())).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    viewResList = viewRecordFlux.collectList().block(timeout);
    assertTrue(!viewResList.stream().filter(vr -> vr.loanAppId().equals(loanAppId)).findFirst().isPresent());

    logger.info("Sending get...");
    LoanAppApi.GetResponse getRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanAppDomain.LoanAppDomainStatus.STATUS_DECLINED,getRes.state().status());
  }
  @Test
  public void endToEndHappyPathWithDeclineByTimeout() throws Exception {
    LoanProcTimeoutAction.timeout = 10000;
    var loanAppId = UUID.randomUUID().toString();
    var reviewerId = "99999";
    var submitRequest = new LoanAppApi.SubmitRequest(
            "clientId",
            5000,
            2000,
            36);
    logger.info("Sending submit...");
    LoanAppApi.EmptyResponse appEmptyRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::submit).params(submitRequest).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertEquals(LoanAppApi.EmptyResponse.of(),appEmptyRes);

    Thread.sleep(2000);

    Flux<LoanProcViewModel.ViewRecord> viewRecordFlux =
            componentClient.forView().call(LoanProcByStatusView::getLoanProcByStatus).params(new LoanProcViewModel.ViewRequest(LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name())).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    List<LoanProcViewModel.ViewRecord> viewResList = viewRecordFlux.collectList().block(timeout);
    assertTrue(!viewResList.stream().filter(vr -> vr.loanAppId().equals(loanAppId)).findFirst().isPresent());

    Thread.sleep(2000);

    viewRecordFlux = componentClient.forView().call(LoanProcByStatusView::getLoanProcByStatus).params(new LoanProcViewModel.ViewRequest(LoanProcDomain.LoanProcDomainStatus.STATUS_DECLINED.name())).execute().toCompletableFuture().get(3, TimeUnit.SECONDS);
    viewResList = viewRecordFlux.collectList().block(timeout);
    assertTrue(!viewResList.stream().filter(vr -> vr.loanAppId().equals(loanAppId)).findFirst().isPresent());

    logger.info("Sending get...");
    LoanAppApi.GetResponse getRes = componentClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::get).execute().toCompletableFuture().get(3,TimeUnit.SECONDS);
    assertEquals(LoanAppDomain.LoanAppDomainStatus.STATUS_DECLINED,getRes.state().status());
  }

}