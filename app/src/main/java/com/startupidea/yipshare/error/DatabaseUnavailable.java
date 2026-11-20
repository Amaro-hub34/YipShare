package com.startupidea.yipshare.error;

import com.startupidea.yipshare.R;

public class DatabaseUnavailable extends AppException {

    public DatabaseUnavailable() {
        super("Database is unavailable", R.string.error_bd_no_disponible);
    }
}
