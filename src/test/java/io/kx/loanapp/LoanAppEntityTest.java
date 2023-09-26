package io.kx.loanapp;

import kalix.javasdk.testkit.EventSourcedResult;
import kalix.javasdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoanAppEntityTest {

    @Test
    public void happyPath(){
        var loanAppId = UUID.randomUUID().toString();
        EventSourcedTestKit<LoanAppDomain.LoanAppDomainState, LoanAppDomain.LoanAppDomainEvent, LoanAppEntity> testKit = EventSourcedTestKit.of(loanAppId, LoanAppEntity::new);

        var submitRequest = new LoanAppApi.SubmitRequest(
                "clientId",
                5000,
                2000,
                36);
        EventSourcedResult<LoanAppApi.EmptyResponse> submitResult = testKit.call(service -> service.submit(submitRequest));
        LoanAppDomain.LoanAppDomainEvent.Submitted submittedEvent = submitResult.getNextEventOfType(LoanAppDomain.LoanAppDomainEvent.Submitted.class);
        assertEquals(loanAppId,submittedEvent.loanAppId());
        LoanAppDomain.LoanAppDomainState updatedStat = (LoanAppDomain.LoanAppDomainState)submitResult.getUpdatedState();
        assertEquals(LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW,updatedStat.status());

        EventSourcedResult<LoanAppApi.EmptyResponse> approveResponse = testKit.call(service -> service.approve());
        LoanAppDomain.LoanAppDomainEvent.Approved approvedEvent = approveResponse.getNextEventOfType(LoanAppDomain.LoanAppDomainEvent.Approved.class);
        assertEquals(loanAppId,approvedEvent.loanAppId());

        updatedStat = (LoanAppDomain.LoanAppDomainState)approveResponse.getUpdatedState();
        assertEquals(LoanAppDomain.LoanAppDomainStatus.STATUS_APPROVED,updatedStat.status());
    }
}
