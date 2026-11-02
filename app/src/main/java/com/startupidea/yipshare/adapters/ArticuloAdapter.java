package com.startupidea.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.DetalleArticuloActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.utils.ImagePreviewUtil;

import java.util.List;

public class ArticuloAdapter extends RecyclerView.Adapter<ArticuloAdapter.ArticuloViewHolder> {

    private static final int CARD_IMAGE_MAX_SIZE_PX = 400;

    private final List<Item> items;
    private final int exampleCount;

    public ArticuloAdapter(List<Item> items, int exampleCount) {
        this.items = items;
        this.exampleCount = exampleCount;
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
        if (position < items.size()) {
            holder.bind(items.get(position));
        } else {
            holder.bindExample();
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            context.startActivity(new Intent(context, DetalleArticuloActivity.class));
        });
    }

    @Override
    public int getItemCount() {
        return items.size() + exampleCount;
    }

    static class ArticuloViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivImage;
        private final TextView tvName;
        private final View tvLender;
        private final View rbRating;

        ArticuloViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_imagen_articulo);
            tvName = itemView.findViewById(R.id.tv_nombre_articulo);
            tvLender = itemView.findViewById(R.id.tv_prestamista);
            rbRating = itemView.findViewById(R.id.tv_calificacion);
        }

        void bind(Item item) {
            Uri imageUri = Uri.parse(item.getPayload().getImageUri());
            Bitmap image = ImagePreviewUtil.tryLoad(itemView.getContext(), imageUri, CARD_IMAGE_MAX_SIZE_PX);

            ivImage.setImageBitmap(image);
            tvName.setText(item.getPayload().getName());
            tvLender.setVisibility(View.GONE);
            rbRating.setVisibility(View.GONE);
        }

        void bindExample() {
            ivImage.setImageDrawable(null);
            tvName.setText(R.string.item_objeto);
            tvLender.setVisibility(View.VISIBLE);
            rbRating.setVisibility(View.VISIBLE);
        }
    }
}
