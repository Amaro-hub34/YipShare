package com.startupidea.yipshare.animation_yipshare;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.utils.TaskUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LoadingOverlay {

    private static final long TURN_DURATION_MS = 1000;
    private static final long MIN_SHOW_MS = 800;

    private final Activity activity;
    private final Dialog dialog;
    private final ImageView spinner;
    private ObjectAnimator spin;

    public LoadingOverlay(Activity activity) {
        this.activity = activity;
        View view = LayoutInflater.from(activity).inflate(R.layout.view_loading, null);
        spinner = view.findViewById(R.id.loadingSpinner);

        dialog = new Dialog(activity);
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

    public <T> void run(Supplier<T> work, Consumer<T> onResult, Consumer<AppException> onError) {
        show();
        long start = SystemClock.uptimeMillis();
        TaskUtil.run(activity, work,
                result -> finish(start, () -> onResult.accept(result)),
                error -> finish(start, () -> onError.accept(error)));
    }

    public void run(Runnable work, Runnable onDone, Consumer<AppException> onError) {
        run(() -> {
            work.run();
            return null;
        }, ignored -> onDone.run(), onError);
    }

    private void finish(long start, Runnable then) {
        long remaining = Math.max(0, MIN_SHOW_MS - (SystemClock.uptimeMillis() - start));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dismiss();
            if (!activity.isDestroyed()) then.run();
        }, remaining);
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
