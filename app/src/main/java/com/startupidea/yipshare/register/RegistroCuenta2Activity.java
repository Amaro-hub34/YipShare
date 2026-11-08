package com.startupidea.yipshare.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import com.startupidea.yipshare.PantallaPrincipalActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.database.DatabaseDriver;
import com.startupidea.yipshare.error.AppException;
import com.startupidea.yipshare.user.DuplicatePhoneNumber;
import com.startupidea.yipshare.user.DuplicateUsername;
import com.startupidea.yipshare.user.InvalidField;
import com.startupidea.yipshare.user.SqliteUserRepository;
import com.startupidea.yipshare.user.SessionRepository;
import com.startupidea.yipshare.user.RegistrationDraft;
import com.startupidea.yipshare.user.RegistrationField;
import com.startupidea.yipshare.user.User;
import com.startupidea.yipshare.user.UserService;
import com.startupidea.yipshare.utils.PasswordToggleUtil;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import com.startupidea.yipshare.MainActivity;

public class RegistroCuenta2Activity extends AppCompatActivity {

    private static final String DEFAULT_REGION = "MX";

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private UserService userService;

    private RegistrationDraft draft;
    private EditText usernameField;
    private EditText passwordField;
    private EditText passwordConfirmationField;
    private EditText phoneNumberField;
    private Spinner countryCodeSpinner;
    private List<String> regions;
    private TextView errorLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = new UserService(new SqliteUserRepository(new DatabaseDriver(this)));
        setContentView(R.layout.activity_registrocuenta2);

        draft = RegistrationDraft.fromIntent(getIntent());

        usernameField = findViewById(R.id.usuario_IS);
        passwordField = findViewById(R.id.contrasenia);
        passwordConfirmationField = findViewById(R.id.contrasenia_confirmacion2);
        phoneNumberField = findViewById(R.id.telefono_frame2);
        countryCodeSpinner = findViewById(R.id.spinner_codigo_pais);
        setUpCountryCodes();
        errorLabel = findViewById(R.id.tv_error);
        PasswordToggleUtil.attach(passwordField, findViewById(R.id.btn_toggle_password));
        PasswordToggleUtil.attach(passwordConfirmationField, findViewById(R.id.btn_toggle_password_confirmation));

        if (savedInstanceState == null) {
            usernameField.setText(draft.getUsername());
            passwordField.setText(draft.getPassword());
            passwordConfirmationField.setText(draft.getPasswordConfirmation());
            showPhoneNumber(draft.getPhoneNumber());
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
                phoneNumberWithCountryCode());
    }

    private String phoneNumberWithCountryCode() {
        String localNumber = phoneNumberField.getText().toString().trim();
        if (localNumber.isEmpty()) return "";
        return "+" + countryCodeOf(regions.get(countryCodeSpinner.getSelectedItemPosition())) + localNumber;
    }

    private void setUpCountryCodes() {
        regions = new ArrayList<>(phoneUtil.getSupportedRegions());
        regions.sort(Comparator.comparing(this::countryName, Collator.getInstance()));

        List<String> labels = new ArrayList<>();
        for (String region : regions) {
            labels.add(flagOf(region) + " " + countryName(region) + " (+" + countryCodeOf(region) + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                String region = regions.get(position);
                view.setText(flagOf(region) + " +" + countryCodeOf(region));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countryCodeSpinner.setAdapter(adapter);

        String deviceRegion = Locale.getDefault().getCountry();
        selectRegion(regions.contains(deviceRegion) ? deviceRegion : DEFAULT_REGION);
    }

    private void showPhoneNumber(String phoneNumber) {
        if (phoneNumber.isEmpty()) return;
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, null);
            selectRegion(phoneUtil.getRegionCodeForCountryCode(number.getCountryCode()));
            phoneNumberField.setText(phoneUtil.getNationalSignificantNumber(number));
        } catch (NumberParseException e) {
            // Too short to parse: keep only the digits the user typed after the country code.
            phoneNumberField.setText(phoneNumber.replaceFirst("^\\+" + countryCodeOf(regions.get(countryCodeSpinner.getSelectedItemPosition())), ""));
        }
    }

    private void selectRegion(String region) {
        int position = regions.indexOf(region);
        if (position >= 0) countryCodeSpinner.setSelection(position);
    }

    private int countryCodeOf(String region) {
        return phoneUtil.getCountryCodeForRegion(region);
    }

    private String countryName(String region) {
        return new Locale("", region).getDisplayCountry();
    }

    private static String flagOf(String region) {
        int base = 0x1F1E6 - 'A';
        return new String(Character.toChars(base + region.charAt(0)))
                + new String(Character.toChars(base + region.charAt(1)));
    }

    private void returnToPrevious() {
        Intent result = new Intent();
        currentDraft().putInto(result);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private void tryRegister() {
        errorLabel.setVisibility(View.GONE);

        try {
            User user = userService.register(currentDraft());
            new SessionRepository(this).createSession(user);
            Intent intent = new Intent(this, PantallaPrincipalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            user.putInto(intent);
            startActivity(intent);
        } catch (InvalidField e) {
            showInputError(inputFor(e.getField()), e);
        } catch (DuplicateUsername e) {
            showInputError(usernameField, e);
        } catch (DuplicatePhoneNumber e) {
            showInputError(phoneNumberField, e);
        } catch (AppException e) {
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
            default:
                return passwordConfirmationField;
        }
    }
}
