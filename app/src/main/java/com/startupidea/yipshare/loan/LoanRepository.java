package com.startupidea.yipshare.loan;

import java.util.List;

public interface LoanRepository {

    List<Loan> getAllLoansByCondominiumId(String condominiumId);

    List<LoanRequest> getAllLoanRequestsByCondominiumId(String condominiumId);

    void createLoanRequest(String articleId, String requestText, int days);

    void modifyLoanStatus(String loanId, LoanStatus answer);

    String createCode(String loanId);

    Loan confirmCode(String loanId, String code);
}
