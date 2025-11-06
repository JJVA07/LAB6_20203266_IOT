package com.example.lab6_20203266.adapters;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20203266.R;
import com.example.lab6_20203266.models.Tarea;

import java.text.SimpleDateFormat;
import java.util.*;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.VH> {

    public interface OnItemActions {
        void onEdit(Tarea t);
        void onDelete(Tarea t);
        void onToggleEstado(Tarea t);
    }

    private final List<Tarea> data = new ArrayList<>();
    private final OnItemActions listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public TareaAdapter(OnItemActions l) { this.listener = l; }

    public void submit(List<Tarea> nuevas) {
        data.clear(); if (nuevas != null) data.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Tarea t = data.get(pos);
        h.tvTitulo.setText(t.getTitulo());
        h.tvFecha.setText("Vence: " + sdf.format(new Date(t.getFechaLimiteMillis())));
        h.cbEstado.setOnCheckedChangeListener(null);
        h.cbEstado.setChecked(t.isEstado());
        h.cbEstado.setOnCheckedChangeListener((b, checked) -> {
            t.setEstado(checked);
            listener.onToggleEstado(t);
        });
        h.btnEditar.setOnClickListener(v -> listener.onEdit(t));
        h.btnEliminar.setOnClickListener(v -> listener.onDelete(t));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha;
        CheckBox cbEstado;
        ImageButton btnEditar, btnEliminar;
        VH(@NonNull View v) {
            super(v);
            tvTitulo = v.findViewById(R.id.tvTitulo);
            tvFecha  = v.findViewById(R.id.tvFecha);
            cbEstado = v.findViewById(R.id.cbEstado);
            btnEditar = v.findViewById(R.id.btnEditar);
            btnEliminar = v.findViewById(R.id.btnEliminar);
        }
    }
}
