package io.kx.loanproc;

public interface LoanProcApi {
    record ApproveRequest(String reviewerId) implements LoanProcApi {}
    record DeclineRequest(String reviewerId, String reason) implements LoanProcApi {}

    record EmptyResponse()implements LoanProcApi {
        public static EmptyResponse of(){
            return new EmptyResponse();
        }
    }

    record GetResponse(LoanProcDomain.LoanProcDomainState state) implements LoanProcApi {}
}
