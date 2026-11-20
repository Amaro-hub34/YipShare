package com.startupidea.yipshare;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.feedback.ApiFeedbackRepository;
import com.startupidea.yipshare.feedback.FeedbackService;
import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.user.SessionRepository;

public class CalificarUsuarioActivity extends AppCompatActivity {

    private Loan loan;
    private RatingBar ratingBar;
    private EditText commentField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calificar_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loan = Loan.fromIntent(getIntent());
        ratingBar = findViewById(R.id.ratingBar);
        commentField = findViewById(R.id.etComentario);
        ((TextView) findViewById(R.id.tvNombreUsuario)).setText(loan.otherUsername());

        findViewById(R.id.btnRegresar).setOnClickListener(v -> finish());
        findViewById(R.id.btnEnviarCalificacion).setOnClickListener(v -> tryCreateReview());
    }

    private void tryCreateReview() {
        FeedbackService feedbackService = new FeedbackService(new ApiFeedbackRepository(new ApiClient(this), this));
        String userId = new SessionRepository(this).getUserId();
        float stars = ratingBar.getRating();
        String comment = commentField.getText().toString();
        new LoadingOverlay(this).run(() -> feedbackService.createReview(userId, loan, stars, comment), () -> {
            Toast.makeText(this, R.string.calificacion_enviada, Toast.LENGTH_SHORT).show();
            finish();
        }, e -> Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show());
    }
}
