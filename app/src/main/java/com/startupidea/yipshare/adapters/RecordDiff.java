package com.startupidea.yipshare.adapters;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;
import java.util.function.Function;

final class RecordDiff<T> extends DiffUtil.ItemCallback<T> {

    private final Function<T, Object> key;

    RecordDiff(Function<T, Object> key) {
        this.key = key;
    }

    @Override
    public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return Objects.equals(key.apply(oldItem), key.apply(newItem));
    }

    @SuppressLint("DiffUtilEquals")
    @Override
    public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return oldItem.equals(newItem);
    }
}
