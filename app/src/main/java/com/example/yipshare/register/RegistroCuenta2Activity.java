package com.example.yipshare.register;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yipshare.MainActivity;
import com.example.yipshare.pantallaPrincipalActivity;
import com.example.yipshare.R;

public class RegistroCuenta2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrocuenta2);

        findViewById(R.id.btn_siguiente).setOnClickListener(v ->
                startActivity(new Intent(RegistroCuenta2Activity.this, pantallaPrincipalActivity.class))
        );
        findViewById(R.id.btn_regresar_registro2).setOnClickListener(v ->
                startActivity(new Intent(RegistroCuenta2Activity.this, RegistroCuenta1Activity.class))
        );
        findViewById(R.id.btn_regresar_main).setOnClickListener(v ->
                startActivity(new Intent(RegistroCuenta2Activity.this, MainActivity.class))
        );
    }
}
