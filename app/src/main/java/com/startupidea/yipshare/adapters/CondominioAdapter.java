package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;

import java.util.List;

public class CondominioAdapter extends RecyclerView.Adapter<CondominioAdapter.CondominioViewHolder> {

    public interface OnCondominioClick {
        void onClick(int position);
    }

    private static final long SLIDE_DURATION_MS = 250;
    private static final long CHECK_DURATION_MS = 300;

    private final List<String> names;
    private final View highlight;
    private final OnCondominioClick listener;
    private RecyclerView recyclerView;
    private int selected;

    public CondominioAdapter(List<String> names, int selected, View highlight, OnCondominioClick listener) {
        this.names = names;
        this.selected = selected;
        this.highlight = highlight;
        this.listener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        // The highlight lives outside the list, so it has to follow the scroll by hand.
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                highlight.setTranslationY(highlight.getTranslationY() - dy);
            }
        });
    }

    @NonNull
    @Override
    public CondominioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_condominio, parent, false);
        return new CondominioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CondominioViewHolder holder, int position) {
        boolean active = position == selected;
        holder.nameLabel.setText(names.get(position));
        holder.detailLabel.setText(R.string.residente);
        holder.checkImage.animate().cancel();
        holder.checkImage.setScaleX(1f);
        holder.checkImage.setScaleY(1f);
        holder.checkImage.setAlpha(1f);
        holder.checkImage.setVisibility(active ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(active);
        if (active) holder.itemView.post(() -> moveHighlight(holder.itemView, false));

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (pos != selected) select(holder, pos);
            listener.onClick(pos);
        });
    }

    private void select(CondominioViewHolder newHolder, int pos) {
        RecyclerView.ViewHolder previousHolder = recyclerView.findViewHolderForAdapterPosition(selected);
        if (previousHolder instanceof CondominioViewHolder) {
            View checkImage = ((CondominioViewHolder) previousHolder).checkImage;
            previousHolder.itemView.setActivated(false);
            checkImage.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(CHECK_DURATION_MS / 2)
                    .withEndAction(() -> checkImage.setVisibility(View.GONE));
        }
        selected = pos;

        newHolder.itemView.setActivated(true);
        moveHighlight(newHolder.itemView, true);
        newHolder.checkImage.setVisibility(View.VISIBLE);
        newHolder.checkImage.setScaleX(0f);
        newHolder.checkImage.setScaleY(0f);
        newHolder.checkImage.setAlpha(0f);
        newHolder.checkImage.animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setStartDelay(SLIDE_DURATION_MS / 2).setDuration(CHECK_DURATION_MS)
                .setInterpolator(new OvershootInterpolator(3f));
    }

    private void moveHighlight(View item, boolean animated) {
        if (highlight.getLayoutParams().height != item.getHeight()) {
            highlight.getLayoutParams().height = item.getHeight();
            highlight.requestLayout();
        }
        float target = recyclerView.getTop() + item.getTop();
        if (animated && highlight.getVisibility() == View.VISIBLE) {
            highlight.animate().translationY(target).setDuration(SLIDE_DURATION_MS);
        } else {
            highlight.animate().cancel();
            highlight.setTranslationY(target);
            highlight.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    static class CondominioViewHolder extends RecyclerView.ViewHolder {
        TextView nameLabel, detailLabel;
        View checkImage;

        CondominioViewHolder(@NonNull View itemView) {
            super(itemView);
            nameLabel = itemView.findViewById(R.id.tv_nombre_condominio);
            detailLabel = itemView.findViewById(R.id.tv_detalle_condominio);
            checkImage = itemView.findViewById(R.id.iv_seleccionado);
        }
    }
}
