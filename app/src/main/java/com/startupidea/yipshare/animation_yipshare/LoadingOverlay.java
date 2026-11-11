package com.startupidea.yipshare.animation_yipshare;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.startupidea.yipshare.R;

public class LoadingOverlay {

    private static final long TURN_DURATION_MS = 1000;
    private static final long MIN_SHOW_MS = 800;

    private final Dialog dialog;
    private final ImageView spinner;
    private ObjectAnimator spin;

    public LoadingOverlay(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_loading, null);
        spinner = view.findViewById(R.id.loadingSpinner);

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.4f);
        }
        dialog.setOnDismissListener(d -> stopSpin());
    }

    public void show() {
        if (dialog.isShowing()) return;
        dialog.show();
        startSpin();
    }

    public void show(int messageRes) {
        spinner.setContentDescription(spinner.getContext().getString(messageRes));
        show();
    }

    public void run(Runnable action) {
        show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!dialog.isShowing()) return;
            dismiss();
            action.run();
        }, MIN_SHOW_MS);
    }

    public void dismiss() {
        if (dialog.isShowing()) dialog.dismiss();
    }

    private void startSpin() {
        stopSpin();
        spin = ObjectAnimator.ofFloat(spinner, View.ROTATION, 0f, 360f);
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
        spinner.setRotation(0f);
    }
}
