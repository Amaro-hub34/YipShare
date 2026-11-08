package com.startupidea.yipshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.CodigoPrestamoActivity;

public class PrestamoAdapter extends RecyclerView.Adapter<PrestamoAdapter.PrestamoViewHolder> {

    private final int[] items = {R.string.demo_taladro, R.string.demo_escalera, R.string.demo_bocina};
    private final String[] people = {"Carlos Ramírez", "Ana López", "Luis Torres"};
    private final int[] types = {R.string.prestaste_a, R.string.te_prestaron, R.string.prestaste_a};

    @NonNull
    @Override
    public PrestamoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prestamo, parent, false);
        return new PrestamoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrestamoViewHolder holder, int position) {
        holder.typeLabel.setText(types[position]);
        holder.itemLabel.setText(items[position]);
        holder.personLabel.setText(people[position]);

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, CodigoPrestamoActivity.class);
            intent.putExtra("item_name", context.getString(items[holder.getAdapterPosition()]));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    static class PrestamoViewHolder extends RecyclerView.ViewHolder {
        TextView typeLabel, itemLabel, personLabel;

        PrestamoViewHolder(@NonNull View itemView) {
            super(itemView);
            typeLabel = itemView.findViewById(R.id.tv_tipo_prestamo);
            itemLabel = itemView.findViewById(R.id.tv_articulo_prestamo);
            personLabel = itemView.findViewById(R.id.tv_persona_prestamo);
        }
    }
}