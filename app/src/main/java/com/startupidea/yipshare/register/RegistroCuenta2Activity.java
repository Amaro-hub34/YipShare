package com.startupidea.yipshare.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.startupidea.yipshare.MainActivity;
import com.startupidea.yipshare.PantallaPrincipalActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.DuplicatePhoneNumber;
import com.startupidea.yipshare.user.DuplicateUsername;
import com.startupidea.yipshare.user.InvalidField;
import com.startupidea.yipshare.user.PendingUserRepository;
import com.startupidea.yipshare.user.RegistrationDraft;
import com.startupidea.yipshare.user.RegistrationField;
import com.startupidea.yipshare.user.User;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.PasswordToggleUtil;

public class RegistroCuenta2Activity extends AppCompatActivity {

    private final UserService userService = new UserService(new PendingUserRepository());

    private RegistrationDraft draft;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etPasswordConfirmation;
    private EditText etPhoneNumber;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrocuenta2);

        draft = RegistrationDraft.fromIntent(getIntent());

        etUsername = findViewById(R.id.usuario_IS);
        etPassword = findViewById(R.id.contraseña);
        etPasswordConfirmation = findViewById(R.id.contraseña_confirmacion);
        etPhoneNumber = findViewById(R.id.telefono);
        tvError = findViewById(R.id.tv_error);
        PasswordToggleUtil.attach(etPassword, findViewById(R.id.btn_toggle_password));
        PasswordToggleUtil.attach(etPasswordConfirmation, findViewById(R.id.btn_toggle_password_confirmation));

        if (savedInstanceState == null) {
            etUsername.setText(draft.getUsername());
            etPassword.setText(draft.getPassword());
            etPasswordConfirmation.setText(draft.getPasswordConfirmation());
            etPhoneNumber.setText(draft.getPhoneNumber());
        }

        findViewById(R.id.btn_siguiente).setOnClickListener(v -> register());
        findViewById(R.id.btn_regresar_registro2).setOnClickListener(v -> returnToNames());
        findViewById(R.id.btn_regresar_main).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnToNames();
            }
        });
    }

    private RegistrationDraft currentDraft() {
        return draft.withCredentials(
                etUsername.getText().toString(),
                etPassword.getText().toString(),
                etPasswordConfirmation.getText().toString(),
                etPhoneNumber.getText().toString());
    }

    private void returnToNames() {
        Intent result = new Intent();
        currentDraft().putInto(result);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private void register() {
        tvError.setVisibility(View.GONE);

        try {
            User user = userService.register(currentDraft());
            Intent intent = new Intent(this, PantallaPrincipalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            user.putInto(intent);
            startActivity(intent);
        } catch (InvalidField e) {
            showInputError(inputFor(e.getField()), e);
        } catch (DuplicateUsername e) {
            showInputError(etUsername, e);
        } catch (DuplicatePhoneNumber e) {
            showInputError(etPhoneNumber, e);
        } catch (AppException e) {
            tvError.setText(e.getMessageRes());
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void showInputError(EditText input, AppException e) {
        input.setError(getString(e.getMessageRes()));
        input.requestFocus();
    }

    private EditText inputFor(RegistrationField field) {
        switch (field) {
            case USERNAME:
                return etUsername;
            case PASSWORD:
                return etPassword;
            case PHONE_NUMBER:
                return etPhoneNumber;
            default:
                return etPasswordConfirmation;
        }
    }
}
