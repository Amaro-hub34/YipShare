package com.startupidea.yipshare.feedback;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class EmptyReportText extends AppException {

    public EmptyReportText() {
        super("Report text is empty", R.string.error_campo_obligatorio);
    }
}
