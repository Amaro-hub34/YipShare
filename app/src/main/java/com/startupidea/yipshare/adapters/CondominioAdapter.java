package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;

import java.util.List;

public class CondominioAdapter extends RecyclerView.Adapter<CondominioAdapter.CondominioViewHolder> {

    public interface OnCondominioClick {
        void onClick(int posicion);
    }

    private static final long DURACION_DESLIZAR_MS = 250;
    private static final long DURACION_CHECK_MS = 300;

    private final List<String> nombres;
    private final View resaltado;
    private final OnCondominioClick listener;
    private RecyclerView recyclerView;
    private int seleccionado;

    public CondominioAdapter(List<String> nombres, int seleccionado, View resaltado, OnCondominioClick listener) {
        this.nombres = nombres;
        this.seleccionado = seleccionado;
        this.resaltado = resaltado;
        this.listener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        // The highlight lives outside the list, so it has to follow the scroll by hand.
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                resaltado.setTranslationY(resaltado.getTranslationY() - dy);
            }
        });
    }

    @NonNull
    @Override
    public CondominioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_condominio, parent, false);
        return new CondominioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CondominioViewHolder holder, int position) {
        boolean activo = position == seleccionado;
        holder.nombre.setText(nombres.get(position));
        holder.detalle.setText(R.string.residente);
        holder.check.animate().cancel();
        holder.check.setScaleX(1f);
        holder.check.setScaleY(1f);
        holder.check.setAlpha(1f);
        holder.check.setVisibility(activo ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(activo);
        if (activo) holder.itemView.post(() -> moverResaltado(holder.itemView, false));

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (pos != seleccionado) seleccionar(holder, pos);
            listener.onClick(pos);
        });
    }

    private void seleccionar(CondominioViewHolder nuevo, int pos) {
        RecyclerView.ViewHolder anterior = recyclerView.findViewHolderForAdapterPosition(seleccionado);
        if (anterior instanceof CondominioViewHolder) {
            View check = ((CondominioViewHolder) anterior).check;
            anterior.itemView.setActivated(false);
            check.animate().scaleX(0f).scaleY(0f).alpha(0f).setDuration(DURACION_CHECK_MS / 2)
                    .withEndAction(() -> check.setVisibility(View.GONE));
        }
        seleccionado = pos;

        nuevo.itemView.setActivated(true);
        moverResaltado(nuevo.itemView, true);
        nuevo.check.setVisibility(View.VISIBLE);
        nuevo.check.setScaleX(0f);
        nuevo.check.setScaleY(0f);
        nuevo.check.setAlpha(0f);
        nuevo.check.animate().scaleX(1f).scaleY(1f).alpha(1f)
                .setStartDelay(DURACION_DESLIZAR_MS / 2).setDuration(DURACION_CHECK_MS)
                .setInterpolator(new OvershootInterpolator(3f));
    }

    private void moverResaltado(View item, boolean animar) {
        if (resaltado.getLayoutParams().height != item.getHeight()) {
            resaltado.getLayoutParams().height = item.getHeight();
            resaltado.requestLayout();
        }
        float destino = recyclerView.getTop() + item.getTop();
        if (animar && resaltado.getVisibility() == View.VISIBLE) {
            resaltado.animate().translationY(destino).setDuration(DURACION_DESLIZAR_MS);
        } else {
            resaltado.animate().cancel();
            resaltado.setTranslationY(destino);
            resaltado.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return nombres.size();
    }

    static class CondominioViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, detalle;
        View check;

        CondominioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.tv_nombre_condominio);
            detalle = itemView.findViewById(R.id.tv_detalle_condominio);
            check = itemView.findViewById(R.id.iv_seleccionado);
        }
    }
}
