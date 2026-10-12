package com.example.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yipshare.R;
import com.example.yipshare.codigoPrestamoActivity;

public class prestamoAdapter extends RecyclerView.Adapter<prestamoAdapter.PrestamoViewHolder> {

    private final String[] articulos = {"Taladro eléctrico", "Escalera plegable", "Bocina portátil"};
    private final String[] personas = {"Carlos Ramírez", "Ana López", "Luis Torres"};
    private final String[] tipos = {"Prestaste a", "Te prestaron", "Prestaste a"};

    @NonNull
    @Override
    public PrestamoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prestamo, parent, false);
        return new PrestamoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrestamoViewHolder holder, int position) {
        holder.tipo.setText(tipos[position]);
        holder.articulo.setText(articulos[position]);
        holder.persona.setText(personas[position]);

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, codigoPrestamoActivity.class);
            intent.putExtra("articulo", articulos[holder.getAdapterPosition()]);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articulos.length;
    }

    static class PrestamoViewHolder extends RecyclerView.ViewHolder {
        TextView tipo, articulo, persona;

        PrestamoViewHolder(@NonNull View itemView) {
            super(itemView);
            tipo = itemView.findViewById(R.id.tv_tipo_prestamo);
            articulo = itemView.findViewById(R.id.tv_articulo_prestamo);
            persona = itemView.findViewById(R.id.tv_persona_prestamo);
        }
    }
}