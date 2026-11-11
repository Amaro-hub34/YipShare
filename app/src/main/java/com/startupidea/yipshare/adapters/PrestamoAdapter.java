package com.startupidea.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.DetalleArticuloActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.loan.LoanStatus;
import com.startupidea.yipshare.utils.ImagePreviewUtil;
import com.startupidea.yipshare.utils.PhoneUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrestamoAdapter extends RecyclerView.Adapter<PrestamoAdapter.PrestamoViewHolder> {

    // must match strftime('%d/%m/%Y') in SqliteLoanRepository
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final int CARD_IMAGE_MAX_SIZE_PX = 400;

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
        String imageUri = loan.getImageUri();
        holder.itemImage.setImageBitmap(imageUri == null
                ? null : ImagePreviewUtil.tryLoad(holder.itemView.getContext(), Uri.parse(imageUri), CARD_IMAGE_MAX_SIZE_PX));
        holder.itemLabel.setText(loan.getItemName());
        holder.personLabel.setText(loan.getOtherUsername());
        holder.statusLabel.setText(loan.getStatus().getMessageRes());
        holder.statusLabel.setBackgroundResource(loan.getStatus().getBadgeRes());
        String message = whatsAppMessage(holder.itemView.getContext(), loan);
        if (message != null) PhoneUtil.attachWhatsApp(holder.phoneLabel, loan.getOtherPhoneNumber(), message, loan.getImageUri());
        else PhoneUtil.attach(holder.phoneLabel, loan.getOtherPhoneNumber());

        holder.itemView.setOnClickListener(v -> {
            int current = holder.getBindingAdapterPosition();
            if (current == RecyclerView.NO_POSITION) return;

            Context context = v.getContext();
            Intent intent = new Intent(context, DetalleArticuloActivity.class);
            loans.get(current).putInto(intent);
            context.startActivity(intent);
        });
    }

    private static String whatsAppMessage(Context context, Loan loan) {
        if (!loan.isLentByMe() && loan.getStatus() == LoanStatus.ACCEPTED)
            return context.getString(R.string.mensaje_interes_articulo, loan.getItemName());
        if (loan.getStatus() == LoanStatus.ON_LOAN && isToday(loan.getEndDate()))
            return context.getString(R.string.mensaje_dia_entrega);
        return null;
    }

    private static boolean isToday(String date) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.ROOT).format(new Date()).equals(date);
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    static class PrestamoViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView typeLabel, itemLabel, personLabel, statusLabel, phoneLabel;

        PrestamoViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.iv_imagen_prestamo);
            typeLabel = itemView.findViewById(R.id.tv_tipo_prestamo);
            itemLabel = itemView.findViewById(R.id.tv_articulo_prestamo);
            personLabel = itemView.findViewById(R.id.tv_persona_prestamo);
            statusLabel = itemView.findViewById(R.id.tv_estado_prestamo);
            phoneLabel = itemView.findViewById(R.id.tv_telefono_prestamo);
        }
    }
}
