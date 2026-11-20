package com.startupidea.yipshare;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import com.startupidea.yipshare.user.InvalidField;
import com.startupidea.yipshare.user.RegistrationField;
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

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.register.RegistroCuenta1Activity;
import com.startupidea.yipshare.user.ApiUserRepository;
import com.startupidea.yipshare.user.LoginPayload;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.User;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.PasswordToggleUtil;

public class MainActivity extends AppCompatActivity {

    private UserService userService;

    private EditText usernameField;
    private EditText passwordField;
    private TextView errorLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = new UserService(new ApiUserRepository(new ApiClient(this), new SessionRepository(this), this));
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameField = findViewById(R.id.usuario_IS);
        passwordField = findViewById(R.id.et_password);
        errorLabel = findViewById(R.id.tv_error);
        PasswordToggleUtil.attach(passwordField, findViewById(R.id.btn_toggle_password));

        findViewById(R.id.btn_entrar).setOnClickListener(v -> sendLoginRequest());

        findViewById(R.id.boton_registrate).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegistroCuenta1Activity.class)));

        findViewById(R.id.tv_terms).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TermsAndConditionsActivity.class)));
    }

    private void sendLoginRequest() {
        errorLabel.setVisibility(View.GONE);
        LoginPayload payload = new LoginPayload(
                usernameField.getText().toString().trim(),
                passwordField.getText().toString());

        new LoadingOverlay(this).run(() -> userService.login(payload), user -> {
            new SessionRepository(this).createSession(user);
            Intent intent = new Intent(this, PantallaPrincipalActivity.class);
            user.putInto(intent);
            startActivity(intent);
            finish();
        }, this::showLoginError);
    }

    private void showLoginError(AppException e) {
        if (e instanceof InvalidField invalidField) {
            EditText field = switch (invalidField.getField()) {
                case PASSWORD -> passwordField;
                default -> usernameField;
            };
            field.setError(getString(e.getMessageRes()));
            field.requestFocus();
        } else {
            errorLabel.setText(e.getMessageRes());
            errorLabel.setVisibility(View.VISIBLE);
        }
    }
}
