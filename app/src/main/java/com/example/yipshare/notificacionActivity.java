package com.example.yipshare;

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

import com.example.yipshare.adapters.notificacionAdapter;

public class notificacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notificacion);

        Guideline lineaGuiaCabeza = findViewById(R.id.guideline_header);
        Guideline lineaGuiaEstado = findViewById(R.id.guideline_status);
        View bottomNav = findViewById(R.id.bottom_nav);
        int headerBase = (int) (70 * getResources().getDisplayMetrics().density);
        int navBase = (int) (64 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            lineaGuiaEstado.setGuidelineBegin(bars.top);
            lineaGuiaCabeza.setGuidelineBegin(headerBase + bars.top);

            bottomNav.setPadding(0, 0, 0, bars.bottom);
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            lp.height = navBase + bars.bottom;
            bottomNav.setLayoutParams(lp);

            return insets;
        });

        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());

        boolean hayNotificaciones = true;
        findViewById(R.id.tv_sin_notificaciones)
                .setVisibility(hayNotificaciones ? View.GONE : View.VISIBLE);
        findViewById(R.id.rv_notificaciones)
                .setVisibility(hayNotificaciones ? View.VISIBLE : View.GONE);

        RecyclerView rvNotificaciones = findViewById(R.id.rv_notificaciones);
        rvNotificaciones.setAdapter(new notificacionAdapter(1));
    }
}