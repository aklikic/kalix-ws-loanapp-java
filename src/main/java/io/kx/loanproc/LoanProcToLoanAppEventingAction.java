package io.kx.loanproc;

import io.kx.loanapp.LoanAppApi;
import io.kx.loanapp.LoanAppEntity;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;

import java.util.concurrent.CompletionStage;

@Subscribe.EventSourcedEntity(value = LoanProcEntity.class, ignoreUnknown = true)
public class LoanProcToLoanAppEventingAction extends Action {

    private final ComponentClient kalixClient;

    public LoanProcToLoanAppEventingAction(ComponentClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    public Effect<LoanProcApi.EmptyResponse> onApproved(LoanProcDomain.LoanProcDomainEvent.Approved event){
        CompletionStage<LoanProcApi.EmptyResponse> processRes =
                kalixClient.forEventSourcedEntity(event.loanAppId()).call(LoanAppEntity::approve).execute()
                        .thenApply(res -> LoanProcApi.EmptyResponse.of())
                        .exceptionally(e -> LoanProcApi.EmptyResponse.of());

        return effects().asyncReply(processRes);
    }

    public Effect<LoanProcApi.EmptyResponse> onDeclined(LoanProcDomain.LoanProcDomainEvent.Declined event){
        CompletionStage<LoanProcApi.EmptyResponse> processRes =
                kalixClient.forEventSourcedEntity(event.loanAppId()).call(LoanAppEntity::decline).params(new LoanAppApi.DeclineRequest(event.reason())).execute()
                        .thenApply(res -> LoanProcApi.EmptyResponse.of())
                        .exceptionally(e -> LoanProcApi.EmptyResponse.of());

        return effects().asyncReply(processRes);
    }

}
