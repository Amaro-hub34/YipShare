package com.startupidea.yipshare;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialog;


public class PerfilActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        Guideline guidelineStatus = findViewById(R.id.guideline_status);

        View bottomNav = findViewById(R.id.bottom_nav);
        int headerBase = (int) (70 * getResources().getDisplayMetrics().density);
        int navBase = (int) (64 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineHeader.setGuidelineBegin(headerBase + bars.top);
            guidelineStatus.setGuidelineBegin(bars.top);

            bottomNav.setPadding(0, 0, 0, bars.bottom);
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            lp.height = navBase + bars.bottom;
            bottomNav.setLayoutParams(lp);

            return insets;
        });
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, PantallaPrincipalActivity.class)));
        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, ReporteActivity.class)));
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, FormularioArticuloActivity.class)));

        findViewById(R.id.nav_prestamos).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, PrestamoActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, PerfilActivity.class)));

        findViewById(R.id.btn_actualizar_datos).setOnClickListener(v -> mostrarMenuActualizarDatos());

        findViewById(R.id.btn_eliminar_cuenta).setOnClickListener(v -> {
            startActivity(new Intent(PerfilActivity.this, MainActivity.class));

        });
    }

    private void mostrarMenuActualizarDatos() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.activity_actualizar_perfil, null);
        dialog.setContentView(sheet);

        ((View) sheet.getParent()).setBackgroundColor(Color.TRANSPARENT);

        sheet.findViewById(R.id.btn_actualizar_datos).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}