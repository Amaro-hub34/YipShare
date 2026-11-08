package com.startupidea.yipshare.loan;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class LoanRequestNotPending extends AppException {

    public LoanRequestNotPending() {
        super("Loan request was already answered or is not addressed to this user", R.string.error_solicitud_respondida);
    }
}
