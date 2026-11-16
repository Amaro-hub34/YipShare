package com.startupidea.yipshare.utils;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.startupidea.yipshare.api.ApiClient;
import com.startupidea.yipshare.error.InvalidImage;
import com.startupidea.yipshare.error.ServerUnavailable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public final class ImageFileUtil {

    private static final String IMAGES_FOLDER = "item_images";
    private static final String TYPE_PNG = "image/png";
    private static final String TYPE_JPEG = "image/jpeg";
    private static final int BUFFER_SIZE = 8192;

    private ImageFileUtil() {
    }

    public static String upload(Context context, ApiClient apiClient, String imageUri) {
        if (imageUri == null) return null;
        File stored = storedFile(context, Uri.parse(imageUri));
        if (stored != null) return "/uploads/" + stored.getName();

        Uri copy = tryCopy(context, Uri.parse(imageUri));
        if (copy == null) throw new InvalidImage();
        File file = new File(copy.getPath());
        String path;
        try (InputStream input = new FileInputStream(file)) {
            path = apiClient.upload(input, file.getName().endsWith(".png") ? TYPE_PNG : TYPE_JPEG);
        } catch (IOException e) {
            throw new InvalidImage();
        } finally {
            file.delete();
        }
        copyTo(context, Uri.parse(imageUri), new File(folder(context), nameOf(path)));
        return path;
    }

    public static String toLocalUri(Context context, ApiClient apiClient, String path) {
        if (path == null) return null;
        File file = new File(folder(context), nameOf(path));
        if (!file.exists()) {
            try {
                if (!folder(context).isDirectory() && !folder(context).mkdirs()) return null;
                apiClient.download(path, file);
            } catch (ServerUnavailable e) {
                return null;
            }
        }
        return Uri.fromFile(file).toString();
    }

    private static Uri tryCopy(Context context, Uri source) {
        File folder = folder(context);
        if (!folder.isDirectory() && !folder.mkdirs()) return null;

        File target = new File(context.getCacheDir(), UUID.randomUUID().toString());
        if (!copyTo(context, source, target)) return null;
        return Uri.fromFile(withExtension(target));
    }

    private static boolean copyTo(Context context, Uri source, File target) {
        try (InputStream input = context.getContentResolver().openInputStream(source);
             OutputStream output = new FileOutputStream(target)) {
            if (input == null) return false;

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            return true;
        } catch (IOException | RuntimeException e) {
            target.delete();
            return false;
        }
    }

    private static File storedFile(Context context, Uri uri) {
        if (!"file".equals(uri.getScheme()) || uri.getPath() == null) return null;
        File file = new File(uri.getPath());
        return folder(context).equals(file.getParentFile()) && file.exists() ? file : null;
    }

    private static File folder(Context context) {
        return new File(context.getFilesDir(), IMAGES_FOLDER);
    }

    private static String nameOf(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static File withExtension(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(options.outMimeType);
        if (extension == null) return file;
        File named = new File(file.getPath() + "." + extension);
        return file.renameTo(named) ? named : file;
    }
}
