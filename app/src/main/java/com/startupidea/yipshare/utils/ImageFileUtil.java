package com.startupidea.yipshare.utils;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.BadRequestException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public final class ImageFileUtil {

    private static final String IMAGES_FOLDER = "item_images";
    private static final int BUFFER_SIZE = 8192;
    private static final long PROFILE_PICTURE_MAX_BYTES = 10L * 1024 * 1024;
    private static final int PROFILE_PICTURE_SIZE_PX = 320;
    private static final int PROFILE_PICTURE_DECODE_MAX_PX = 1280;
    private static final int JPEG_QUALITY = 90;

    private ImageFileUtil() {
    }

    public static String save(Context context, String imageUri) {
        if (imageUri == null) return null;
        if (storedFile(context, Uri.parse(imageUri)) != null) return imageUri;

        Uri copy = tryCopy(context, Uri.parse(imageUri));
        if (copy == null) throw new BadRequestException("Image could not be read", R.string.error_imagen_invalida);
        return copy.toString();
    }

    public static String saveProfilePicture(Context context, String imageUri) {
        Uri source = Uri.parse(imageUri);
        if (isLargerThan(context, source, PROFILE_PICTURE_MAX_BYTES)) throw new BadRequestException("Profile picture is larger than 10 MB", R.string.error_foto_perfil_grande);
        Bitmap picture = ImagePreviewUtil.tryLoad(context, source, PROFILE_PICTURE_DECODE_MAX_PX);
        if (picture == null) throw new BadRequestException("Image could not be read", R.string.error_imagen_invalida);

        int side = Math.min(picture.getWidth(), picture.getHeight());
        Bitmap square = Bitmap.createBitmap(picture, (picture.getWidth() - side) / 2, (picture.getHeight() - side) / 2, side, side);
        Bitmap scaled = Bitmap.createScaledBitmap(square, PROFILE_PICTURE_SIZE_PX, PROFILE_PICTURE_SIZE_PX, true);
        File folder = folder(context);
        if (!folder.isDirectory() && !folder.mkdirs()) throw new BadRequestException("Image could not be read", R.string.error_imagen_invalida);
        File file = new File(folder, UUID.randomUUID() + ".jpg");
        try (OutputStream output = new FileOutputStream(file)) {
            if (!scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) throw new IOException("JPEG compression failed");
            return Uri.fromFile(file).toString();
        } catch (IOException e) {
            file.delete();
            throw new BadRequestException("Image could not be read", R.string.error_imagen_invalida);
        }
    }

    private static boolean isLargerThan(Context context, Uri uri, long maxBytes) {
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) return false;
            byte[] buffer = new byte[BUFFER_SIZE];
            long total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > maxBytes) return true;
            }
            return false;
        } catch (IOException | RuntimeException e) {
            throw new BadRequestException("Image could not be read", R.string.error_imagen_invalida);
        }
    }

    private static Uri tryCopy(Context context, Uri source) {
        File folder = folder(context);
        if (!folder.isDirectory() && !folder.mkdirs()) return null;

        File target = new File(folder, UUID.randomUUID().toString());
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
