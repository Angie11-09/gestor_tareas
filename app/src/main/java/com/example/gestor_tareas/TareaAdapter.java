package com.example.gestor_tareas;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private List<Tarea> listaTareas;

    public TareaAdapter(List<Tarea> listaTareas) {
        this.listaTareas = listaTareas;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int posicion) {
        Tarea tareaActual = listaTareas.get(posicion);

        holder.tvTitulo.setText(tareaActual.getTitulo());
        holder.tvDescripcion.setText(tareaActual.getDescripcion());
        holder.tvEstado.setText(tareaActual.getEstado());
        holder.tvId.setText("ID: " + tareaActual.getDocumentId());

        if (tareaActual.getEstado().equals("Completada")) {
            holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvEstado.setTextColor(Color.parseColor("#F44336"));
        }
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    public static class TareaViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvTitulo, tvDescripcion, tvEstado;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvItemId);
            tvTitulo = itemView.findViewById(R.id.tvItemTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvItemDescripcion);
            tvEstado = itemView.findViewById(R.id.tvItemEstado);
        }
    }
}