package com.startupidea.yipshare.condominium;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class AlreadyJoined extends AppException {

    public AlreadyJoined() {
        super("User already joined or asked to join this condominium", R.string.error_ya_unido);
    }
}
