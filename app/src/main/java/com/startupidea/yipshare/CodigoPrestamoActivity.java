package com.startupidea.yipshare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class CodigoPrestamoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_codigo_prestamo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });

        TextView tvArticulo = findViewById(R.id.tv_articulo_codigo);
        TextView tvCodigo = findViewById(R.id.tv_codigo_generado);
        EditText etCodigo = findViewById(R.id.et_codigo);

        String articulo = getIntent().getStringExtra("articulo");
        if (articulo != null) {
            tvArticulo.setText(articulo);
        }

        findViewById(R.id.btn_generar_codigo).setOnClickListener(v -> {
            String codigo = String.format("%06d", new Random().nextInt(1000000));
            tvCodigo.setText(codigo);
            tvCodigo.setVisibility(TextView.VISIBLE);
        });

        findViewById(R.id.btn_confirmar_codigo).setOnClickListener(v -> {
            String codigo = etCodigo.getText().toString().trim();
            if (codigo.length() != 6) {
                etCodigo.setError(getString(R.string.error_codigo_longitud));
            } else {
                Toast.makeText(CodigoPrestamoActivity.this, R.string.codigo_confirmado, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CodigoPrestamoActivity.this, CalificarUsuarioActivity.class));
                finish();
            }
        });
    }
}