package com.startupidea.yipshare.condominium;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class EmptyApartment extends AppException {

    public EmptyApartment() {
        super("Apartment is empty", R.string.error_campo_obligatorio);
    }
}
