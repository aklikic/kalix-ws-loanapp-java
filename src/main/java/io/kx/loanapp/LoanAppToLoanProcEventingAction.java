package io.kx.loanapp;

import io.kx.loanproc.LoanProcEntity;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;

import java.util.concurrent.CompletionStage;

@Subscribe.EventSourcedEntity(value = LoanAppEntity.class, ignoreUnknown = true)
public class LoanAppToLoanProcEventingAction extends Action {

    private final ComponentClient kalixClient;

    public LoanAppToLoanProcEventingAction(ComponentClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    public Effect<LoanAppApi.EmptyResponse> onSubmitted(LoanAppDomain.LoanAppDomainEvent.Submitted event){
        CompletionStage<LoanAppApi.EmptyResponse> processRes =
        kalixClient.forEventSourcedEntity(event.loanAppId()).call(LoanProcEntity::process).execute()
                        .thenApply(res -> LoanAppApi.EmptyResponse.of())
                        .exceptionally(e -> LoanAppApi.EmptyResponse.of());
        return effects().asyncReply(processRes);
    }
}
