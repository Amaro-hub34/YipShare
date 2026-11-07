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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RegistroCuenta2Activity extends AppCompatActivity {

    private static final String DEFAULT_REGION = "MX";

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private final UserService userService = new UserService(new PendingUserRepository());

    private RegistrationDraft draft;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etPasswordConfirmation;
    private EditText etPhoneNumber;
    private Spinner spCountryCode;
    private List<String> regions;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrocuenta2);

        draft = RegistrationDraft.fromIntent(getIntent());

        etUsername = findViewById(R.id.usuario_IS);
        etPassword = findViewById(R.id.contrasenia);
        etPasswordConfirmation = findViewById(R.id.contrasenia_confirmacion2);
        etPhoneNumber = findViewById(R.id.telefono_frame2);
        spCountryCode = findViewById(R.id.spinner_codigo_pais);
        setUpCountryCodes();
        tvError = findViewById(R.id.tv_error);
        PasswordToggleUtil.attach(etPassword, findViewById(R.id.btn_toggle_password));
        PasswordToggleUtil.attach(etPasswordConfirmation, findViewById(R.id.btn_toggle_password_confirmation));

        if (savedInstanceState == null) {
            etUsername.setText(draft.getUsername());
            etPassword.setText(draft.getPassword());
            etPasswordConfirmation.setText(draft.getPasswordConfirmation());
            showPhoneNumber(draft.getPhoneNumber());
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
                phoneNumberWithCountryCode());
    }

    private String phoneNumberWithCountryCode() {
        String localNumber = etPhoneNumber.getText().toString().trim();
        if (localNumber.isEmpty()) return "";
        return "+" + countryCodeOf(regions.get(spCountryCode.getSelectedItemPosition())) + localNumber;
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
        spCountryCode.setAdapter(adapter);

        String deviceRegion = Locale.getDefault().getCountry();
        selectRegion(regions.contains(deviceRegion) ? deviceRegion : DEFAULT_REGION);
    }

    private void showPhoneNumber(String phoneNumber) {
        if (phoneNumber.isEmpty()) return;
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, null);
            selectRegion(phoneUtil.getRegionCodeForCountryCode(number.getCountryCode()));
            etPhoneNumber.setText(phoneUtil.getNationalSignificantNumber(number));
        } catch (NumberParseException e) {
            // Too short to parse: keep only the digits the user typed after the country code.
            etPhoneNumber.setText(phoneNumber.replaceFirst("^\\+" + countryCodeOf(regions.get(spCountryCode.getSelectedItemPosition())), ""));
        }
    }

    private void selectRegion(String region) {
        int position = regions.indexOf(region);
        if (position >= 0) spCountryCode.setSelection(position);
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
