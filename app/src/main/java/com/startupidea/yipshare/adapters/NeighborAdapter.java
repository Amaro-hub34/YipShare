package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.condominium.Neighbor;
import com.startupidea.yipshare.utils.PhoneUtil;

import java.util.List;
import java.util.Locale;

public class NeighborAdapter extends RecyclerView.Adapter<NeighborAdapter.NeighborViewHolder> {

    public interface OnNeighborAction {
        void onNeighborClick(Neighbor neighbor);

        void onLoanAccessChange(Neighbor neighbor, boolean allowed);
    }

    private final List<Neighbor> neighbors;
    private final OnNeighborAction listener;

    public NeighborAdapter(List<Neighbor> neighbors, OnNeighborAction listener) {
        this.neighbors = neighbors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NeighborViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vecino, parent, false);
        return new NeighborViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NeighborViewHolder holder, int position) {
        Neighbor neighbor = neighbors.get(position);
        String unit = holder.itemView.getContext().getString(R.string.unidad_vecino, neighbor.getUnit());
        if (neighbor.isAdmin()) {
            unit = unit + " · " + holder.itemView.getContext().getString(R.string.rol_admin_condominio);
        }
        holder.nameLabel.setText(neighbor.getUsername());
        holder.unitLabel.setText(unit);
        holder.ratingBar.setRating(neighbor.getRating());
        holder.ratingLabel.setText(String.format(Locale.getDefault(), "%.1f", neighbor.getRating()));
        PhoneUtil.attach(holder.phoneLabel, neighbor.getPhoneNumber());
        holder.neighborArea.setOnClickListener(v -> listener.onNeighborClick(neighbor));

        holder.loanAccess.setOnCheckedChangeListener(null);
        holder.loanAccess.setChecked(neighbor.canRequestLoans());
        holder.loanAccess.setVisibility(neighbor.isAdmin() ? View.GONE : View.VISIBLE);
        holder.loanAccess.setOnCheckedChangeListener((button, checked) -> listener.onLoanAccessChange(neighbor, checked));
    }

    @Override
    public int getItemCount() {
        return neighbors.size();
    }

    static class NeighborViewHolder extends RecyclerView.ViewHolder {
        TextView nameLabel, unitLabel, ratingLabel, phoneLabel;
        RatingBar ratingBar;
        View neighborArea;
        SwitchCompat loanAccess;

        NeighborViewHolder(@NonNull View itemView) {
            super(itemView);
            nameLabel = itemView.findViewById(R.id.tv_nombre_vecino);
            unitLabel = itemView.findViewById(R.id.tv_unidad_vecino);
            ratingLabel = itemView.findViewById(R.id.tv_calificacion_vecino);
            phoneLabel = itemView.findViewById(R.id.tv_telefono_vecino);
            ratingBar = itemView.findViewById(R.id.rb_vecino);
            neighborArea = itemView.findViewById(R.id.layout_vecino);
            loanAccess = itemView.findViewById(R.id.sw_puede_pedir);
        }
    }
}
