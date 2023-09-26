package io.kx.loanproc;

import kalix.javasdk.annotations.TypeName;

import java.time.Instant;

public interface LoanProcDomain {
    enum LoanProcDomainStatus {
        STATUS_UNKNOWN,
        STATUS_READY_FOR_REVIEW,
        STATUS_APPROVED,
        STATUS_DECLINED;
    }

    record LoanProcDomainState(String loanAppId,
                                      String reviewerId,
                                      LoanProcDomainStatus status,
                                      String declineReason,
                                      Instant lastUpdatedTimestamp) {

        public static LoanProcDomainState empty(String loanAppId){
            return new LoanProcDomainState(loanAppId,null, LoanProcDomainStatus.STATUS_UNKNOWN,null, null);
        }
        public LoanProcDomainState onReadyForReview(LoanProcDomainEvent.ReadyForReview event){
            return new LoanProcDomainState(this.loanAppId, null, LoanProcDomainStatus.STATUS_READY_FOR_REVIEW,null,event.timestamp());
        }
        public LoanProcDomainState onApproved(LoanProcDomainEvent.Approved event){
            return new LoanProcDomainState(this.loanAppId, event.reviewerId(), LoanProcDomainStatus.STATUS_APPROVED,null,event.timestamp());
        }
        public LoanProcDomainState onDeclined(LoanProcDomainEvent.Declined event){
            return new LoanProcDomainState(this.loanAppId, event.reviewerId(), LoanProcDomainStatus.STATUS_DECLINED,event.reason(),event.timestamp());
        }
    }
    interface LoanProcDomainEvent {
        @TypeName("proc_ready_for_review")
        record ReadyForReview(String loanAppId,
                              Instant timestamp) implements LoanProcDomainEvent {}
        @TypeName("proc_approved")
        record Approved(String loanAppId, String reviewerId, Instant timestamp) implements LoanProcDomainEvent {}
        @TypeName("proc_declined")
        record Declined(String loanAppId, String reviewerId, String reason, Instant timestamp) implements LoanProcDomainEvent {}
    }
}
