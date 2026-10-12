package com.example.yipshare;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class termsAndConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        findViewById(R.id.btn_volver_main).setOnClickListener(v ->
                startActivity(new Intent(termsAndConditionsActivity.this, MainActivity.class))
        );
    }
}
