package com.startupidea.yipshare.utils;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.Looper;

import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.error.DatabaseUnavailable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TaskUtil {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private TaskUtil() {
    }

    public static <T> void run(Activity activity, Supplier<T> work, Consumer<T> onResult, Consumer<AppException> onError) {
        EXECUTOR.execute(() -> {
            try {
                T result = work.get();
                MAIN.post(() -> {
                    if (!activity.isDestroyed()) onResult.accept(result);
                });
            } catch (AppException e) {
                MAIN.post(() -> {
                    if (!activity.isDestroyed()) onError.accept(e);
                });
            } catch (SQLiteException e) {
                MAIN.post(() -> {
                    if (!activity.isDestroyed()) onError.accept(new DatabaseUnavailable());
                });
            }
        });
    }

    public static void runInBackground(Runnable work) {
        EXECUTOR.execute(work);
    }

    public static void run(Activity activity, Runnable work, Runnable onDone, Consumer<AppException> onError) {
        run(activity, () -> {
            work.run();
            return null;
        }, ignored -> onDone.run(), onError);
    }
}
