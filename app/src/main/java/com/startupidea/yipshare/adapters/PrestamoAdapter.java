package com.startupidea.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.DetalleArticuloActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.utils.PhoneUtil;

import java.util.List;

public class PrestamoAdapter extends RecyclerView.Adapter<PrestamoAdapter.PrestamoViewHolder> {

    private final List<Loan> loans;

    public PrestamoAdapter(List<Loan> loans) {
        this.loans = loans;
    }

    @NonNull
    @Override
    public PrestamoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prestamo, parent, false);
        return new PrestamoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrestamoViewHolder holder, int position) {
        Loan loan = loans.get(position);
        holder.typeLabel.setText(loan.isLentByMe() ? R.string.prestaste_a : R.string.te_prestaron);
        holder.itemLabel.setText(loan.getItemName());
        holder.personLabel.setText(loan.getOtherUsername());
        holder.statusLabel.setText(loan.getStatus().getMessageRes());
        holder.statusLabel.setBackgroundResource(loan.getStatus().getBadgeRes());
        PhoneUtil.attach(holder.phoneLabel, loan.getOtherPhoneNumber());

        holder.itemView.setOnClickListener(v -> {
            int current = holder.getBindingAdapterPosition();
            if (current == RecyclerView.NO_POSITION) return;

            Context context = v.getContext();
            Intent intent = new Intent(context, DetalleArticuloActivity.class);
            loans.get(current).putInto(intent);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    static class PrestamoViewHolder extends RecyclerView.ViewHolder {
        TextView typeLabel, itemLabel, personLabel, statusLabel, phoneLabel;

        PrestamoViewHolder(@NonNull View itemView) {
            super(itemView);
            typeLabel = itemView.findViewById(R.id.tv_tipo_prestamo);
            itemLabel = itemView.findViewById(R.id.tv_articulo_prestamo);
            personLabel = itemView.findViewById(R.id.tv_persona_prestamo);
            statusLabel = itemView.findViewById(R.id.tv_estado_prestamo);
            phoneLabel = itemView.findViewById(R.id.tv_telefono_prestamo);
        }
    }
}
