package com.startupidea.yipshare.user;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;

public class ProfilePictureTooLarge extends AppException {

    public ProfilePictureTooLarge() {
        super("Profile picture is larger than 10 MB", R.string.error_foto_perfil_grande);
    }
}
