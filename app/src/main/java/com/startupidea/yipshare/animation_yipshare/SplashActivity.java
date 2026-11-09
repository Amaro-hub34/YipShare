package com.startupidea.yipshare.animation_yipshare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.startupidea.yipshare.PantallaPrincipalActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.SqliteUserRepository;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Splash);
        super.onCreate(savedInstanceState);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, hasActiveSession() ? PantallaPrincipalActivity.class : MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }

    // Closing the app keeps the user logged in; only logging out or deleting the account ends the session.
    private boolean hasActiveSession() {
        SessionRepository session = new SessionRepository(this);
        boolean active;
        try {
            active = new UserService(new SqliteUserRepository(new DatabaseDriver(this))).isActiveSession(session.getUserId());
        } catch (AppException e) {
            active = false;
        }
        if (!active) session.deleteSession();
        return active;
    }
}
