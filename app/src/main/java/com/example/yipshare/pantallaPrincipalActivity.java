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

        boolean hayArticulos = true;
        findViewById(R.id.group_empty_state).setVisibility(hayArticulos ? View.GONE : View.VISIBLE);
        findViewById(R.id.group_content_state).setVisibility(hayArticulos ? View.VISIBLE : View.GONE);

        RecyclerView rvMisArticulos = findViewById(R.id.rv_mis_articulos);
        rvMisArticulos.setAdapter(new articuloAdapter(1));

        findViewById(R.id.iv_agregar_top).setOnClickListener(v -> mostrarMenuAgregar());

        findViewById(R.id.iv_notificaciones).setOnClickListener(v -> {
            startActivity(new Intent(pantallaPrincipalActivity.this, notificacionActivity.class));
        });

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

}