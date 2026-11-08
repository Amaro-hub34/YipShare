package com.startupidea.yipshare.loan;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class AlreadyRequested extends AppException {

    public AlreadyRequested() {
        super("User already has a pending request for this item", R.string.error_ya_solicitado);
    }
}
