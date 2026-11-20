package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.loan.LoanRequest;

import java.util.List;
import java.util.Locale;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder> {

    public interface OnLoanRequestAction {
        void onAccept(LoanRequest request);

        void onReject(LoanRequest request);

        void onRequesterClick(LoanRequest request);
    }

    private final List<LoanRequest> requests;
    private final OnLoanRequestAction listener;

    public NotificacionAdapter(List<LoanRequest> requests, OnLoanRequestAction listener) {
        this.requests = requests;
        this.listener = listener;
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
        LoanRequest request = requests.get(position);
        holder.itemNameLabel.setText(request.itemName());
        holder.requesterLabel.setText(request.requesterUsername());
        holder.ratingBar.setRating(request.getRequesterRating());
        holder.ratingLabel.setText(String.format(Locale.getDefault(), "%.1f", request.getRequesterRating()));
        holder.requestTextLabel.setText(request.requestText());

        holder.requesterLabel.setOnClickListener(v -> listener.onRequesterClick(request));
        holder.ratingRow.setOnClickListener(v -> listener.onRequesterClick(request));
        holder.acceptButton.setOnClickListener(v -> listener.onAccept(request));
        holder.rejectButton.setOnClickListener(v -> listener.onReject(request));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameLabel, requesterLabel, ratingLabel, requestTextLabel;
        RatingBar ratingBar;
        View ratingRow, acceptButton, rejectButton;

        NotificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameLabel = itemView.findViewById(R.id.tv_nombre_articulo);
            requesterLabel = itemView.findViewById(R.id.tv_solicitante);
            ratingLabel = itemView.findViewById(R.id.tv_calificacion_solicitante);
            requestTextLabel = itemView.findViewById(R.id.tv_direccion_solicitante);
            ratingBar = itemView.findViewById(R.id.rb_calificacion_solicitante);
            ratingRow = itemView.findViewById(R.id.layout_calificacion_solicitante);
            acceptButton = itemView.findViewById(R.id.btn_aceptar_solicitud);
            rejectButton = itemView.findViewById(R.id.btn_denegar_solicitud);
        }
    }
}
