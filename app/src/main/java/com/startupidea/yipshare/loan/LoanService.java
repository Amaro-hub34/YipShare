package com.startupidea.yipshare.loan;

import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.item.ItemAvailability;
import com.startupidea.yipshare.item.ItemNotAvailable;
import com.startupidea.yipshare.item.ItemNotFound;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class LoanService {

    private static final Pattern CODE = Pattern.compile("[A-Z0-9]{6}");

    private final LoanRepository loanRepository;

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public List<Loan> getAllLoansByUserId(String userId, String condominiumId) {
        List<Loan> loans = new ArrayList<>();
        if (condominiumId == null) return loans;
        for (Loan loan : loanRepository.getAllLoansByCondominiumId(condominiumId)) {
            if (loan.getStatus() != LoanStatus.PENDING && loan.getStatus() != LoanStatus.REJECTED) loans.add(loan);
        }
        return loans;
    }

    public List<LoanRequest> getAllLoanRequests(String userId, String condominiumId) {
        if (condominiumId == null) return new ArrayList<>();
        return loanRepository.getAllLoanRequestsByCondominiumId(condominiumId);
    }

    public void requestLoan(String userId, Item item, String requestText, int days) {
        if (item.isOwnedBy(userId)) throw new ItemNotFound();
        if (item.getAvailability() != ItemAvailability.AVAILABLE) throw new ItemNotAvailable();
        loanRepository.createLoanRequest(item.getId(), requestText, days);
    }

    public void acceptLoanRequest(String userId, String loanId) {
        loanRepository.modifyLoanStatus(loanId, LoanStatus.ACCEPTED);
    }

    public void rejectLoanRequest(String userId, String loanId) {
        loanRepository.modifyLoanStatus(loanId, LoanStatus.REJECTED);
    }

    public String createCode(Loan loan) {
        if (!loan.isLentByMe() || !loan.getStatus().acceptsCode()) throw new LoanNotActive();
        return loanRepository.createCode(loan.getId());
    }

    public Loan confirmCode(String userId, Loan loan, String code) {
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if (!CODE.matcher(normalized).matches()) throw new InvalidCode();
        if (loan.isLentByMe() || !loan.getStatus().acceptsCode()) throw new LoanNotActive();
        return loanRepository.confirmCode(loan.getId(), normalized);
    }
}
