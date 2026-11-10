package com.startupidea.yipshare.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.startupidea.yipshare.R;

public final class ConfirmDialogUtil {

    private ConfirmDialogUtil() {
    }

    public static void show(Context context, CharSequence message, Runnable onConfirm) {
        show(context, message, onConfirm, null);
    }

    public static void show(Context context, CharSequence message, Runnable onConfirm, @Nullable Runnable onCancel) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirmar, null);
        ((TextView) view.findViewById(R.id.tv_mensaje_confirmar)).setText(message);

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        if (onCancel != null) dialog.setOnCancelListener(d -> onCancel.run());

        view.findViewById(R.id.btn_cancelar_confirmar).setOnClickListener(v -> dialog.cancel());
        view.findViewById(R.id.btn_aceptar_confirmar).setOnClickListener(v -> {
            dialog.dismiss();
            onConfirm.run();
        });

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
    }
}
