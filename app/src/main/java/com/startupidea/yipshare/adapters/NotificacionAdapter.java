package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder> {

    private final int cantidad;

    public NotificacionAdapter(int cantidad) {
        this.cantidad = cantidad;
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion, parent, false);
        return new NotificacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        holder.nombreArticulo.setText("Taladro eléctrico");
        holder.solicitante.setText("Carlos Ramírez");
        holder.ratingBar.setRating(4.5f);
        holder.calificacion.setText("4.5");
        holder.direccion.setText("Edificio B, Depto. 302");
    }

    @Override
    public int getItemCount() {
        return cantidad;
    }

    /**
     * ViewHolder for the notification items. this method is not final but is just for the teacher homework
     */
    static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        TextView nombreArticulo, solicitante, calificacion, direccion;
        RatingBar ratingBar;

        NotificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreArticulo = itemView.findViewById(R.id.tv_nombre_articulo);
            solicitante = itemView.findViewById(R.id.tv_solicitante);
            calificacion = itemView.findViewById(R.id.tv_calificacion_solicitante);
            direccion = itemView.findViewById(R.id.tv_direccion_solicitante);
            ratingBar = itemView.findViewById(R.id.rb_calificacion_solicitante);
        }
    }
}