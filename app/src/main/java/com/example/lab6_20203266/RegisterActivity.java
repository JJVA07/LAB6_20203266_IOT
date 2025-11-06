package com.example.lab6_20203266;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20203266.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding b;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        auth = FirebaseAuth.getInstance();

        b.btnCrearCuenta.setOnClickListener(v -> registrar());
        b.btnIrLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registrar() {
        String email = b.etEmail.getText().toString().trim();
        String pass  = b.etPassword.getText().toString().trim();
        String pass2 = b.etPassword2.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(pass2)) {
            toast("Completa todos los campos"); return;
        }
        if (!pass.equals(pass2)) { toast("Las contraseñas no coinciden"); return; }
        if (pass.length() < 6)   { toast("Mínimo 6 caracteres"); return; }

        b.progress.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this::onRegisterResult);
    }

    private void onRegisterResult(Task<AuthResult> t) {
        b.progress.setVisibility(View.GONE);
        if (!t.isSuccessful()) { toast("Error: " + t.getException().getMessage()); return; }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnSuccessListener(v -> {
                        toast("Te enviamos un correo de verificación");
                        startActivity(new Intent(this, VerifyEmailActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> toast("No se pudo enviar el correo: " + e.getMessage()));
        }
    }

    private void toast(String s){ Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
