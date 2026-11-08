package com.startupidea.yipshare.feedback;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class EmptyRating extends AppException {

    public EmptyRating() {
        super("Rating is empty", R.string.error_calificacion_vacia);
    }
}
