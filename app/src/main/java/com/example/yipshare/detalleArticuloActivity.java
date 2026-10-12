package com.example.yipshare;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class detalleArticuloActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_articulo);
        findViewById(R.id.btn_regresar).setOnClickListener(v -> finish());
    }
}
