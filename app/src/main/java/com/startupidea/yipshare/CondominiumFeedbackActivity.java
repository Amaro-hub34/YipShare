package com.startupidea.yipshare;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.feedback.ApiFeedbackRepository;
import com.startupidea.yipshare.feedback.FeedbackService;
import com.startupidea.yipshare.feedback.Report;
import com.startupidea.yipshare.feedback.ReportState;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.utils.FeedbackSectionUtil;
import com.startupidea.yipshare.utils.TaskUtil;

public class CondominiumFeedbackActivity extends AppCompatActivity {

    private FeedbackService feedbackService;
    private String condominiumId;

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
        ((TextView) findViewById(R.id.tv_titulo_pantalla)).setText(R.string.btn_ver_reportes_condominio);
        ((View) findViewById(R.id.rb_calificacion).getParent()).setVisibility(View.GONE);

        feedbackService = new FeedbackService(new ApiFeedbackRepository(new ApiClient(this), this));
        condominiumId = new SessionRepository(this).getCondominiumId();
        showFeedback();
    }

    private void showFeedback() {
        TaskUtil.run(this, () -> feedbackService.getAllReviewsByCondominiumId(condominiumId),
                reviews -> TaskUtil.run(this, () -> feedbackService.getAllReportsByCondominiumId(condominiumId),
                        reports -> FeedbackSectionUtil.show(this, reviews, reports, this::askReportState),
                        this::showError),
                this::showError);
    }

    private void askReportState(Report report) {
        ReportState[] states = ReportState.values();
        String[] labels = new String[states.length];
        for (int i = 0; i < states.length; i++) labels[i] = getString(states[i].getMessageRes());

        new AlertDialog.Builder(this)
                .setTitle(R.string.cambiar_estado_reporte)
                .setSingleChoiceItems(labels, report.getState().ordinal(), (dialog, which) -> {
                    dialog.dismiss();
                    TaskUtil.run(this, () -> feedbackService.modifyReportState(condominiumId, report, states[which]),
                            this::showFeedback, this::showError);
                })
                .setNegativeButton(R.string.opcion_cancelar, null)
                .show();
    }

    private void showError(AppException e) {
        Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
    }
}
