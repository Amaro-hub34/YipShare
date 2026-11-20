package com.startupidea.yipshare.loan;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.BadRequestException;
import com.startupidea.yipshare.error.ConflictException;
import com.startupidea.yipshare.error.ResourceNotFound;
import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.item.ItemAvailability;

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
            if (loan.status() != LoanStatus.PENDING && loan.status() != LoanStatus.REJECTED) loans.add(loan);
        }
        return loans;
    }

    public List<LoanRequest> getAllLoanRequests(String userId, String condominiumId) {
        if (condominiumId == null) return new ArrayList<>();
        return loanRepository.getAllLoanRequestsByCondominiumId(condominiumId);
    }

    public void requestLoan(String userId, Item item, String requestText, int days) {
        if (item.isOwnedBy(userId)) throw new ResourceNotFound("Item does not exist or belongs to another user", R.string.error_articulo_no_existe);
        if (item.availability() != ItemAvailability.AVAILABLE) throw new ConflictException("Only an available item can be modified or deleted", R.string.error_articulo_no_disponible);
        loanRepository.createLoanRequest(item.id(), requestText, days);
    }

    public void acceptLoanRequest(String userId, String loanId) {
        loanRepository.modifyLoanStatus(loanId, LoanStatus.ACCEPTED);
    }

    public void rejectLoanRequest(String userId, String loanId) {
        loanRepository.modifyLoanStatus(loanId, LoanStatus.REJECTED);
    }

    public String createCode(Loan loan) {
        if (!loan.lentByMe() || !loan.status().acceptsCode()) throw new ConflictException("Loan does not take codes in its current state", R.string.error_prestamo_no_activo);
        return loanRepository.createCode(loan.id());
    }

    public Loan confirmCode(String userId, Loan loan, String code) {
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if (!CODE.matcher(normalized).matches()) throw new BadRequestException("Loan code is wrong or must have 6 digits", R.string.error_codigo_longitud);
        if (loan.lentByMe() || !loan.status().acceptsCode()) throw new ConflictException("Loan does not take codes in its current state", R.string.error_prestamo_no_activo);
        return loanRepository.confirmCode(loan.id(), normalized);
    }
}
