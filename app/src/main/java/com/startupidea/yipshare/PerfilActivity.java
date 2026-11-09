package com.startupidea.yipshare;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.DuplicateUsername;
import com.startupidea.yipshare.user.InvalidField;
import com.startupidea.yipshare.user.ProfilePayload;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.SqliteUserRepository;
import com.startupidea.yipshare.user.UserProfile;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.user.WrongPassword;
import com.startupidea.yipshare.utils.AsteriskPasswordTransformation;
import com.startupidea.yipshare.utils.FeedbackSectionUtil;

import java.util.Locale;

import com.google.android.material.bottomsheet.BottomSheetDialog;


public class PerfilActivity extends AppCompatActivity {

    private UserService userService;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        userService = new UserService(new SqliteUserRepository(new DatabaseDriver(this)));
        userId = new SessionRepository(this).getUserId();
        showProfile();

        Guideline guidelineHeader = findViewById(R.id.guideline_header);
        Guideline guidelineStatus = findViewById(R.id.guideline_status);

        View bottomNav = findViewById(R.id.bottom_nav);
        int headerBase = (int) (48 * getResources().getDisplayMetrics().density);
        int navBase = (int) (64 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            guidelineHeader.setGuidelineBegin(headerBase + bars.top);
            guidelineStatus.setGuidelineBegin(bars.top);

            bottomNav.setPadding(0, 0, 0, bars.bottom);
            ViewGroup.LayoutParams lp = bottomNav.getLayoutParams();
            lp.height = navBase + bars.bottom;
            bottomNav.setLayoutParams(lp);

            return insets;
        });
        findViewById(R.id.nav_inicio).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, PantallaPrincipalActivity.class)));
        findViewById(R.id.nav_reportes).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, ReporteActivity.class)));
        findViewById(R.id.nav_agregar).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, FormularioArticuloActivity.class)));

        findViewById(R.id.nav_prestamos).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, PrestamoActivity.class)));

        findViewById(R.id.nav_perfil).setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, PerfilActivity.class)));

        findViewById(R.id.btn_actualizar_datos).setOnClickListener(v -> showUpdateSheet());
        findViewById(R.id.btn_eliminar_cuenta).setOnClickListener(v -> confirm(
                R.string.confirmar_eliminar_cuenta, this::tryDeleteAccount));
    }

    private void showUpdateSheet() {
        UserProfile profile;
        try {
            profile = userService.getUserProfile(userId);
        } catch (AppException e) {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.activity_actualizar_perfil, null);
        dialog.setContentView(sheet);
        ((View) sheet.getParent()).setBackgroundColor(Color.TRANSPARENT);

        EditText usernameField = sheet.findViewById(R.id.et_correo_electronico);
        EditText passwordField = sheet.findViewById(R.id.et_contrasena);
        EditText passwordConfirmationField = sheet.findViewById(R.id.et_confirmar_contrasena);
        EditText currentPasswordField = sheet.findViewById(R.id.et_contrasena_actual);
        EditText paternalLastNameField = sheet.findViewById(R.id.apellido_paterno);
        EditText maternalLastNameField = sheet.findViewById(R.id.apellido_materno);
        EditText firstNamesField = sheet.findViewById(R.id.nombres);

        usernameField.setText(profile.getUsername());
        paternalLastNameField.setText(profile.getPaternalLastName());
        maternalLastNameField.setText(profile.getMaternalLastName());
        firstNamesField.setText(profile.getFirstNames());
        passwordField.setTransformationMethod(new AsteriskPasswordTransformation());
        passwordConfirmationField.setTransformationMethod(new AsteriskPasswordTransformation());
        currentPasswordField.setTransformationMethod(new AsteriskPasswordTransformation());

        View updateButton = sheet.findViewById(R.id.btn_actualizar_datos);
        EditText[] fields = {usernameField, firstNamesField, paternalLastNameField, maternalLastNameField,
                passwordField, passwordConfirmationField, currentPasswordField};
        String[] saved = {profile.getUsername(), profile.getFirstNames(), profile.getPaternalLastName(),
                profile.getMaternalLastName(), "", "", ""};
        TextWatcher changeWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                boolean changed = false;
                for (int i = 0; i < fields.length; i++) {
                    changed |= !fields[i].getText().toString().trim().equals(saved[i] == null ? "" : saved[i].trim());
                }
                updateButton.setEnabled(changed);
                updateButton.setAlpha(changed ? 1f : 0.5f);
            }
        };
        for (EditText field : fields) field.addTextChangedListener(changeWatcher);
        changeWatcher.afterTextChanged(null);

        updateButton.setOnClickListener(v -> confirm(R.string.confirmar_actualizar_perfil, () -> {
            for (EditText field : new EditText[]{usernameField, passwordField, passwordConfirmationField, currentPasswordField,
                    paternalLastNameField, maternalLastNameField, firstNamesField}) {
                field.setError(null);
            }

            try {
                userService.modifyProfile(
                        userId,
                        new ProfilePayload(
                                usernameField.getText().toString(),
                                firstNamesField.getText().toString(),
                                paternalLastNameField.getText().toString(),
                                maternalLastNameField.getText().toString()),
                        passwordField.getText().toString(),
                        passwordConfirmationField.getText().toString(),
                        currentPasswordField.getText().toString());
                Toast.makeText(this, R.string.perfil_actualizado, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                showProfile();
            } catch (InvalidField e) {
                EditText field;
                switch (e.getField()) {
                    case USERNAME: field = usernameField; break;
                    case PASSWORD: field = passwordField; break;
                    case PASSWORD_CONFIRMATION: field = passwordConfirmationField; break;
                    case PATERNAL_LAST_NAME: field = paternalLastNameField; break;
                    case MATERNAL_LAST_NAME: field = maternalLastNameField; break;
                    default: field = firstNamesField; break;
                }
                showInputError(field, e);
            } catch (DuplicateUsername e) {
                showInputError(usernameField, e);
            } catch (WrongPassword e) {
                showInputError(currentPasswordField, e);
            } catch (AppException e) {
                Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
            }
        }));

        dialog.show();
    }

    private void tryDeleteAccount() {
        try {
            userService.deleteAccount(userId);
            new SessionRepository(this).deleteSession();
            Toast.makeText(this, R.string.cuenta_eliminada, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (AppException e) {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirm(int message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.opcion_si, (dialog, which) -> onConfirm.run())
                .setNegativeButton(R.string.opcion_cancelar, null)
                .show();
    }

    private void showInputError(EditText field, AppException e) {
        field.setError(getString(e.getMessageRes()));
        field.requestFocus();
    }

    private void showProfile() {
        try {
            UserProfile profile = userService.getUserProfile(userId);
            FeedbackSectionUtil.attach(this, userId);

            ((TextView) findViewById(R.id.et_correo_electronico)).setText(profile.getUsername());
            ((TextView) findViewById(R.id.apellido_paterno)).setText(profile.getPaternalLastName());
            ((TextView) findViewById(R.id.apellido_materno)).setText(profile.getMaternalLastName());
            ((TextView) findViewById(R.id.nombres)).setText(profile.getFirstNames());
            ((RatingBar) findViewById(R.id.rb_mi_calificacion)).setRating(profile.getRating());
            ((TextView) findViewById(R.id.tv_calificacion_numero))
                    .setText(String.format(Locale.getDefault(), "%.1f", profile.getRating()));
        } catch (AppException e) {
            Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
        }
    }
}
