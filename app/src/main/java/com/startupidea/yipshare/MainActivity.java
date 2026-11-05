package com.startupidea.yipshare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.register.RegistroCuenta1Activity;
import com.startupidea.yipshare.user.EmptyPassword;
import com.startupidea.yipshare.user.EmptyUsername;
import com.startupidea.yipshare.user.LoginPayload;
import com.startupidea.yipshare.user.PendingUserRepository;
import com.startupidea.yipshare.user.User;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.PasswordToggleUtil;

public class MainActivity extends AppCompatActivity {

    private final UserService userService = new UserService(new PendingUserRepository());

    private EditText etUsername;
    private EditText etPassword;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.usuario_IS);
        etPassword = findViewById(R.id.et_password);
        tvError = findViewById(R.id.tv_error);
        PasswordToggleUtil.attach(etPassword, findViewById(R.id.btn_toggle_password));

        findViewById(R.id.btn_entrar).setOnClickListener(v -> login());

        findViewById(R.id.boton_registrate).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegistroCuenta1Activity.class)));

        findViewById(R.id.tv_terms).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TermsAndConditionsActivity.class)));
    }

    private void login() {
        tvError.setVisibility(View.GONE);
        LoginPayload payload = new LoginPayload(
                etUsername.getText().toString().trim(),
                etPassword.getText().toString());

        try {
            User user = userService.login(payload);
            Intent intent = new Intent(this, PantallaPrincipalActivity.class);
            user.putInto(intent);
            startActivity(intent);
            finish();
        } catch (EmptyUsername e) {
            etUsername.setError(getString(e.getMessageRes()));
            etUsername.requestFocus();
        } catch (EmptyPassword e) {
            etPassword.setError(getString(e.getMessageRes()));
            etPassword.requestFocus();
        } catch (AppException e) {
            tvError.setText(e.getMessageRes());
            tvError.setVisibility(View.VISIBLE);
        }

    }
}
