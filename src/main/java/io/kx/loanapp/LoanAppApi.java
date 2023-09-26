package io.kx.loanapp;

public interface LoanAppApi {
    record SubmitRequest(String clientId,
                         Integer clientMonthlyIncomeCents,
                         Integer loanAmountCents,
                         Integer loanDurationMonths) implements LoanAppApi{}
    record DeclineRequest(String reason) implements LoanAppApi{}

    record EmptyResponse()implements LoanAppApi{
        public static EmptyResponse of(){
            return new EmptyResponse();
        }
    }

    record GetResponse(LoanAppDomain.LoanAppDomainState state) implements LoanAppApi{}
}
