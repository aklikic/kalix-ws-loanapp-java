package io.kx.loanapp;

import kalix.javasdk.annotations.TypeName;

import java.time.Instant;

public interface LoanAppDomain {
    enum LoanAppDomainStatus {
        STATUS_UNKNOWN,
        STATUS_IN_REVIEW,
        STATUS_APPROVED,
        STATUS_DECLINED;
    }
    record LoanAppDomainState(String loanAppId,
                              String clientId,
                              Integer clientMonthlyIncomeCents,
                              Integer loanAmountCents,
                              Integer loanDurationMonths,
                              LoanAppDomainStatus status,
                              String declineReason,
                              Instant lastUpdatedTimestamp) {

        public static LoanAppDomainState empty(String loanAppId){
            return new LoanAppDomainState(loanAppId,null,null,null, null,LoanAppDomainStatus.STATUS_UNKNOWN,null,null);
        }
        public LoanAppDomainState onSubmitted(LoanAppDomainEvent.Submitted event){
            return new LoanAppDomainState(this.loanAppId, event.clientId(), event.clientMonthlyIncomeCents(), event.loanAmountCents(), event.loanDurationMonths(), LoanAppDomainStatus.STATUS_IN_REVIEW,null,event.timestamp());
        }
        public LoanAppDomainState onApproved(LoanAppDomainEvent.Approved event){
            return new LoanAppDomainState(this.loanAppId, this.clientId, this.clientMonthlyIncomeCents, this.loanAmountCents, this.loanDurationMonths, LoanAppDomainStatus.STATUS_APPROVED,null,event.timestamp());
        }
        public LoanAppDomainState onDeclined(LoanAppDomainEvent.Declined event){
            return new LoanAppDomainState(this.loanAppId, this.clientId, this.clientMonthlyIncomeCents, this.loanAmountCents, this.loanDurationMonths, LoanAppDomainStatus.STATUS_DECLINED,event.reason(),event.timestamp());
        }
    }
    interface LoanAppDomainEvent {
        @TypeName("submitted")
        record Submitted(String loanAppId,
                         String clientId,
                         Integer clientMonthlyIncomeCents,
                         Integer loanAmountCents,
                         Integer loanDurationMonths,
                         Instant timestamp) implements LoanAppDomainEvent {}

        @TypeName("approved")
        record Approved(String loanAppId, Instant timestamp) implements LoanAppDomainEvent {}
        @TypeName("declined")
        record Declined(String loanAppId, String reason, Instant timestamp) implements LoanAppDomainEvent {}
    }
}
