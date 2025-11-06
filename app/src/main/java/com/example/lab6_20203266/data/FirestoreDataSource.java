package com.example.lab6_20203266.data;

import com.example.lab6_20203266.models.Tarea;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.Objects;

public class FirestoreDataSource {

    private final FirebaseFirestore db;
    private final String uid;
    private final CollectionReference col;

    public FirestoreDataSource() {
        this.db = FirebaseFirestore.getInstance();
        this.uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.col = db.collection("usuarios").document(uid).collection("tareas");
    }

    /** Escucha en vivo todas las tareas (ordenadas por fecha) */
    public ListenerRegistration listenAll(EventListener<QuerySnapshot> listener) {
        return col.orderBy("fechaLimiteMillis", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }

    /** Crea o sobrescribe una tarea (si trae id, la usa; si no, genera una) */
    public void addTarea(Tarea t) {
        if (t.getId() == null || t.getId().isEmpty()) {
            DocumentReference ref = col.document();
            t.setId(ref.getId());
            ref.set(t);
        } else {
            col.document(t.getId()).set(t);
        }
    }

    /** Actualiza campos de una tarea (merge) */
    public void updateTarea(Tarea t) {
        if (t.getId() != null) {
            col.document(t.getId()).set(t, SetOptions.merge());
        }
    }

    /** Elimina una tarea */
    public void deleteTarea(String id, Runnable onOk, Runnable onErr) {
        col.document(id).delete()
                .addOnSuccessListener(v -> { if (onOk != null) onOk.run(); })
                .addOnFailureListener(e -> { if (onErr != null) onErr.run(); });
    }
}
