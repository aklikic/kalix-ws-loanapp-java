package io.kx.loanproc;

import kalix.javasdk.testkit.EventSourcedResult;
import kalix.javasdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoanProcEntityTest {
    @Test
    public void happyPath(){
        var loanAppId = UUID.randomUUID().toString();
        EventSourcedTestKit<LoanProcDomain.LoanProcDomainState, LoanProcDomain.LoanProcDomainEvent, LoanProcEntity> testKit = EventSourcedTestKit.of(loanAppId,LoanProcEntity::new);

        var reviewerId = UUID.randomUUID().toString();
        EventSourcedResult<LoanProcApi.EmptyResponse> processResult = testKit.call(service -> service.process());
        LoanProcDomain.LoanProcDomainEvent.ReadyForReview readyForReviewEvent = processResult.getNextEventOfType(LoanProcDomain.LoanProcDomainEvent.ReadyForReview.class);
        assertEquals(loanAppId,readyForReviewEvent.loanAppId());
        LoanProcDomain.LoanProcDomainState updatedStat = (LoanProcDomain.LoanProcDomainState) processResult.getUpdatedState();
        assertEquals(LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW,updatedStat.status());

        EventSourcedResult<LoanProcApi.EmptyResponse> approveResponse = testKit.call(service -> service.approve(new LoanProcApi.ApproveRequest(reviewerId)));
        LoanProcDomain.LoanProcDomainEvent.Approved approvedEvent = approveResponse.getNextEventOfType(LoanProcDomain.LoanProcDomainEvent.Approved.class);
        assertEquals(loanAppId,approvedEvent.loanAppId());

        updatedStat = (LoanProcDomain.LoanProcDomainState) approveResponse.getUpdatedState();
        assertEquals(LoanProcDomain.LoanProcDomainStatus.STATUS_APPROVED,updatedStat.status());
    }
}
