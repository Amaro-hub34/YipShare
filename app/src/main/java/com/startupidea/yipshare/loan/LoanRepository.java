package com.startupidea.yipshare.loan;

import java.util.List;

public interface LoanRepository {

    List<Loan> getAllLoansByUserId(String userId, String condominiumId);

    List<LoanRequest> getAllLoanRequestsByLenderId(String lenderId, String condominiumId);

    boolean existsLoanByUserId(String userId);

    Loan getLoanById(String loanId, String userId);

    void modifyLoanStatus(String loanId, LoanStatus status);

    boolean existsPendingRequest(String userId, String articleId);

    void createLoanRequest(String lenderId, String borrowerId, String articleId, String requestText, int days);

    void createDemoLoans(String userId, String administeredId, String livedId);
}
