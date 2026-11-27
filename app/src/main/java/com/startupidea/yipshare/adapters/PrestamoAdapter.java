package com.startupidea.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.DetalleArticuloActivity;
import com.startupidea.yipshare.R;
import com.startupidea.yipshare.loan.Loan;
import com.startupidea.yipshare.loan.LoanStatus;
import com.startupidea.yipshare.utils.ImagePreviewUtil;
import com.startupidea.yipshare.utils.PhoneUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrestamoAdapter extends ListAdapter<Loan, PrestamoAdapter.PrestamoViewHolder> {

    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final int CARD_IMAGE_MAX_SIZE_PX = 400;

    public PrestamoAdapter() {
        super(new RecordDiff<>(Loan::id));
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
        Loan loan = getItem(position);
        holder.typeLabel.setText(loan.lentByMe() ? R.string.prestaste_a : R.string.te_prestaron);
        ImagePreviewUtil.loadInto(holder.itemImage, loan.imageUri(), CARD_IMAGE_MAX_SIZE_PX);
        holder.itemLabel.setText(loan.itemName());
        holder.personLabel.setText(loan.otherUsername());
        holder.statusLabel.setText(loan.status().getMessageRes());
        holder.statusLabel.setBackgroundResource(loan.status().getBadgeRes());
        String message = whatsAppMessage(holder.itemView.getContext(), loan);
        if (message != null) PhoneUtil.attachWhatsApp(holder.phoneLabel, loan.otherPhoneNumber(), message, loan.imageUri());
        else PhoneUtil.attach(holder.phoneLabel, loan.otherPhoneNumber());

        holder.itemView.setOnClickListener(v -> {
            int current = holder.getBindingAdapterPosition();
            if (current == RecyclerView.NO_POSITION) return;

            Context context = v.getContext();
            Intent intent = new Intent(context, DetalleArticuloActivity.class);
            getItem(current).putInto(intent);
            context.startActivity(intent);
        });
    }

    private static String whatsAppMessage(Context context, Loan loan) {
        if (!loan.lentByMe() && loan.status() == LoanStatus.ACCEPTED)
            return context.getString(R.string.mensaje_interes_articulo, loan.itemName());
        if (loan.status() == LoanStatus.ON_LOAN && isToday(loan.endDate()))
            return context.getString(R.string.mensaje_dia_entrega);
        return null;
    }

    private static boolean isToday(String date) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.ROOT).format(new Date()).equals(date);
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
