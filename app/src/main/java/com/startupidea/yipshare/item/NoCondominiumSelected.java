package com.startupidea.yipshare.item;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class NoCondominiumSelected extends AppException {

    public NoCondominiumSelected() {
        super("User has no condominium to publish in", R.string.error_sin_condominio);
    }
}
