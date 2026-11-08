package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.feedback.Report;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<Report> reports;

    public ReportAdapter(List<Report> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.statusLabel.setText(report.isResolved() ? R.string.reporte_resuelto : R.string.reporte_abierto);
        holder.statusLabel.setBackgroundResource(
                report.isResolved() ? R.drawable.bg_badge_disponible : R.drawable.bg_badge_uso);
        holder.textLabel.setText(report.getText());
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView statusLabel, textLabel;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            statusLabel = itemView.findViewById(R.id.tv_estado_reporte);
            textLabel = itemView.findViewById(R.id.tv_texto_reporte);
        }
    }
}
