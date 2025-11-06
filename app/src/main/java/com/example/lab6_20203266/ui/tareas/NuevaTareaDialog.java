package com.example.lab6_20203266.ui.tareas;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lab6_20203266.data.FirebaseDataSource;
import com.example.lab6_20203266.databinding.DialogTareaBinding;
import com.example.lab6_20203266.models.Tarea;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NuevaTareaDialog extends DialogFragment {

    private static final String ARG_ID   = "arg_id";
    private static final String ARG_TIT  = "arg_tit";
    private static final String ARG_DESC = "arg_desc";
    private static final String ARG_DATE = "arg_date";
    private static final String ARG_EST  = "arg_est";

    private long selectedMillis = System.currentTimeMillis();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static NuevaTareaDialog newInstance(@Nullable Tarea t) {
        NuevaTareaDialog d = new NuevaTareaDialog();
        if (t != null) {
            Bundle args = new Bundle();
            args.putString(ARG_ID, t.getId());
            args.putString(ARG_TIT, t.getTitulo());
            args.putString(ARG_DESC, t.getDescripcion());
            args.putLong(ARG_DATE, t.getFechaLimiteMillis());
            args.putBoolean(ARG_EST, t.isEstado());
            d.setArguments(args);
        }
        return d;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DialogTareaBinding b = DialogTareaBinding.inflate(LayoutInflater.from(getContext()));

        // Precarga si estamos editando
        Bundle a = getArguments();
        final String editingId = (a != null) ? a.getString(ARG_ID) : null;

        if (a != null) {
            b.etTitulo.setText(a.getString(ARG_TIT, ""));
            b.etDescripcion.setText(a.getString(ARG_DESC, ""));
            selectedMillis = a.getLong(ARG_DATE, System.currentTimeMillis());
            b.cbCompletada.setChecked(a.getBoolean(ARG_EST, false));
        }
        // Pintar fecha inicial formateada
        b.etFecha.setText(sdf.format(new Date(selectedMillis)));

        // Deshabilitamos escritura y abrimos el date picker al tocar el campo
        b.etFecha.setFocusable(false);
        b.etFecha.setClickable(true);
        b.etFecha.setOnClickListener(v -> showDatePicker(b));

        return new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(editingId == null ? "Nueva tarea" : "Editar tarea")
                .setView(b.getRoot())
                .setPositiveButton("Guardar", (d1, w) -> {
                    String titulo = textOrEmpty(b.etTitulo);
                    String desc   = textOrEmpty(b.etDescripcion);
                    boolean est   = b.cbCompletada.isChecked();

                    if (TextUtils.isEmpty(titulo)) {
                        Toast.makeText(getContext(), "Completa el título", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseDataSource ds = new FirebaseDataSource();
                    Tarea t = new Tarea(editingId, titulo, desc, selectedMillis, est);
                    if (editingId == null) {
                        ds.addTarea(t);
                        Toast.makeText(getContext(), "Tarea registrada", Toast.LENGTH_SHORT).show();
                    } else {
                        ds.updateTarea(t);
                        Toast.makeText(getContext(), "Tarea modificada", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
    }

    private void showDatePicker(DialogTareaBinding b) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Selecciona fecha límite")
                .setSelection(selectedMillis)
                .build();

        picker.addOnPositiveButtonClickListener(sel -> {
            if (sel != null) {
                selectedMillis = sel;
                b.etFecha.setText(sdf.format(new Date(sel)));
            }
        });
        picker.show(getParentFragmentManager(), "datePicker");
    }

    private String textOrEmpty(android.widget.EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
