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

        TextView itemLabel = findViewById(R.id.tv_articulo_codigo);
        TextView codeLabel = findViewById(R.id.tv_codigo_generado);
        EditText codeField = findViewById(R.id.et_codigo);

        String item = getIntent().getStringExtra("item_name");
        if (item != null) {
            itemLabel.setText(item);
        }

        findViewById(R.id.btn_generar_codigo).setOnClickListener(v -> {
            String code = String.format("%06d", new Random().nextInt(1000000));
            codeLabel.setText(code);
            codeLabel.setVisibility(TextView.VISIBLE);
        });

        findViewById(R.id.btn_confirmar_codigo).setOnClickListener(v -> {
            String code = codeField.getText().toString().trim();
            if (code.length() != 6) {
                codeField.setError(getString(R.string.error_codigo_longitud));
            } else {
                Toast.makeText(CodigoPrestamoActivity.this, R.string.codigo_confirmado, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CodigoPrestamoActivity.this, CalificarUsuarioActivity.class));
                finish();
            }
        });
    }
}