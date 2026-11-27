package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.condominium.Unit;

import java.util.function.Consumer;

public class UnitAdapter extends ListAdapter<Unit, UnitAdapter.UnitViewHolder> {

    private final Consumer<Unit> onUnitClick;

    public UnitAdapter(Consumer<Unit> onUnitClick) {
        super(new RecordDiff<>(Unit::id));
        this.onUnitClick = onUnitClick;
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new UnitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        Unit unit = getItem(position);
        holder.nameLabel.setText(unit.name());
        holder.itemView.setOnClickListener(v -> onUnitClick.accept(unit));
    }

    static class UnitViewHolder extends RecyclerView.ViewHolder {
        TextView nameLabel;

        UnitViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.findViewById(R.id.tv_estado_reporte).setVisibility(View.GONE);
            nameLabel = itemView.findViewById(R.id.tv_texto_reporte);
        }
    }
}
