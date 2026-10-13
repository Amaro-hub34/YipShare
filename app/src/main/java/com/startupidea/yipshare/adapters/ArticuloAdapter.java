package com.startupidea.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.DetalleArticuloActivity;

public class ArticuloAdapter extends RecyclerView.Adapter<ArticuloAdapter.ArticuloViewHolder> {

    private final int cantidad;

    public ArticuloAdapter(int cantidad) {
        this.cantidad = cantidad;
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
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            context.startActivity(new Intent(context, DetalleArticuloActivity.class));
        });
    }

    @Override
    public int getItemCount() {
        return cantidad;
    }

    static class ArticuloViewHolder extends RecyclerView.ViewHolder {
        ArticuloViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}