package io.kx.loanapp;

import io.grpc.Status;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;


@Id("loanAppId")
@TypeId("loanapp")
@RequestMapping("/loanapp/{loanAppId}")
public class LoanAppEntity extends EventSourcedEntity<LoanAppDomain.LoanAppDomainState, LoanAppDomain.LoanAppDomainEvent> {
    private final String loanAppId;

    public LoanAppEntity(EventSourcedEntityContext context) {
        this.loanAppId = context.entityId();
    }

    @Override
    public LoanAppDomain.LoanAppDomainState emptyState() {
        return LoanAppDomain.LoanAppDomainState.empty(loanAppId);
    }

    @PostMapping("/submit")
    public Effect<LoanAppApi.EmptyResponse> submit(@RequestBody LoanAppApi.SubmitRequest request){
        switch (currentState().status()){
            case STATUS_UNKNOWN:
                LoanAppDomain.LoanAppDomainEvent.Submitted event =
                        new LoanAppDomain.LoanAppDomainEvent.Submitted(
                                loanAppId,
                                request.clientId(),
                                request.clientMonthlyIncomeCents(),
                                request.loanAmountCents(),
                                request.loanDurationMonths(),
                                Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanAppApi.EmptyResponse.of());

            case STATUS_IN_REVIEW:
                return effects().reply(LoanAppApi.EmptyResponse.of());
            case STATUS_APPROVED:
            case STATUS_DECLINED:
            default:
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
        }
    }

    @PostMapping("/approve")
    public Effect<LoanAppApi.EmptyResponse> approve(){
        switch (currentState().status()){
            case STATUS_UNKNOWN:
                return effects().error("Not found", Status.Code.NOT_FOUND);
            case STATUS_IN_REVIEW:
                LoanAppDomain.LoanAppDomainEvent.Approved event = new LoanAppDomain.LoanAppDomainEvent.Approved(loanAppId,Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanAppApi.EmptyResponse.of());
            case STATUS_APPROVED:
                return effects().reply(LoanAppApi.EmptyResponse.of());
            case STATUS_DECLINED:
            default:
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
        }


    }

    @PostMapping("/decline")
    public Effect<LoanAppApi.EmptyResponse> decline(@RequestBody LoanAppApi.DeclineRequest request){
        switch (currentState().status()){
            case STATUS_UNKNOWN:
                return effects().error("Not found", Status.Code.NOT_FOUND);
            case STATUS_IN_REVIEW:
                LoanAppDomain.LoanAppDomainEvent.Declined event = new LoanAppDomain.LoanAppDomainEvent.Declined(loanAppId,request.reason(),Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanAppApi.EmptyResponse.of());
            case STATUS_DECLINED:
                return effects().reply(LoanAppApi.EmptyResponse.of());
            case STATUS_APPROVED:
            default:
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
        }
    }

    @GetMapping
    public Effect<LoanAppApi.GetResponse> get(){
        return effects().reply(new LoanAppApi.GetResponse(currentState()));
    }

    @EventHandler
    public LoanAppDomain.LoanAppDomainState onSubmitted(LoanAppDomain.LoanAppDomainEvent.Submitted event){
        return currentState().onSubmitted(event);
    }
    @EventHandler
    public LoanAppDomain.LoanAppDomainState onApproved(LoanAppDomain.LoanAppDomainEvent.Approved event){
        return currentState().onApproved(event);
    }
    @EventHandler
    public LoanAppDomain.LoanAppDomainState onDeclined(LoanAppDomain.LoanAppDomainEvent.Declined event){
        return currentState().onDeclined(event);
    }
}
