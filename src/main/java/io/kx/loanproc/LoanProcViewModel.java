package io.kx.loanproc;

import java.util.List;

public interface LoanProcViewModel {
    record ViewRecordList(List<ViewRecord> list) implements LoanProcViewModel{}
    record ViewRecord(String statusId, String loanAppId, long lastUpdated) implements LoanProcViewModel{}
    record ViewRequest(String statusId) implements LoanProcViewModel{}
}
