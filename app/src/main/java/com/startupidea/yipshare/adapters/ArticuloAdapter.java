package com.startupidea.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.DetalleArticuloActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.utils.ImagePreviewUtil;

public class ArticuloAdapter extends ListAdapter<Item, ArticuloAdapter.ArticuloViewHolder> {

    private static final int CARD_IMAGE_MAX_SIZE_PX = 400;

    public ArticuloAdapter() {
        super(new RecordDiff<>(Item::id));
    }

    @NonNull
    @Override
    public ArticuloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_articulo, parent, false);
        return new ArticuloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticuloViewHolder holder, int position) {
        holder.bind(getItem(position));

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DetalleArticuloActivity.class);
            int current = holder.getBindingAdapterPosition();
            if (current == RecyclerView.NO_POSITION) return;
            getItem(current).putInto(intent);
            context.startActivity(intent);
        });
    }

    static class ArticuloViewHolder extends RecyclerView.ViewHolder {

        private final ImageView itemImage;
        private final TextView nameLabel;
        private final View lenderLabel;
        private final View ratingLabel;

        ArticuloViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.iv_imagen_articulo);
            nameLabel = itemView.findViewById(R.id.tv_nombre_articulo);
            lenderLabel = itemView.findViewById(R.id.tv_prestamista);
            ratingLabel = itemView.findViewById(R.id.tv_calificacion);
        }

        void bind(Item item) {
            nameLabel.setText(item.payload().getName());
            lenderLabel.setVisibility(View.GONE);
            ratingLabel.setVisibility(View.GONE);
            ImagePreviewUtil.loadInto(itemImage, item.payload().getImageUri(), CARD_IMAGE_MAX_SIZE_PX);
        }
    }
}
