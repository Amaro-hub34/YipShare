package com.startupidea.yipshare.utils;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;

import com.startupidea.yipshare.R;

public final class PasswordToggleUtil {

    private PasswordToggleUtil() {
    }

    public static void attach(EditText input, ImageView toggle) {
        toggle.setOnClickListener(v -> {
            boolean hidden = input.getTransformationMethod() instanceof PasswordTransformationMethod;
            int cursor = input.getSelectionEnd();

            input.setTransformationMethod(hidden
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            input.setSelection(cursor);

            toggle.setImageResource(hidden ? R.drawable.ic_visibility_off : R.drawable.ic_visibility);
            toggle.setContentDescription(toggle.getContext().getString(
                    hidden ? R.string.ocultar_contrasena : R.string.mostrar_contrasena));
        });
    }
}
