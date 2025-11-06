package com.example.lab6_20203266;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20203266.databinding.ActivityMainBinding;
import com.example.lab6_20203266.ui.resumen.ResumenFragment;
import com.example.lab6_20203266.ui.tareas.TareasFragment;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // Default: Tareas
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_container, new TareasFragment()).commit();

        b.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tareas) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_container, new TareasFragment()).commit();
                return true;
            } else if (id == R.id.nav_resumen) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_container, new ResumenFragment()).commit();
                return true;
            } else if (id == R.id.nav_logout) {
                new AlertDialog.Builder(this)
                        .setTitle("Cerrar sesión")
                        .setMessage("¿Deseas salir de tu cuenta?")
                        .setPositiveButton("Sí", (d, w) -> {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
            return false;
        });
    }
}
