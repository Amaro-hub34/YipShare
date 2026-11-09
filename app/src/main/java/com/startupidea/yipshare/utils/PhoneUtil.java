package com.startupidea.yipshare.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import com.startupidea.yipshare.R;

public final class PhoneUtil {

    private PhoneUtil() {
    }

    public static void attach(TextView label, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            label.setVisibility(View.GONE);
            return;
        }
        Context context = label.getContext();
        label.setVisibility(View.VISIBLE);
        label.setText(context.getString(com.startupidea.yipshare.R.string.telefono_contacto, phoneNumber));
        // ACTION_DIAL only opens the dialer with the number, so it needs no call permission.
        label.setOnClickListener(v -> context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber))));
    }
}
