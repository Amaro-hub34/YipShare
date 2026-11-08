package com.startupidea.yipshare.loan;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class LoanNotActive extends AppException {

    public LoanNotActive() {
        super("Loan does not take codes in its current state", R.string.error_prestamo_no_activo);
    }
}
