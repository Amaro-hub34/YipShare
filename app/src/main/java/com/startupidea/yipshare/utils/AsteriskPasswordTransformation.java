package com.startupidea.yipshare.utils;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

import androidx.annotation.NonNull;

/** Masks a password with '*' instead of Android's dots. */
public class AsteriskPasswordTransformation extends PasswordTransformationMethod {

    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new Asterisks(source);
    }

    private static class Asterisks implements CharSequence {

        private final CharSequence source;

        Asterisks(CharSequence source) {
            this.source = source;
        }

        @Override
        public int length() {
            return source.length();
        }

        @Override
        public char charAt(int index) {
            return '*';
        }

        @Override
        @NonNull
        public CharSequence subSequence(int start, int end) {
            return new Asterisks(source.subSequence(start, end));
        }

        @NonNull
        @Override
        public String toString() {
            return new String(new char[length()]).replace('\0', '*');
        }
    }
}
