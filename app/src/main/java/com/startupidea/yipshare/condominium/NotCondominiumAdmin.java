package com.startupidea.yipshare.condominium;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class NotCondominiumAdmin extends AppException {

    public NotCondominiumAdmin() {
        super("Only the condominium admin can do this", R.string.error_no_admin);
    }
}
