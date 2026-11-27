package com.startupidea.yipshare.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public final class ImagePreviewUtil {

    private static final String TYPE_PNG = "image/png";
    private static final String TYPE_JPEG = "image/jpeg";

    private ImagePreviewUtil() {
    }

    public static void loadInto(ImageView imageView, String imageUri, int maxSizePx) {
        imageView.setTag(imageUri);
        imageView.setImageBitmap(null);
        if (imageUri == null) return;

        Context context = imageView.getContext().getApplicationContext();
        TaskUtil.runInBackground(() -> {
            Bitmap image = tryLoad(context, Uri.parse(imageUri), maxSizePx);
            imageView.post(() -> {
                if (imageUri.equals(imageView.getTag())) imageView.setImageBitmap(image);
            });
        });
    }

    public static Bitmap tryLoad(Context context, Uri uri, int maxSizePx) {
        ContentResolver resolver = context.getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();

        try {
            options.inJustDecodeBounds = true;
            decode(resolver, uri, options);
            if (!TYPE_PNG.equals(options.outMimeType) && !TYPE_JPEG.equals(options.outMimeType)) {
                return null;
            }

            int rotation = TYPE_JPEG.equals(options.outMimeType) ? readRotation(resolver, uri) : 0;
            options.inSampleSize = sampleSizeFor(options.outWidth, options.outHeight, maxSizePx);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = decode(resolver, uri, options);
            return bitmap == null ? null : rotate(bitmap, rotation);
        } catch (IOException | RuntimeException | OutOfMemoryError e) {
            return null;
        }
    }

    private static Bitmap decode(ContentResolver resolver, Uri uri, BitmapFactory.Options options)
            throws IOException {
        try (InputStream stream = resolver.openInputStream(uri)) {
            return BitmapFactory.decodeStream(stream, null, options);
        }
    }

    private static int readRotation(ContentResolver resolver, Uri uri) throws IOException {
        try (InputStream stream = resolver.openInputStream(uri)) {
            int orientation = new ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            return switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 -> 90;
                case ExifInterface.ORIENTATION_ROTATE_180 -> 180;
                case ExifInterface.ORIENTATION_ROTATE_270 -> 270;
                default -> 0;
            };
        }
    }

    private static Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees == 0) return bitmap;

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static int sampleSizeFor(int width, int height, int maxSizePx) {
        int longestSide = Math.max(width, height);
        int sampleSize = 1;
        while (longestSide / sampleSize > maxSizePx) sampleSize *= 2;
        return sampleSize;
    }
}
