package com.startupidea.yipshare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.item.ItemPayload;
import com.startupidea.yipshare.utils.ImagePreviewUtil;

public class DetalleArticuloActivity extends AppCompatActivity {

    private static final int IMAGE_MAX_SIZE_PX = 1080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_articulo);

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
        Item item = Item.fromIntent(getIntent());
        if (item != null) showItem(item.getPayload());

        findViewById(R.id.btn_accion_articulo).setOnClickListener(v -> {
            startActivity(new Intent(DetalleArticuloActivity.this, PantallaPrincipalActivity.class));
        });

        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(DetalleArticuloActivity.this, PantallaPrincipalActivity.class)));

        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(DetalleArticuloActivity.this, ReporteActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(DetalleArticuloActivity.this, PerfilActivity.class)));
        findViewById(R.id.nav_prestamos).setOnClickListener(v -> {
            startActivity(new Intent(DetalleArticuloActivity.this, PrestamoActivity.class));

        });
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(DetalleArticuloActivity.this, FormularioArticuloActivity.class)));

    }

    private void showItem(ItemPayload payload) {
        ((ImageView) findViewById(R.id.iv_imagen_articulo)).setImageBitmap(
                ImagePreviewUtil.tryLoad(this, Uri.parse(payload.getImageUri()), IMAGE_MAX_SIZE_PX));
        ((TextView) findViewById(R.id.tv_nombre_articulo)).setText(payload.getName());
        ((TextView) findViewById(R.id.tv_categoria)).setText(
                getString(R.string.DA_categoria_estado, payload.getCategory(), payload.getPhysicalCondition()));
        ((TextView) findViewById(R.id.tv_descripcion)).setText(payload.getDescription());
        ((Spinner) findViewById(R.id.sp_estado_fisico)).setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, payload.getLoanDurations()));
    }
}