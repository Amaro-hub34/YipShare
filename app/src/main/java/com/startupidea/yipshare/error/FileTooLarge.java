package com.startupidea.yipshare.error;

import com.startupidea.yipshare.R;

public class FileTooLarge extends AppException {

    public FileTooLarge() {
        super("File is larger than the server allows", R.string.error_archivo_grande);
    }
}
