package com.example.lab6_20203266.ui.resumen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.lab6_20203266.data.FirebaseDataSource; // nuestra clase adaptada a Firestore
import com.example.lab6_20203266.databinding.FragmentResumenBinding;
import com.example.lab6_20203266.models.Tarea;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ResumenFragment extends Fragment {

    private FragmentResumenBinding b;
    private FirebaseDataSource ds;
    private ListenerRegistration reg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentResumenBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ds = new FirebaseDataSource();

        reg = ds.listenAll((QuerySnapshot snap, com.google.firebase.firestore.FirebaseFirestoreException e) -> {
            int total = 0, comp = 0, pend = 0;
            if (e == null && snap != null) {
                for (DocumentSnapshot d : snap.getDocuments()) {
                    Tarea t = d.toObject(Tarea.class);
                    if (t != null) {
                        total++;
                        if (t.isEstado()) comp++; else pend++;
                    }
                }
            }
            b.tvResumen.setText("Total: " + total + "  |  Completadas: " + comp + "  |  Pendientes: " + pend);

            Pie pie = AnyChart.pie();
            List<DataEntry> data = new ArrayList<>();
            data.add(new ValueDataEntry("Completadas", comp));
            data.add(new ValueDataEntry("Pendientes", pend));
            pie.data(data);
            b.chart.setChart(pie);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reg != null) reg.remove();
        b = null;
    }
}
