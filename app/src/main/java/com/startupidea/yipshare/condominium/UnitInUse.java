package com.startupidea.yipshare.condominium;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class UnitInUse extends AppException {

    public UnitInUse() {
        super("Unit has members assigned", R.string.error_unidad_ocupada);
    }
}
