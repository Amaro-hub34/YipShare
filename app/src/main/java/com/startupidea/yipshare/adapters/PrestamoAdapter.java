package com.startupidea.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.CodigoPrestamoActivity;

public class PrestamoAdapter extends RecyclerView.Adapter<PrestamoAdapter.PrestamoViewHolder> {

    private final int[] articulos = {R.string.demo_taladro, R.string.demo_escalera, R.string.demo_bocina};
    private final String[] personas = {"Carlos Ramírez", "Ana López", "Luis Torres"};
    private final int[] tipos = {R.string.prestaste_a, R.string.te_prestaron, R.string.prestaste_a};

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
            Intent intent = new Intent(context, CodigoPrestamoActivity.class);
            intent.putExtra("articulo", context.getString(articulos[holder.getAdapterPosition()]));
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