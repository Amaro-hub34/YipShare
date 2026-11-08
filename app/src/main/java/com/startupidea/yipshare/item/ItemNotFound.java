package com.startupidea.yipshare.item;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class ItemNotFound extends AppException {

    public ItemNotFound() {
        super("Item does not exist or belongs to another user", R.string.error_articulo_no_existe);
    }
}
