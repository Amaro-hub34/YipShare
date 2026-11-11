package com.startupidea.yipshare.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import java.io.File;

public final class PhoneUtil {

    private static final String WHATSAPP_PACKAGE = "com.whatsapp";

    private PhoneUtil() {
    }

    public static void attach(TextView label, String phoneNumber) {
        if (!show(label, phoneNumber)) return;
        Context context = label.getContext();
        label.setOnClickListener(v -> context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber))));
    }

    public static void attachWhatsApp(TextView label, String phoneNumber, String message, String imageUri) {
        if (!show(label, phoneNumber)) return;
        label.setOnClickListener(v -> openWhatsApp(label.getContext(), phoneNumber, message, imageUri));
    }

    private static boolean show(TextView label, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            label.setVisibility(View.GONE);
            return false;
        }
        label.setVisibility(View.VISIBLE);
        label.setText(label.getContext().getString(com.startupidea.yipshare.R.string.telefono_contacto, phoneNumber));
        return true;
    }

    private static void openWhatsApp(Context context, String phoneNumber, String message, String imageUri) {
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        Uri image = toContentUri(context, imageUri);
        if (image != null) {
            Intent intent = new Intent(Intent.ACTION_SEND)
                    .setType("image/*")
                    .setPackage(WHATSAPP_PACKAGE)
                    .putExtra(Intent.EXTRA_STREAM, image)
                    .putExtra(Intent.EXTRA_TEXT, message)
                    .putExtra("jid", digits + "@s.whatsapp.net")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(intent);
                return;
            } catch (ActivityNotFoundException ignored) {
            }
        }
        Uri chat = Uri.parse("https://wa.me/" + digits + "?text=" + Uri.encode(message));
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, chat));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
        }
    }

    private static Uri toContentUri(Context context, String imageUri) {
        if (imageUri == null || imageUri.isEmpty()) return null;
        Uri uri = Uri.parse(imageUri);
        if (!"file".equals(uri.getScheme())) return null;
        try {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", new File(uri.getPath()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
