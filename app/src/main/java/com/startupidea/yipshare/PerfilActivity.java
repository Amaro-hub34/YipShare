package com.startupidea.yipshare;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import com.startupidea.yipshare.utils.ConfirmDialogUtil;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.startupidea.yipshare.animation_yipshare.LoadingOverlay;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.error.UnauthorizedException;
import com.startupidea.yipshare.user.SqliteUserRepository;
import com.startupidea.yipshare.user.InvalidField;
import com.startupidea.yipshare.user.ProfilePayload;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.UserProfile;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.FeedbackSectionUtil;
import com.startupidea.yipshare.utils.ImagePreviewUtil;
import com.startupidea.yipshare.utils.PhoneInputUtil;
import com.startupidea.yipshare.utils.TaskUtil;

import java.util.Locale;

import com.google.android.material.bottomsheet.BottomSheetDialog;


public class PerfilActivity extends AppCompatActivity {

    private static final int PROFILE_PICTURE_SIZE_PX = 320;

    private final ActivityResultLauncher<PickVisualMediaRequest> profilePicturePicker = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) tryChangeProfilePicture(uri);
            });

    private UserService userService;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        userService = new UserService(new SqliteUserRepository(this));
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

        findViewById(R.id.iv_foto_perfil).setOnClickListener(v -> profilePicturePicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        findViewById(R.id.btn_actualizar_datos).setOnClickListener(v -> showUpdateSheet());
        findViewById(R.id.btn_eliminar_cuenta).setOnClickListener(v -> confirm(
                R.string.confirmar_eliminar_cuenta, this::tryDeleteAccount));
    }

    private void showUpdateSheet() {
        TaskUtil.run(this, () -> userService.getUserProfile(userId), profile -> showUpdateSheet(profile), this::showError);
    }

    private void showUpdateSheet(UserProfile profile) {
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
        EditText phoneNumberField = sheet.findViewById(R.id.telefono);
        EditText emailField = sheet.findViewById(R.id.et_correo);
        PhoneInputUtil phoneInput = PhoneInputUtil.attach(sheet.findViewById(R.id.spinner_codigo_pais), phoneNumberField);

        usernameField.setText(profile.username());
        paternalLastNameField.setText(profile.paternalLastName());
        maternalLastNameField.setText(profile.maternalLastName());
        firstNamesField.setText(profile.firstNames());
        phoneInput.setPhoneNumber(profile.phoneNumber());
        emailField.setText(profile.email());

        View updateButton = sheet.findViewById(R.id.btn_actualizar_datos);
        EditText[] fields = {usernameField, firstNamesField, paternalLastNameField, maternalLastNameField,
                passwordField, passwordConfirmationField, currentPasswordField, emailField};
        String[] saved = {profile.username(), profile.firstNames(), profile.paternalLastName(),
                profile.maternalLastName(), "", "", "", profile.email()};
        TextWatcher changeWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                boolean changed = !phoneInput.getPhoneNumber().equals(profile.phoneNumber());
                for (int i = 0; i < fields.length; i++) {
                    changed |= !fields[i].getText().toString().trim().equals(saved[i] == null ? "" : saved[i].trim());
                }
                updateButton.setEnabled(changed);
                updateButton.setAlpha(changed ? 1f : 0.5f);
            }
        };
        for (EditText field : fields) field.addTextChangedListener(changeWatcher);
        phoneNumberField.addTextChangedListener(changeWatcher);
        ((Spinner) sheet.findViewById(R.id.spinner_codigo_pais)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeWatcher.afterTextChanged(null);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        changeWatcher.afterTextChanged(null);

        updateButton.setOnClickListener(v -> confirm(R.string.confirmar_actualizar_perfil, () -> {
            for (EditText field : new EditText[]{usernameField, passwordField, passwordConfirmationField, currentPasswordField,
                    paternalLastNameField, maternalLastNameField, firstNamesField, phoneNumberField, emailField}) {
                field.setError(null);
            }

            ProfilePayload payload = new ProfilePayload(
                    usernameField.getText().toString(),
                    firstNamesField.getText().toString(),
                    paternalLastNameField.getText().toString(),
                    maternalLastNameField.getText().toString(),
                    phoneInput.getPhoneNumber(),
                    emailField.getText().toString());
            String newPassword = passwordField.getText().toString();
            String passwordConfirmation = passwordConfirmationField.getText().toString();
            String currentPassword = currentPasswordField.getText().toString();
            new LoadingOverlay(this).run(
                    () -> userService.modifyProfile(userId, payload, newPassword, passwordConfirmation, currentPassword),
                    () -> {
                        Toast.makeText(this, R.string.perfil_actualizado, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        showProfile();
                    },
                    e -> {
                        if (e instanceof InvalidField invalidField) {
                            EditText field = switch (invalidField.getField()) {
                                case USERNAME -> usernameField;
                                case PASSWORD -> passwordField;
                                case PASSWORD_CONFIRMATION -> passwordConfirmationField;
                                case PATERNAL_LAST_NAME -> paternalLastNameField;
                                case MATERNAL_LAST_NAME -> maternalLastNameField;
                                case PHONE_NUMBER -> phoneNumberField;
                                case EMAIL -> emailField;
                                default -> firstNamesField;
                            };
                            showInputError(field, e);
                        } else if (e instanceof UnauthorizedException) {
                            showInputError(currentPasswordField, e);
                        } else {
                            showError(e);
                        }
                    });
        }));

        dialog.show();
    }

    private void tryChangeProfilePicture(Uri uri) {
        new LoadingOverlay(this).run(() -> userService.modifyProfilePicture(userId, uri.toString()), () -> {
            Toast.makeText(this, R.string.foto_perfil_actualizada, Toast.LENGTH_SHORT).show();
            showProfile();
        }, this::showError);
    }

    private void tryDeleteAccount() {
        new LoadingOverlay(this).run(() -> userService.deleteAccount(userId), () -> {
            new SessionRepository(this).deleteSession();
            Toast.makeText(this, R.string.cuenta_eliminada, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, this::showError);
    }

    private void showError(AppException e) {
        Toast.makeText(this, e.getMessageRes(), Toast.LENGTH_SHORT).show();
    }

    private void confirm(int message, Runnable onConfirm) {
        ConfirmDialogUtil.show(this, getString(message), onConfirm);
    }

    private void showInputError(EditText field, AppException e) {
        field.setError(getString(e.getMessageRes()));
        field.requestFocus();
    }

    private void showProfile() {
        FeedbackSectionUtil.attach(this, userId);
        TaskUtil.run(this, () -> userService.getUserProfile(userId), profile -> {
            showProfilePicture(profile.profilePictureUri());
            ((TextView) findViewById(R.id.et_correo_electronico)).setText(profile.username());
            ((TextView) findViewById(R.id.tv_correo)).setText(profile.email());
            ((TextView) findViewById(R.id.apellido_paterno)).setText(profile.paternalLastName());
            ((TextView) findViewById(R.id.apellido_materno)).setText(profile.maternalLastName());
            ((TextView) findViewById(R.id.nombres)).setText(profile.firstNames());
            ((TextView) findViewById(R.id.telefono)).setText(profile.phoneNumber());
            ((RatingBar) findViewById(R.id.rb_mi_calificacion)).setRating(profile.getRating());
            ((TextView) findViewById(R.id.tv_calificacion_numero))
                    .setText(String.format(Locale.getDefault(), "%.1f", profile.getRating()));
        }, this::showError);
    }

    private void showProfilePicture(String pictureUri) {
        ImageView profilePictureImage = findViewById(R.id.iv_foto_perfil);
        Bitmap picture = pictureUri == null ? null : ImagePreviewUtil.tryLoad(this, Uri.parse(pictureUri), PROFILE_PICTURE_SIZE_PX);
        if (picture == null) profilePictureImage.setImageResource(R.drawable.ic_person);
        else profilePictureImage.setImageBitmap(picture);
    }
}
