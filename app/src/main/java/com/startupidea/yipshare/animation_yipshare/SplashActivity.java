package com.startupidea.yipshare.animation_yipshare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.appcompat.app.AppCompatActivity;

import com.startupidea.yipshare.PantallaPrincipalActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.SqliteUserRepository;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.TaskUtil;
import com.startupidea.yipshare.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Splash);
        super.onCreate(savedInstanceState);

        long start = SystemClock.uptimeMillis();
        TaskUtil.run(this, this::hasActiveSession, active -> {
            long remaining = Math.max(0, SPLASH_DURATION - (SystemClock.uptimeMillis() - start));
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(this, active ? PantallaPrincipalActivity.class : MainActivity.class));
                finish();
            }, remaining);
        }, error -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private boolean hasActiveSession() {
        SessionRepository session = new SessionRepository(this);
        boolean active;
        try {
            active = new UserService(new SqliteUserRepository(this)).isActiveSession(session.getUserId());
        } catch (AppException e) {
            active = false;
        }
        if (!active) session.deleteSession();
        return active;
    }
}
