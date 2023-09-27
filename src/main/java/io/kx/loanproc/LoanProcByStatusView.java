package io.kx.loanproc;


import kalix.javasdk.annotations.Query;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.annotations.Table;
import kalix.javasdk.view.View;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

import java.util.List;

@Table("loanproc_by_status")
public class LoanProcByStatusView extends View<LoanProcViewModel.ViewRecord> {
    @PostMapping("/loanproc/views/by-status")
    @Query("SELECT * as list FROM loanproc_by_status WHERE statusId = :statusId")
    public LoanProcViewModel.ViewRecordList getLoanProcByStatus(@RequestBody LoanProcViewModel.ViewRequest request){
        return null;
    }

    @Subscribe.EventSourcedEntity(LoanProcEntity.class)
    public UpdateEffect<LoanProcViewModel.ViewRecord> onEvent(LoanProcDomain.LoanProcDomainEvent.ReadyForReview event){
        return effects().updateState(new LoanProcViewModel.ViewRecord(LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW.name(),event.loanAppId(), event.timestamp().toEpochMilli()));
    }
    @Subscribe.EventSourcedEntity(LoanProcEntity.class)
    public UpdateEffect<LoanProcViewModel.ViewRecord> onEvent(LoanProcDomain.LoanProcDomainEvent.Approved event){
        return effects().updateState(new LoanProcViewModel.ViewRecord(LoanProcDomain.LoanProcDomainStatus.STATUS_APPROVED.name(),event.loanAppId(), event.timestamp().toEpochMilli()));
    }
    @Subscribe.EventSourcedEntity(LoanProcEntity.class)
    public UpdateEffect<LoanProcViewModel.ViewRecord> onEvent(LoanProcDomain.LoanProcDomainEvent.Declined event){
        return effects().updateState(new LoanProcViewModel.ViewRecord(LoanProcDomain.LoanProcDomainStatus.STATUS_DECLINED.name(), event.loanAppId(), event.timestamp().toEpochMilli()));
    }
}
