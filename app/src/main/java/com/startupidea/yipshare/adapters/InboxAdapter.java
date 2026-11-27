package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.notification.Notification;

public class InboxAdapter extends ListAdapter<Notification, InboxAdapter.InboxViewHolder> {

    public interface OnNotificationAction {
        void onRead(Notification notification);

        void onDelete(Notification notification);
    }

    private final OnNotificationAction listener;

    public InboxAdapter(OnNotificationAction listener) {
        super(new RecordDiff<>(Notification::id));
        this.listener = listener;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        Notification notification = getItem(position);
        holder.statusLabel.setText(notification.read() ? R.string.aviso_leido : R.string.aviso_nuevo);
        holder.statusLabel.setBackgroundResource(
                notification.read() ? R.drawable.bg_badge_disponible : R.drawable.bg_badge_uso);
        holder.textLabel.setText(holder.itemView.getContext()
                .getString(R.string.aviso_texto, notification.header(), notification.body()));
        holder.itemView.setOnClickListener(v -> listener.onRead(notification));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(notification);
            return true;
        });
    }

    static class InboxViewHolder extends RecyclerView.ViewHolder {
        TextView statusLabel, textLabel;

        InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            statusLabel = itemView.findViewById(R.id.tv_estado_reporte);
            textLabel = itemView.findViewById(R.id.tv_texto_reporte);
        }
    }
}
