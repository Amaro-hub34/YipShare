package com.startupidea.yipshare.loan;

import com.startupidea.yipshare.condominium.CondominiumMembership;
import com.startupidea.yipshare.condominium.CondominiumRepository;
import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.item.ItemAvailability;
import com.startupidea.yipshare.item.ItemNotAvailable;
import com.startupidea.yipshare.item.ItemNotFound;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LoanService {

    private static final Pattern CODE = Pattern.compile("\\d{6}");

    private final LoanRepository loanRepository;
    private final CondominiumRepository condominiumRepository;

    public LoanService(LoanRepository loanRepository, CondominiumRepository condominiumRepository) {
        this.loanRepository = loanRepository;
        this.condominiumRepository = condominiumRepository;
    }

    /** Loans of the selected condominium only; the other condominiums keep theirs. */
    public List<Loan> getAllLoansByUserId(String userId, String condominiumId) {
        ensureDemoLoans(userId);
        if (condominiumId == null) return new ArrayList<>();
        return loanRepository.getAllLoansByUserId(userId, condominiumId);
    }

    public List<LoanRequest> getAllLoanRequests(String userId, String condominiumId) {
        ensureDemoLoans(userId);
        if (condominiumId == null) return new ArrayList<>();
        return loanRepository.getAllLoanRequestsByLenderId(userId, condominiumId);
    }

    /** Sends the owner a request; it shows up in their notifications. */
    public void requestLoan(String userId, Item item, String requestText, int days) {
        if (item.isOwnedBy(userId)) throw new ItemNotFound();
        if (item.getAvailability() != ItemAvailability.AVAILABLE) throw new ItemNotAvailable();
        if (!condominiumRepository.canRequestLoans(userId, item.getCondominiumId())) throw new LoanAccessRevoked();
        if (loanRepository.existsPendingRequest(userId, item.getId())) throw new AlreadyRequested();

        loanRepository.createLoanRequest(item.getOwnerId(), userId, item.getId(), requestText, days);
    }

    public void acceptLoanRequest(String userId, String loanId) {
        answerLoanRequest(userId, loanId, LoanStatus.ACCEPTED);
    }

    public void rejectLoanRequest(String userId, String loanId) {
        answerLoanRequest(userId, loanId, LoanStatus.REJECTED);
    }

    // shortcut: any 6 digits are accepted, check it against the generated code once the API exists.
    public Loan confirmCode(String userId, Loan loan, String code) {
        if (!CODE.matcher(code).matches()) throw new InvalidCode();
        if (!loan.getStatus().acceptsCode()) throw new LoanNotActive();

        loanRepository.modifyLoanStatus(loan.getId(), loan.getStatus().next());
        return loanRepository.getLoanById(loan.getId(), userId);
    }

    // Only the lender answers, and only once.
    private void answerLoanRequest(String userId, String loanId, LoanStatus answer) {
        Loan loan = loanRepository.getLoanById(loanId, userId);
        if (loan == null || !loan.isLentByMe() || loan.getStatus() != LoanStatus.PENDING) throw new LoanRequestNotPending();
        loanRepository.modifyLoanStatus(loanId, answer);
    }

    // shortcut: loans can't be requested locally yet (articles need an approved condominium),
    // so every user gets demo loans and requests from demo neighbors; remove once loans are requested for real.
    private void ensureDemoLoans(String userId) {
        if (loanRepository.existsLoanByUserId(userId)) return;
        if (!condominiumRepository.existsMembershipByUserId(userId)) condominiumRepository.createDemoCondominiums(userId);

        String administered = null;
        String lived = null;
        for (CondominiumMembership membership : condominiumRepository.getAllMembershipsByUserId(userId)) {
            if (membership.isAdmin() && administered == null) administered = membership.getCondominiumId();
            if (!membership.isAdmin() && lived == null) lived = membership.getCondominiumId();
        }
        if (administered == null) administered = lived;
        if (lived == null) lived = administered;
        if (administered != null) loanRepository.createDemoLoans(userId, administered, lived);
    }
}
