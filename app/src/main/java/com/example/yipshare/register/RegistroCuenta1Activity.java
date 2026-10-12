package com.example.yipshare.register;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yipshare.MainActivity;
import com.example.yipshare.R;
import com.example.yipshare.termsAndConditionsActivity;

public class RegistroCuenta1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrocuenta1);

        findViewById(R.id.btn_siguiente).setOnClickListener(v ->
                startActivity(new Intent(RegistroCuenta1Activity.this, RegistroCuenta2Activity.class))
        );
        findViewById(R.id.boton_regresar_Main).setOnClickListener(v ->
                startActivity(new Intent(RegistroCuenta1Activity.this, MainActivity.class))
        );
        findViewById(R.id.tv_terms).setOnClickListener(v ->
                startActivity(new Intent(RegistroCuenta1Activity.this, termsAndConditionsActivity.class))
        );
    }
}