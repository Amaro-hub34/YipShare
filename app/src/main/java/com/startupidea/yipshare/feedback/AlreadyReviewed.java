package com.startupidea.yipshare.feedback;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class AlreadyReviewed extends AppException {

    public AlreadyReviewed() {
        super("User already reviewed this loan", R.string.error_ya_calificaste);
    }
}
