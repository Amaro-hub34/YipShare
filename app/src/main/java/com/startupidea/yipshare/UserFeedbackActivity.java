package com.startupidea.yipshare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.SqliteUserRepository;
import com.startupidea.yipshare.user.UserProfile;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.FeedbackSectionUtil;

import java.util.Locale;

public class UserFeedbackActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "com.startupidea.yipshare.extra.USER_ID";

    public static Intent intentFor(Context context, String userId) {
        return new Intent(context, UserFeedbackActivity.class).putExtra(EXTRA_USER_ID, userId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_feedback);

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        Guideline guidelineStatus = findViewById(R.id.guideline_status);
        int headerBase = (int) (48 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineStatus.setGuidelineBegin(bars.top);
            guidelineHeader.setGuidelineBegin(headerBase + bars.top);
            findViewById(R.id.scroll_feedback).setPadding(0, 0, 0, bars.bottom);

            return insets;
        });
        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());

        String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        try {
            UserProfile profile = new UserService(new SqliteUserRepository(new DatabaseDriver(this))).getUserProfile(userId);
            ((TextView) findViewById(R.id.tv_titulo_pantalla)).setText(profile.getUsername());
            ((RatingBar) findViewById(R.id.rb_calificacion)).setRating(profile.getRating());
            ((TextView) findViewById(R.id.tv_calificacion_numero))
                    .setText(String.format(Locale.getDefault(), "%.1f", profile.getRating()));
        } catch (AppException e) {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FeedbackSectionUtil.attach(this, userId);
    }
}
