package com.startupidea.yipshare;

import android.content.Intent;
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

import com.startupidea.yipshare.adapters.NotificacionAdapter;

public class NotificacionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notificacion);

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        Guideline guidelineStatus = findViewById(R.id.guideline_status);
        View bottomNav = findViewById(R.id.bottom_nav);
        int headerBase = (int) (70 * getResources().getDisplayMetrics().density);
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



        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(NotificacionActivity.this, PantallaPrincipalActivity.class)));

        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(NotificacionActivity.this, ReporteActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(NotificacionActivity.this, PerfilActivity.class)));
        findViewById(R.id.nav_prestamos).setOnClickListener(v -> {
            startActivity(new Intent(NotificacionActivity.this, PrestamoActivity.class));

        });
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(NotificacionActivity.this, FormularioArticuloActivity.class)));

        boolean notificationsExist = true;
        findViewById(R.id.tv_sin_notificaciones)
                .setVisibility(notificationsExist ? View.GONE : View.VISIBLE);
        findViewById(R.id.rv_notificaciones)
                .setVisibility(notificationsExist ? View.VISIBLE : View.GONE);

        RecyclerView notificationList = findViewById(R.id.rv_notificaciones);
        notificationList.setAdapter(new NotificacionAdapter(1));
    }
}