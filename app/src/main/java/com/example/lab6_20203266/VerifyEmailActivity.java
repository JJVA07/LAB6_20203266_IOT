package com.example.lab6_20203266;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20203266.databinding.ActivityVerifyEmailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    private ActivityVerifyEmailBinding b;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVerifyEmailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        auth = FirebaseAuth.getInstance();
        FirebaseUser u = auth.getCurrentUser();

        b.tvInfo.setText("Te enviamos un correo de verificación a tu bandeja. " +
                "Confirma tu correo y luego presiona 'Ya verifiqué'.");

        b.btnReenviar.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) { toast("Inicia sesión nuevamente"); return; }
            b.progress.setVisibility(View.VISIBLE);
            user.sendEmailVerification()
                    .addOnSuccessListener(x -> toast("Correo reenviado"))
                    .addOnFailureListener(e -> toast("Error: " + e.getMessage()))
                    .addOnCompleteListener(task -> b.progress.setVisibility(View.GONE));
        });

        b.btnYaVerifique.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) { toast("Inicia sesión nuevamente"); return; }
            user.reload().addOnSuccessListener(zz -> {
                if (user.isEmailVerified()) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    toast("Aún no verificas tu correo");
                }
            });
        });

        b.btnCambiarCorreo.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void toast(String s){ Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
