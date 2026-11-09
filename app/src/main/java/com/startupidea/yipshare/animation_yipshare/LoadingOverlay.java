package com.startupidea.yipshare.animation_yipshare;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.startupidea.yipshare.R;

/**
 * Loading overlay with the YipShare house spinning around its vertical axis.
 * Usage: LoadingOverlay loading = new LoadingOverlay(this); loading.show(); ... loading.dismiss();
 */
public class LoadingOverlay {

    private static final long TURN_DURATION_MS = 1400;

    private final Dialog dialog;
    private final ImageView house;
    private final TextView message;
    private ObjectAnimator spin;

    public LoadingOverlay(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_loading, null);
        house = view.findViewById(R.id.loadingHouse);
        message = view.findViewById(R.id.loadingMessage);


        house.setCameraDistance(8000 * context.getResources().getDisplayMetrics().density);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.5f);
        }
        dialog.setOnDismissListener(d -> stopSpin());
    }

    public void show() {
        if (dialog.isShowing()) return;
        dialog.show();
        startSpin();
    }

    public void show(int messageRes) {
        message.setText(messageRes);
        show();
    }

    public void dismiss() {
        if (dialog.isShowing()) dialog.dismiss();
    }

    private void startSpin() {
        stopSpin();
        spin = ObjectAnimator.ofFloat(house, View.ROTATION_Y, 0f, 360f);
        spin.setDuration(TURN_DURATION_MS);
        spin.setInterpolator(new LinearInterpolator());
        spin.setRepeatCount(ObjectAnimator.INFINITE);
        spin.start();
    }

    private void stopSpin() {
        if (spin != null) {
            spin.cancel();
            spin = null;
        }
        house.setRotationY(0f);
    }
}
