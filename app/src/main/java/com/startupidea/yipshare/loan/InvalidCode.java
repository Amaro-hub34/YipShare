package com.startupidea.yipshare.loan;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class InvalidCode extends AppException {

    public InvalidCode() {
        super("Loan code must have 6 digits", R.string.error_codigo_longitud);
    }
}
