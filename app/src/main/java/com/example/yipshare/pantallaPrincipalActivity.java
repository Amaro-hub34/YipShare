package com.example.yipshare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;

import com.example.yipshare.adapters.condominioAdapter;

import java.util.Arrays;

import com.example.yipshare.adapters.articuloAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class pantallaPrincipalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_principal);

        Guideline guidelineStatus = findViewById(R.id.guideline_status);
        View bottomNav = findViewById(R.id.bottom_nav);
        int navBaseHeight = (int) (64 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineStatus.setGuidelineBegin(bars.top);

            bottomNav.setPadding(0, 0, 0, bars.bottom);
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            lp.height = navBaseHeight + bars.bottom;
            bottomNav.setLayoutParams(lp);

            return insets;
        });

        boolean articlesExist = true;
        findViewById(R.id.group_empty_state).setVisibility(articlesExist ? View.GONE : View.VISIBLE);
        findViewById(R.id.group_content_state).setVisibility(articlesExist ? View.VISIBLE : View.GONE);

        RecyclerView rvMisArticulos = findViewById(R.id.rv_mis_articulos);
        rvMisArticulos.setAdapter(new articuloAdapter(1));

        RecyclerView rvArticulosDisponibles = findViewById(R.id.rv_articulos_disponibles);
        rvArticulosDisponibles.setAdapter(new articuloAdapter(1));

        findViewById(R.id.iv_agregar_top).setOnClickListener(v -> mostrarMenuAgregar());

        findViewById(R.id.iv_notificaciones).setOnClickListener(v ->
                startActivity(new Intent(pantallaPrincipalActivity.this, notificacionActivity.class)));

        findViewById(R.id.nav_prestamos).setOnClickListener(v ->
                startActivity(new Intent(pantallaPrincipalActivity.this, prestamoActivity.class)));

        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(pantallaPrincipalActivity.this, reporteActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(pantallaPrincipalActivity.this, perfilActivity.class)));
        findViewById(R.id.logo_yipshare).setOnClickListener(v -> mostrarPanelCondominios());
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(pantallaPrincipalActivity.this, formularioArticuloActivity.class)));
    }

    private void mostrarMenuAgregar() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.boton_agregar_sobreposicion, null);
        dialog.setContentView(sheet);

        ((View) sheet.getParent()).setBackgroundColor(Color.TRANSPARENT);

        sheet.findViewById(R.id.btn_sheet_crear).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(pantallaPrincipalActivity.this, registrarCondominioActivity.class));
        });

        sheet.findViewById(R.id.btn_sheet_unirse).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(pantallaPrincipalActivity.this, ingresoCondominioActivity.class));
        });

        dialog.show();
    }
    private void mostrarPanelCondominios() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View panel = getLayoutInflater().inflate(R.layout.activity_panel_condominios, null);
        dialog.setContentView(panel);

        RecyclerView rv = panel.findViewById(R.id.rv_condominios);
        rv.setAdapter(new condominioAdapter(
                Arrays.asList("Torres del Valle", "Residencial Los Pinos", "Condominio Reforma"),
                nombre -> {
                    ((TextView) findViewById(R.id.tv_condominio)).setText(nombre);
                    dialog.dismiss();
                }));

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.START);
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }

        dialog.show();
    }
}