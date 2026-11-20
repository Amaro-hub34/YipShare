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

    public interface OnReportClick {
        void onReportClick(Report report);
    }

    private final List<Report> reports;
    private final OnReportClick onReportClick;

    public ReportAdapter(List<Report> reports, OnReportClick onReportClick) {
        this.reports = reports;
        this.onReportClick = onReportClick;
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
        holder.statusLabel.setText(report.state().getMessageRes());
        holder.statusLabel.setBackgroundResource(report.state().getBadgeRes());
        holder.textLabel.setText(report.text());
        holder.itemView.setOnClickListener(onReportClick == null ? null : v -> onReportClick.onReportClick(report));
        holder.itemView.setClickable(onReportClick != null);
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
