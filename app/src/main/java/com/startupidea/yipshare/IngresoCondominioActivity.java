package com.startupidea.yipshare;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
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

import com.startupidea.yipshare.condominium.CondominiumNotFound;
import com.startupidea.yipshare.condominium.CondominiumService;
import com.startupidea.yipshare.condominium.EmptyApartment;
import com.startupidea.yipshare.condominium.SqliteCondominiumRepository;
import com.startupidea.yipshare.database.DatabaseDriver;
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

        condominiumService = new CondominiumService(new SqliteCondominiumRepository(new DatabaseDriver(this), this));
        codeField = findViewById(R.id.et_codigo_condominio);
        buildingField = findViewById(R.id.et_direccion_edificio);
        apartmentField = findViewById(R.id.et_direccion_departamento);
        codeErrorLabel = findViewById(R.id.tv_error_codigo);

        findViewById(R.id.btn_unirse).setOnClickListener(v -> new LoadingOverlay(this).run(this::tryJoinCondominium));

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

        try {
            condominiumService.joinCondominium(
                    new SessionRepository(this).getUserId(),
                    codeField.getText().toString(),
                    buildingField.getText().toString(),
                    apartmentField.getText().toString());
            Toast.makeText(this, R.string.solicitud_union_enviada, Toast.LENGTH_LONG).show();
            finish();
        } catch (CondominiumNotFound e) {
            codeErrorLabel.setText(e.getMessageRes());
            codeErrorLabel.setVisibility(View.VISIBLE);
        } catch (EmptyApartment e) {
            apartmentField.setError(getString(e.getMessageRes()));
            apartmentField.requestFocus();
        } catch (AppException e) {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cleanGUI() {
        codeErrorLabel.setVisibility(View.GONE);
        apartmentField.setError(null);
    }
}
