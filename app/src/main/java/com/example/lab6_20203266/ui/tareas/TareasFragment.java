package com.example.lab6_20203266.ui.tareas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lab6_20203266.adapters.TareaAdapter;
import com.example.lab6_20203266.data.FirebaseDataSource; // <- nuestra clase adaptada a Firestore
import com.example.lab6_20203266.databinding.FragmentTareasBinding;
import com.example.lab6_20203266.models.Tarea;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TareasFragment extends Fragment implements TareaAdapter.OnItemActions {

    private FragmentTareasBinding b;
    private FirebaseDataSource ds;
    private TareaAdapter adapter;
    private ListenerRegistration reg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentTareasBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ds = new FirebaseDataSource();
        adapter = new TareaAdapter(this);

        b.rvTareas.setLayoutManager(new LinearLayoutManager(getContext()));
        b.rvTareas.setAdapter(adapter);

        // FAB para nueva tarea
        b.fabAdd.setOnClickListener(v ->
                NuevaTareaDialog.newInstance(null).show(getParentFragmentManager(), "NuevaTarea")
        );

        // Listener en vivo (FIRESTORE)
        reg = ds.listenAll((QuerySnapshot snap, com.google.firebase.firestore.FirebaseFirestoreException e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            List<Tarea> items = new ArrayList<>();
            if (snap != null) {
                for (DocumentSnapshot d : snap.getDocuments()) {
                    Tarea t = d.toObject(Tarea.class);
                    if (t != null) items.add(t);
                }
            }
            adapter.submit(items);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reg != null) reg.remove();
        b = null;
    }

    // Acciones del adapter
    @Override
    public void onEdit(Tarea t) {
        NuevaTareaDialog.newInstance(t).show(getParentFragmentManager(), "EditarTarea");
    }

    @Override
    public void onDelete(Tarea t) {
        ConfirmarEliminarDialog.show(getParentFragmentManager(),
                () -> ds.deleteTarea(
                        t.getId(),
                        () -> Toast.makeText(getContext(), "Eliminada", Toast.LENGTH_SHORT).show(),
                        () -> Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                )
        );
    }

    @Override
    public void onToggleEstado(Tarea t) {
        ds.updateTarea(t);
    }
}
