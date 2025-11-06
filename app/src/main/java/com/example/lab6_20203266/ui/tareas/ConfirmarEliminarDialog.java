package com.example.lab6_20203266.ui.tareas;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirmarEliminarDialog extends DialogFragment {

    public interface OnConfirm { void run(); }

    public static void show(@NonNull androidx.fragment.app.FragmentManager fm, @NonNull OnConfirm onConfirm) {
        new androidx.appcompat.app.AlertDialog.Builder(fm.findFragmentById(android.R.id.content) != null ?
                fm.findFragmentById(android.R.id.content).requireContext() :
                ((androidx.fragment.app.FragmentActivity)fm.getFragments().get(0).getActivity()))
                .setTitle("Confirmar")
                .setMessage("¿Eliminar esta tarea?")
                .setPositiveButton("Sí", (d,w) -> onConfirm.run())
                .setNegativeButton("No", null)
                .show();
    }
}
