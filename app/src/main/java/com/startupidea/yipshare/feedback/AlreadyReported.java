package com.startupidea.yipshare.feedback;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class AlreadyReported extends AppException {

    public AlreadyReported() {
        super("User already reported this loan", R.string.error_ya_reportado);
    }
}
