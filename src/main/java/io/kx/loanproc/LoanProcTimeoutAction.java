package io.kx.loanproc;

import io.kx.loanapp.LoanAppApi;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;

import java.time.Duration;

@Subscribe.EventSourcedEntity(value = LoanProcEntity.class, ignoreUnknown = true)
public class LoanProcTimeoutAction extends Action {

    public static long timeout = 600000l;
    private final ComponentClient kalixClient;
    //private final LoanProcConfig config;

    public LoanProcTimeoutAction(ComponentClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    private String getTimerName(String loanAppId){
        return "timeout-"+loanAppId;
    }

    public Effect<LoanAppApi.EmptyResponse> onReadyForReview(LoanProcDomain.LoanProcDomainEvent.ReadyForReview event){
        var deferredCall = kalixClient.forEventSourcedEntity(event.loanAppId()).call(LoanProcEntity::decline).params(new LoanProcApi.DeclineRequest("SYSTEM", "timeout by timer"));
        return effects().asyncReply(timers().startSingleTimer(getTimerName(event.loanAppId()), Duration.ofMillis(timeout),deferredCall)
                .thenApply(d -> LoanAppApi.EmptyResponse.of()));
    }

    public Effect<LoanAppApi.EmptyResponse> onApproved(LoanProcDomain.LoanProcDomainEvent.Approved event){
        return effects().asyncReply(timers().cancel(getTimerName(event.loanAppId())).thenApply(d -> LoanAppApi.EmptyResponse.of()));
    }
    public Effect<LoanAppApi.EmptyResponse> onDeclined(LoanProcDomain.LoanProcDomainEvent.Declined event){
        return effects().asyncReply(timers().cancel(getTimerName(event.loanAppId())).thenApply(d -> LoanAppApi.EmptyResponse.of()));
    }

}
