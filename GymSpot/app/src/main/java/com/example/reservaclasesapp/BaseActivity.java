package com.example.reservaclasesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actualizarNombreUsuarioEnToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarNombreUsuarioEnToolbar();
    }

    private void actualizarNombreUsuarioEnToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView nombreUsuarioTexto = findViewById(R.id.nombreUsuarioTexto);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
        }

        if (nombreUsuarioTexto != null) {
            SharedPreferences prefs = getSharedPreferences("Sesion", MODE_PRIVATE);
            String nombre = prefs.getString("nombre", "");
            String apellidos = prefs.getString("apellidos", "");
            nombreUsuarioTexto.setText(nombre);
        }
    }

    protected void setupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_usuario, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_ver_perfil) {
                startActivity(new Intent(this, PerfilActivity.class));
                return true;
            } else if (id == R.id.menu_cerrar_sesion) {
                SharedPreferences preferences = getSharedPreferences("Sesion", MODE_PRIVATE);
                preferences.edit().clear().apply();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}
