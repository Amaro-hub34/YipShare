package com.startupidea.yipshare.loan;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class LoanAccessRevoked extends AppException {

    public LoanAccessRevoked() {
        super("Condominium admin took away this user's loan access", R.string.error_sin_acceso_prestamos);
    }
}
