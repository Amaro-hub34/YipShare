package com.startupidea.yipshare.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.startupidea.yipshare.MainActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.TermsAndConditionsActivity;
import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.user.ApiUserRepository;
import com.startupidea.yipshare.user.InvalidField;
import com.startupidea.yipshare.user.RegistrationDraft;
import com.startupidea.yipshare.user.RegistrationField;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.UserService;

public class RegistroCuenta1Activity extends AppCompatActivity {

    private UserService userService;

    private final ActivityResultLauncher<Intent> credentialsScreen = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    draft = RegistrationDraft.fromIntent(result.getData());
                }
            });

    private RegistrationDraft draft = RegistrationDraft.EMPTY;
    private EditText paternalLastNameField;
    private EditText maternalLastNameField;
    private EditText firstNamesField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = new UserService(new ApiUserRepository(new ApiClient(this), new SessionRepository(this), this));
        setContentView(R.layout.activity_registrocuenta1);

        if (savedInstanceState != null) {
            draft = RegistrationDraft.fromBundle(savedInstanceState);
        }

        paternalLastNameField = findViewById(R.id.apellido_paterno);
        maternalLastNameField = findViewById(R.id.apellido_materno);
        firstNamesField = findViewById(R.id.nombres);

        findViewById(R.id.btn_siguiente).setOnClickListener(v -> tryNext());
        findViewById(R.id.boton_regresar_Main).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        findViewById(R.id.tv_terms).setOnClickListener(v ->
                startActivity(new Intent(this, TermsAndConditionsActivity.class)));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        draft.putInto(outState);
    }

    private void tryNext() {
        draft = draft.withNames(
                paternalLastNameField.getText().toString(),
                maternalLastNameField.getText().toString(),
                firstNamesField.getText().toString());

        try {
            userService.validateNames(draft);
            Intent intent = new Intent(this, RegistroCuenta2Activity.class);
            draft.putInto(intent);
            credentialsScreen.launch(intent);
        } catch (InvalidField e) {
            EditText input = inputFor(e.getField());
            input.setError(getString(e.getMessageRes()));
            input.requestFocus();
        }
    }

    private EditText inputFor(RegistrationField field) {
        return switch (field) {
            case PATERNAL_LAST_NAME -> paternalLastNameField;
            case MATERNAL_LAST_NAME -> maternalLastNameField;
            default -> firstNamesField;
        };
    }
}
