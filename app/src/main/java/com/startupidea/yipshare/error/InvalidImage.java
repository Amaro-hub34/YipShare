package com.startupidea.yipshare.error;

import com.startupidea.yipshare.R;

public class InvalidImage extends AppException {

    public InvalidImage() {
        super("Image could not be read", R.string.error_imagen_invalida);
    }
}
