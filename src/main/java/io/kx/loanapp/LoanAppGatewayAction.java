package io.kx.loanapp;

import kalix.javasdk.action.Action;
import kalix.javasdk.client.ComponentClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@RequestMapping("/loanapp-gw")
public class LoanAppGatewayAction extends Action {

    private final ComponentClient kalixClient;
    public LoanAppGatewayAction(ComponentClient kalixClient) {
        this.kalixClient = kalixClient;
    }

    @PostMapping("/submit")
    public Effect<LoanAppApi.GetResponse> submit(@RequestBody LoanAppApi.SubmitRequest request){
        var loanAppId = UUID.randomUUID().toString();
        CompletionStage<LoanAppApi.EmptyResponse> submit = kalixClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::submit).params(request).execute();
        CompletionStage<LoanAppApi.GetResponse> res = submit.thenCompose(r ->
                                                        kalixClient.forEventSourcedEntity(loanAppId).call(LoanAppEntity::get).execute()
                                                   );
        return effects().asyncReply(res);
    }
}
