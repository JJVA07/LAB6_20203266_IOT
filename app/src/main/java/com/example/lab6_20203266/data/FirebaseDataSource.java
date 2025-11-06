package com.example.lab6_20203266.data;

import androidx.annotation.Nullable;

import com.example.lab6_20203266.models.Tarea;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Objects;

/**
 * Adaptador con el mismo nombre que antes (FirebaseDataSource),
 * pero implementado sobre Cloud Firestore.
 */
public class FirebaseDataSource {

    private final FirebaseFirestore db;
    private final String uid;
    private final CollectionReference col;

    public FirebaseDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.col = db.collection("usuarios").document(uid).collection("tareas");
    }

    /** Escucha todas las tareas ordenadas por fecha límite (en vivo). */
    public ListenerRegistration listenAll(EventListener<QuerySnapshot> listener) {
        return col.orderBy("fechaLimiteMillis", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }

    /** Crea o sobrescribe una tarea. Si no trae id, lo genera. */
    public void addTarea(Tarea t) {
        if (t.getId() == null || t.getId().isEmpty()) {
            DocumentReference ref = col.document();
            t.setId(ref.getId());
            ref.set(t);
        } else {
            col.document(t.getId()).set(t);
        }
    }

    /** Actualiza/merge de una tarea existente. */
    public void updateTarea(Tarea t) {
        if (t.getId() != null && !t.getId().isEmpty()) {
            col.document(t.getId()).set(t, SetOptions.merge());
        }
    }

    /** Elimina una tarea por id. */
    public void deleteTarea(String id, @Nullable Runnable onOk, @Nullable Runnable onErr) {
        col.document(id).delete()
                .addOnSuccessListener(v -> { if (onOk != null) onOk.run(); })
                .addOnFailureListener(e -> { if (onErr != null) onErr.run(); });
    }
}
