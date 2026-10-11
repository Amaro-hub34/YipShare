package com.example.yipshare;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroCuenta2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrocuenta2);

        findViewById(R.id.btn_siguiente).setOnClickListener(v ->
                startActivity(new Intent(RegistroCuenta2Activity.this, PantallaPrincipalActivity.class))
        );
    }
}
