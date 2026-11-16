package com.startupidea.yipshare.register;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;


import com.startupidea.yipshare.PantallaPrincipalActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.ApiUserRepository;
import com.startupidea.yipshare.user.DuplicateEmail;
import com.startupidea.yipshare.user.DuplicatePhoneNumber;
import com.startupidea.yipshare.user.DuplicateUsername;
import com.startupidea.yipshare.user.InvalidField;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.RegistrationDraft;
import com.startupidea.yipshare.user.RegistrationField;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.PasswordToggleUtil;
import com.startupidea.yipshare.utils.PhoneInputUtil;

import com.startupidea.yipshare.MainActivity;

public class RegistroCuenta2Activity extends AppCompatActivity {

    private UserService userService;

    private RegistrationDraft draft;
    private EditText usernameField;
    private EditText passwordField;
    private EditText passwordConfirmationField;
    private EditText phoneNumberField;
    private EditText emailField;
    private PhoneInputUtil phoneInput;
    private TextView errorLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = new UserService(new ApiUserRepository(new ApiClient(this), new SessionRepository(this)));
        setContentView(R.layout.activity_registrocuenta2);

        draft = RegistrationDraft.fromIntent(getIntent());

        usernameField = findViewById(R.id.usuario_IS);
        passwordField = findViewById(R.id.contrasenia);
        passwordConfirmationField = findViewById(R.id.contrasenia_confirmacion2);
        phoneNumberField = findViewById(R.id.telefono_frame2);
        emailField = findViewById(R.id.et_correo);
        phoneInput = PhoneInputUtil.attach(findViewById(R.id.spinner_codigo_pais), phoneNumberField);
        errorLabel = findViewById(R.id.tv_error);
        PasswordToggleUtil.attach(passwordField, findViewById(R.id.btn_toggle_password));
        PasswordToggleUtil.attach(passwordConfirmationField, findViewById(R.id.btn_toggle_password_confirmation));

        if (savedInstanceState == null) {
            usernameField.setText(draft.getUsername());
            passwordField.setText(draft.getPassword());
            passwordConfirmationField.setText(draft.getPasswordConfirmation());
            phoneInput.setPhoneNumber(draft.getPhoneNumber());
            emailField.setText(draft.getEmail());
        }

        findViewById(R.id.btn_siguiente).setOnClickListener(v -> tryRegister());
        findViewById(R.id.btn_regresar_registro2).setOnClickListener(v -> returnToPrevious());
        findViewById(R.id.btn_regresar_main).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToPrevious();
            }
        });
    }

    private RegistrationDraft currentDraft() {
        return draft.withCredentials(
                usernameField.getText().toString(),
                passwordField.getText().toString(),
                passwordConfirmationField.getText().toString(),
                phoneInput.getPhoneNumber(),
                emailField.getText().toString());
    }

    private void returnToPrevious() {
        Intent result = new Intent();
        currentDraft().putInto(result);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private void tryRegister() {
        errorLabel.setVisibility(View.GONE);

        RegistrationDraft submitted = currentDraft();
        new LoadingOverlay(this).run(() -> userService.register(submitted), user -> {
            new SessionRepository(this).createSession(user);
            Intent intent = new Intent(this, PantallaPrincipalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            user.putInto(intent);
            startActivity(intent);
        }, this::showRegisterError);
    }

    private void showRegisterError(AppException e) {
        if (e instanceof InvalidField) {
            showInputError(inputFor(((InvalidField) e).getField()), e);
        } else if (e instanceof DuplicateUsername) {
            showInputError(usernameField, e);
        } else if (e instanceof DuplicatePhoneNumber) {
            showInputError(phoneNumberField, e);
        } else if (e instanceof DuplicateEmail) {
            showInputError(emailField, e);
        } else {
            errorLabel.setText(e.getMessageRes());
            errorLabel.setVisibility(View.VISIBLE);
        }
    }

    private void showInputError(EditText input, AppException e) {
        input.setError(getString(e.getMessageRes()));
        input.requestFocus();
    }

    private EditText inputFor(RegistrationField field) {
        switch (field) {
            case USERNAME:
                return usernameField;
            case PASSWORD:
                return passwordField;
            case PHONE_NUMBER:
                return phoneNumberField;
            case EMAIL:
                return emailField;
            default:
                return passwordConfirmationField;
        }
    }
}
