package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;

import java.util.List;

public class CondominioAdapter extends RecyclerView.Adapter<CondominioAdapter.CondominioViewHolder> {

    public interface OnCondominioClick {
        void onClick(String nombre);
    }

    private final List<String> nombres;
    private final OnCondominioClick listener;
    private int seleccionado = 0;

    public CondominioAdapter(List<String> nombres, OnCondominioClick listener) {
        this.nombres = nombres;
        this.listener = listener;
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
        holder.nombre.setText(nombres.get(position));
        holder.detalle.setText(R.string.residente);
        holder.check.setVisibility(position == seleccionado ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(position == seleccionado);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            seleccionado = pos;
            notifyDataSetChanged();
            listener.onClick(nombres.get(pos));
        });
    }

    @Override
    public int getItemCount() {
        return nombres.size();
    }

    static class CondominioViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, detalle;
        View check;

        CondominioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.tv_nombre_condominio);
            detalle = itemView.findViewById(R.id.tv_detalle_condominio);
            check = itemView.findViewById(R.id.iv_seleccionado);
        }
    }
}