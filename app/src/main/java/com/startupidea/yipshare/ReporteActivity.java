package com.startupidea.yipshare;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import com.startupidea.yipshare.feedback.SqliteFeedbackRepository;
import com.startupidea.yipshare.feedback.FeedbackService;
import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.utils.ImagePreviewUtil;

import java.util.ArrayList;
import java.util.List;


public class ReporteActivity extends AppCompatActivity{

    private static final int PREVIEW_MAX_SIZE_PX = 1080;
    private static final int EVIDENCE_HEIGHT_DP = 140;
    private static final int EVIDENCE_MARGIN_DP = 16;

    private final ActivityResultLauncher<PickVisualMediaRequest> imagePicker = registerForActivityResult(
            new ActivityResultContracts.PickMultipleVisualMedia(),
            uris -> {
                for (Uri uri : uris) addEvidence(uri);
            });

    private Loan loan;
    private EditText descriptionField;
    private LinearLayout evidenceList;
    private final List<String> evidenceUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loan = Loan.fromIntent(getIntent());
        if (loan == null) {
            Toast.makeText(this, R.string.reporte_bloqueado, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reporte);

        EditText neighborField = findViewById(R.id.et_nombre_vecino);
        neighborField.setText(loan.otherUsername());
        neighborField.setEnabled(false);
        descriptionField = findViewById(R.id.et_descripcion);
        evidenceList = findViewById(R.id.ll_evidencias);
        findViewById(R.id.btn_agregar_imagen).setOnClickListener(v -> imagePicker.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        Guideline guidelineStatus = findViewById(R.id.guideline_status);
        View bottomNav = findViewById(R.id.bottom_nav);
        int headerBase = (int) (48 * getResources().getDisplayMetrics().density);
        int navBase = (int) (64 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineStatus.setGuidelineBegin(bars.top);
            guidelineHeader.setGuidelineBegin(headerBase + bars.top);

            bottomNav.setPadding(0, 0, 0, bars.bottom);
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            lp.height = navBase + bars.bottom;
            bottomNav.setLayoutParams(lp);

            return insets;
        });
        findViewById(R.id.btn_enviar_Reporte).setOnClickListener(v -> tryCreateReport());
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(ReporteActivity.this, PantallaPrincipalActivity.class)));

        findViewById(R.id.nav_prestamos).setOnClickListener(v ->
                startActivity(new Intent(ReporteActivity.this, PrestamoActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(ReporteActivity.this, PerfilActivity.class)));
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(ReporteActivity.this, FormularioArticuloActivity.class)));
    }

    private void addEvidence(Uri uri) {
        Bitmap preview = ImagePreviewUtil.tryLoad(this, uri, PREVIEW_MAX_SIZE_PX);
        if (preview == null) {
            Toast.makeText(this, R.string.error_imagen_invalida, Toast.LENGTH_SHORT).show();
            return;
        }
        evidenceUris.add(uri.toString());
        ImageView evidenceImage = new ImageView(this);
        evidenceImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        evidenceImage.setContentDescription(getString(R.string.iv_preview_imagen));
        evidenceImage.setImageBitmap(preview);
        evidenceImage.setOnClickListener(v -> {
            evidenceUris.remove(evidenceList.indexOfChild(v));
            evidenceList.removeView(v);
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (EVIDENCE_HEIGHT_DP * getResources().getDisplayMetrics().density));
        params.topMargin = (int) (EVIDENCE_MARGIN_DP * getResources().getDisplayMetrics().density);
        evidenceList.addView(evidenceImage, params);
    }

    private void tryCreateReport() {
        descriptionField.setError(null);

        FeedbackService feedbackService = new FeedbackService(new SqliteFeedbackRepository(this));
        String userId = new SessionRepository(this).getUserId();
        String description = descriptionField.getText().toString();
        new LoadingOverlay(this).run(() -> feedbackService.createReport(userId, loan, description, evidenceUris), () -> {
            Toast.makeText(this, R.string.reporte_enviado, Toast.LENGTH_SHORT).show();
            finish();
        }, e -> {
            if (e.getMessageRes() == R.string.error_campo_obligatorio) {
                descriptionField.setError(getString(e.getMessageRes()));
                descriptionField.requestFocus();
            } else {
                Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
