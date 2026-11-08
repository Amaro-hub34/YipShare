package com.startupidea.yipshare.item;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class ItemNotAvailable extends AppException {

    public ItemNotAvailable() {
        super("Only an available item can be modified or deleted", R.string.error_articulo_no_disponible);
    }
}
