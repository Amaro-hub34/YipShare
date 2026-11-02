package com.startupidea.yipshare.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public final class ImageFileUtil {

    private static final String IMAGES_FOLDER = "item_images";
    private static final int BUFFER_SIZE = 8192;

    private ImageFileUtil() {
    }

    public static Uri tryCopy(Context context, Uri source) {
        File folder = new File(context.getFilesDir(), IMAGES_FOLDER);
        if (!folder.isDirectory() && !folder.mkdirs()) return null;

        File target = new File(folder, UUID.randomUUID().toString());
        try (InputStream input = context.getContentResolver().openInputStream(source);
             OutputStream output = new FileOutputStream(target)) {
            if (input == null) return null;

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            return Uri.fromFile(target);
        } catch (IOException | RuntimeException e) {
            target.delete();
            return null;
        }
    }
}
