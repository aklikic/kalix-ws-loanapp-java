package io.kx.loanproc;

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
@TypeId("loanproc")
@RequestMapping("/loanproc/{loanAppId}")
public class LoanProcEntity extends EventSourcedEntity<LoanProcDomain.LoanProcDomainState, LoanProcDomain.LoanProcDomainEvent> {
    private final String loanAppId;

    public LoanProcEntity(EventSourcedEntityContext context) {
        this.loanAppId = context.entityId();
    }

    @Override
    public LoanProcDomain.LoanProcDomainState emptyState() {
        return LoanProcDomain.LoanProcDomainState.empty(loanAppId);
    }

    @PostMapping("/process")
    public Effect<LoanProcApi.EmptyResponse> process(){
        switch (currentState().status()){
            case STATUS_UNKNOWN:
                LoanProcDomain.LoanProcDomainEvent.ReadyForReview event =
                        new LoanProcDomain.LoanProcDomainEvent.ReadyForReview(
                                loanAppId,
                                Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanProcApi.EmptyResponse.of());

            case STATUS_READY_FOR_REVIEW:
                return effects().reply(LoanProcApi.EmptyResponse.of());
            case STATUS_APPROVED:
            case STATUS_DECLINED:
            default:
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
        }
    }

    @PostMapping("/approve")
    public Effect<LoanProcApi.EmptyResponse> approve(@RequestBody LoanProcApi.ApproveRequest request){
        switch (currentState().status()){
            case STATUS_UNKNOWN:
                return effects().error("Not found", Status.Code.NOT_FOUND);
            case STATUS_READY_FOR_REVIEW:
                LoanProcDomain.LoanProcDomainEvent.Approved event = new LoanProcDomain.LoanProcDomainEvent.Approved(loanAppId, request.reviewerId(), Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanProcApi.EmptyResponse.of());
            case STATUS_APPROVED:
                return effects().reply(LoanProcApi.EmptyResponse.of());
            case STATUS_DECLINED:
            default:
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
        }


    }

    @PostMapping("/decline")
    public Effect<LoanProcApi.EmptyResponse> decline(@RequestBody LoanProcApi.DeclineRequest request){
        switch (currentState().status()){
            case STATUS_UNKNOWN:
                return effects().error("Not found", Status.Code.NOT_FOUND);
            case STATUS_READY_FOR_REVIEW:
                LoanProcDomain.LoanProcDomainEvent.Declined event = new LoanProcDomain.LoanProcDomainEvent.Declined(loanAppId, request.reviewerId(), request.reason(),Instant.now());
                return effects().emitEvent(event).thenReply(newState -> LoanProcApi.EmptyResponse.of());
            case STATUS_DECLINED:
                return effects().reply(LoanProcApi.EmptyResponse.of());
            case STATUS_APPROVED:
            default:
                return effects().error("Wrong status", Status.Code.INVALID_ARGUMENT);
        }
    }

    @GetMapping
    public Effect<LoanProcApi.GetResponse> get(){
        return effects().reply(new LoanProcApi.GetResponse(currentState()));
    }

    @EventHandler
    public LoanProcDomain.LoanProcDomainState onReadyForReview(LoanProcDomain.LoanProcDomainEvent.ReadyForReview event){
        return currentState().onReadyForReview(event);
    }
    @EventHandler
    public LoanProcDomain.LoanProcDomainState onApproved(LoanProcDomain.LoanProcDomainEvent.Approved event){
        return currentState().onApproved(event);
    }
    @EventHandler
    public LoanProcDomain.LoanProcDomainState onDeclined(LoanProcDomain.LoanProcDomainEvent.Declined event){
        return currentState().onDeclined(event);
    }
}
