package com.startupidea.yipshare.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Country code spinner + local number field that together read and write an E.164 phone number. */
public final class PhoneInputUtil {

    private static final String DEFAULT_REGION = "MX";

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private final Spinner countryCodeSpinner;
    private final EditText phoneNumberField;
    private final List<String> regions;

    private PhoneInputUtil(Spinner countryCodeSpinner, EditText phoneNumberField) {
        this.countryCodeSpinner = countryCodeSpinner;
        this.phoneNumberField = phoneNumberField;
        regions = new ArrayList<>(phoneUtil.getSupportedRegions());
        regions.sort(Comparator.comparing(PhoneInputUtil::countryName, Collator.getInstance()));
    }

    public static PhoneInputUtil attach(Spinner countryCodeSpinner, EditText phoneNumberField) {
        PhoneInputUtil input = new PhoneInputUtil(countryCodeSpinner, phoneNumberField);
        input.setUpCountryCodes();
        return input;
    }

    public String getPhoneNumber() {
        String localNumber = phoneNumberField.getText().toString().trim();
        if (localNumber.isEmpty()) return "";
        return "+" + countryCodeOf(selectedRegion()) + localNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return;
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, null);
            selectRegion(phoneUtil.getRegionCodeForCountryCode(number.getCountryCode()));
            phoneNumberField.setText(phoneUtil.getNationalSignificantNumber(number));
        } catch (NumberParseException e) {
            // Too short to parse: keep only the digits the user typed after the country code.
            phoneNumberField.setText(phoneNumber.replaceFirst("^\\+" + countryCodeOf(selectedRegion()), ""));
        }
    }

    private void setUpCountryCodes() {
        List<String> labels = new ArrayList<>();
        for (String region : regions) {
            labels.add(flagOf(region) + " " + countryName(region) + " (+" + countryCodeOf(region) + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                countryCodeSpinner.getContext(), android.R.layout.simple_spinner_item, labels) {
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

    private String selectedRegion() {
        return regions.get(countryCodeSpinner.getSelectedItemPosition());
    }

    private void selectRegion(String region) {
        int position = regions.indexOf(region);
        if (position >= 0) countryCodeSpinner.setSelection(position);
    }

    private int countryCodeOf(String region) {
        return phoneUtil.getCountryCodeForRegion(region);
    }

    private static String countryName(String region) {
        return new Locale("", region).getDisplayCountry();
    }

    private static String flagOf(String region) {
        int base = 0x1F1E6 - 'A';
        return new String(Character.toChars(base + region.charAt(0)))
                + new String(Character.toChars(base + region.charAt(1)));
    }
}
