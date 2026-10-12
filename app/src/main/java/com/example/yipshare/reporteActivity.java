package com.example.yipshare;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


public class reporteActivity  extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);
        findViewById(R.id.btn_regresar).setOnClickListener(v -> {
            startActivity(new Intent(reporteActivity.this, pantallaPrincipalActivity.class));
        });
        findViewById(R.id.btn_enviar_Reporte).setOnClickListener(v -> {
            startActivity(new Intent(reporteActivity.this, pantallaPrincipalActivity.class));
        });
    }
}
