package com.startupidea.yipshare;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import com.startupidea.yipshare.error.BadRequestException;
import com.startupidea.yipshare.error.ResourceNotFound;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.condominium.ApiCondominiumRepository;
import com.startupidea.yipshare.condominium.CondominiumService;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.SessionRepository;

public class IngresoCondominioActivity extends AppCompatActivity {

    private CondominiumService condominiumService;
    private EditText codeField;
    private EditText buildingField;
    private EditText apartmentField;
    private TextView codeErrorLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ingreso_condominio);

        condominiumService = new CondominiumService(new ApiCondominiumRepository(new ApiClient(this), this));
        codeField = findViewById(R.id.et_codigo_condominio);
        buildingField = findViewById(R.id.et_direccion_edificio);
        apartmentField = findViewById(R.id.et_direccion_departamento);
        codeErrorLabel = findViewById(R.id.tv_error_codigo);

        findViewById(R.id.btn_unirse).setOnClickListener(v -> tryJoinCondominium());

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        View bottomNav = findViewById(R.id.bottom_nav);
        int headerBase = (int) (70 * getResources().getDisplayMetrics().density);
        int navBase = (int) (64 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineHeader.setGuidelineBegin(headerBase + bars.top);

            bottomNav.setPadding(0, 0, 0, bars.bottom);
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            lp.height = navBase + bars.bottom;
            bottomNav.setLayoutParams(lp);

            return insets;
        });
        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(IngresoCondominioActivity.this, PantallaPrincipalActivity.class)));

        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(IngresoCondominioActivity.this, ReporteActivity.class)));
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(IngresoCondominioActivity.this, FormularioArticuloActivity.class)));
        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(IngresoCondominioActivity.this, PerfilActivity.class)));
        findViewById(R.id.nav_prestamos).setOnClickListener(v ->
                startActivity(new Intent(IngresoCondominioActivity.this, PrestamoActivity.class)));
    }

    private void tryJoinCondominium() {
        cleanGUI();

        String userId = new SessionRepository(this).getUserId();
        String code = codeField.getText().toString();
        String building = buildingField.getText().toString();
        String apartment = apartmentField.getText().toString();
        new LoadingOverlay(this).run(() -> condominiumService.joinCondominium(userId, code, building, apartment), () -> {
            Toast.makeText(this, R.string.solicitud_union_enviada, Toast.LENGTH_LONG).show();
            finish();
        }, this::showJoinError);
    }

    private void showJoinError(AppException e) {
        if (e instanceof ResourceNotFound) {
            codeErrorLabel.setText(e.getMessageRes());
            codeErrorLabel.setVisibility(View.VISIBLE);
        } else if (e instanceof BadRequestException) {
            apartmentField.setError(getString(e.getMessageRes()));
            apartmentField.requestFocus();
        } else {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cleanGUI() {
        codeErrorLabel.setVisibility(View.GONE);
        apartmentField.setError(null);
    }
}
