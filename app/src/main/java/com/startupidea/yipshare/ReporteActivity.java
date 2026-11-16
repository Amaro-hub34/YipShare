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
import android.widget.Toast;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.feedback.EmptyReportText;
import com.startupidea.yipshare.feedback.FeedbackService;
import com.startupidea.yipshare.feedback.ApiFeedbackRepository;
import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.utils.ImagePreviewUtil;


public class ReporteActivity extends AppCompatActivity{

    private static final int PREVIEW_MAX_SIZE_PX = 1080;

    private final ActivityResultLauncher<PickVisualMediaRequest> imagePicker = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) showEvidence(uri);
            });

    private Loan loan;
    private EditText descriptionField;
    private ImageView evidenceImage;
    private Uri evidenceUri;

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
        neighborField.setText(loan.getOtherUsername());
        neighborField.setEnabled(false);
        descriptionField = findViewById(R.id.et_descripcion);
        evidenceImage = findViewById(R.id.iv_preview_imagen);
        findViewById(R.id.btn_cambiar_imagen2).setOnClickListener(v -> imagePicker.launch(
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

    private void showEvidence(Uri uri) {
        Bitmap preview = ImagePreviewUtil.tryLoad(this, uri, PREVIEW_MAX_SIZE_PX);
        if (preview == null) {
            Toast.makeText(this, R.string.error_imagen_invalida, Toast.LENGTH_SHORT).show();
            return;
        }
        evidenceUri = uri;
        evidenceImage.setImageBitmap(preview);
        evidenceImage.setVisibility(View.VISIBLE);
    }

    private void tryCreateReport() {
        descriptionField.setError(null);

        FeedbackService feedbackService = new FeedbackService(new ApiFeedbackRepository(new ApiClient(this), this));
        String userId = new SessionRepository(this).getUserId();
        String description = descriptionField.getText().toString();
        String image = evidenceUri == null ? null : evidenceUri.toString();
        new LoadingOverlay(this).run(() -> feedbackService.createReport(userId, loan, description, image), () -> {
            Toast.makeText(this, R.string.reporte_enviado, Toast.LENGTH_SHORT).show();
            finish();
        }, e -> {
            if (e instanceof EmptyReportText) {
                descriptionField.setError(getString(e.getMessageRes()));
                descriptionField.requestFocus();
            } else {
                Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
