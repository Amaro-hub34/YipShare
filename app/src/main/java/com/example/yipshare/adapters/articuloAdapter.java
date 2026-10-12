package com.example.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yipshare.R;
import com.example.yipshare.detalleArticuloActivity;

public class articuloAdapter extends RecyclerView.Adapter<articuloAdapter.ArticuloViewHolder> {

    private final int cantidad;

    public articuloAdapter(int cantidad) {
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
            context.startActivity(new Intent(context, detalleArticuloActivity.class));
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