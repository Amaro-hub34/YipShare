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

    private final int count;

    public NotificacionAdapter(int count) {
        this.count = count;
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
        holder.itemNameLabel.setText(R.string.demo_taladro);
        holder.requesterLabel.setText("Carlos Ramírez");
        holder.ratingBar.setRating(4.5f);
        holder.ratingLabel.setText("4.5");
        holder.addressLabel.setText(R.string.demo_direccion);
    }

    @Override
    public int getItemCount() {
        return count;
    }

    /**
     * ViewHolder for the notification items. this method is not final but is just for the teacher homework
     */
    static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameLabel, requesterLabel, ratingLabel, addressLabel;
        RatingBar ratingBar;

        NotificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameLabel = itemView.findViewById(R.id.tv_nombre_articulo);
            requesterLabel = itemView.findViewById(R.id.tv_solicitante);
            ratingLabel = itemView.findViewById(R.id.tv_calificacion_solicitante);
            addressLabel = itemView.findViewById(R.id.tv_direccion_solicitante);
            ratingBar = itemView.findViewById(R.id.rb_calificacion_solicitante);
        }
    }
}