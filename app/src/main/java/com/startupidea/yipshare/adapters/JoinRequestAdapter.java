package com.startupidea.yipshare.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.condominium.JoinRequest;
import com.startupidea.yipshare.utils.PhoneUtil;

import java.util.List;
import java.util.Locale;

public class JoinRequestAdapter extends RecyclerView.Adapter<JoinRequestAdapter.JoinRequestViewHolder> {

    public interface OnJoinRequestAction {
        void onAccept(JoinRequest request);

        void onReject(JoinRequest request);

        void onRequesterClick(JoinRequest request);
    }

    private final List<JoinRequest> requests;
    private final OnJoinRequestAction listener;

    public JoinRequestAdapter(List<JoinRequest> requests, OnJoinRequestAction listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JoinRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud_union, parent, false);
        return new JoinRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JoinRequestViewHolder holder, int position) {
        JoinRequest request = requests.get(position);
        Context context = holder.itemView.getContext();
        holder.messageLabel.setText(context.getString(R.string.notif_quiere_unirse, request.getCondominiumName()));
        holder.requesterLabel.setText(request.getUsername());
        holder.fullNameLabel.setText(request.getFullName());
        PhoneUtil.attach(holder.phoneLabel, request.getPhoneNumber());
        holder.ratingBar.setRating(request.getRating());
        holder.ratingLabel.setText(String.format(Locale.getDefault(), "%.1f", request.getRating()));
        holder.unitLabel.setText(context.getString(R.string.notif_unidad, request.getUnit()));

        holder.requesterArea.setOnClickListener(v -> listener.onRequesterClick(request));
        holder.acceptButton.setOnClickListener(v -> listener.onAccept(request));
        holder.rejectButton.setOnClickListener(v -> listener.onReject(request));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class JoinRequestViewHolder extends RecyclerView.ViewHolder {
        TextView messageLabel, requesterLabel, fullNameLabel, phoneLabel, ratingLabel, unitLabel;
        RatingBar ratingBar;
        View requesterArea, acceptButton, rejectButton;

        JoinRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            messageLabel = itemView.findViewById(R.id.tv_mensaje_union);
            requesterLabel = itemView.findViewById(R.id.tv_solicitante_union);
            fullNameLabel = itemView.findViewById(R.id.tv_nombre_completo_union);
            phoneLabel = itemView.findViewById(R.id.tv_telefono_union);
            ratingLabel = itemView.findViewById(R.id.tv_calificacion_union);
            unitLabel = itemView.findViewById(R.id.tv_unidad_union);
            ratingBar = itemView.findViewById(R.id.rb_solicitante_union);
            requesterArea = itemView.findViewById(R.id.layout_solicitante_union);
            acceptButton = itemView.findViewById(R.id.btn_aceptar_union);
            rejectButton = itemView.findViewById(R.id.btn_denegar_union);
        }
    }
}
