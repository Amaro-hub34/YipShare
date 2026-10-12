package com.example.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yipshare.R;

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