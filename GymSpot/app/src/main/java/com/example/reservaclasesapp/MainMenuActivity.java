package com.example.reservaclasesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class MainMenuActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Tema por usuario
        SharedPreferences sesionPrefs = getSharedPreferences("Sesion", MODE_PRIVATE);
        int idUsuario = sesionPrefs.getInt("id_usuario", -1);
        if (idUsuario != -1) {
            SharedPreferences temaPrefs = getSharedPreferences("TemaPorUsuario", MODE_PRIVATE);
            boolean modoOscuro = temaPrefs.getBoolean("modo_oscuro_" + idUsuario, false);
            AppCompatDelegate.setDefaultNightMode(
                    modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Referencia a toolbar incluido
        View toolbarLayout = findViewById(R.id.toolbar);
        Toolbar toolbar = toolbarLayout.findViewById(R.id.toolbar);
        TextView nombreUsuarioTexto = toolbarLayout.findViewById(R.id.nombreUsuarioTexto);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        SharedPreferences prefs = getSharedPreferences("Sesion", MODE_PRIVATE);
        String nombre = prefs.getString("nombre", "");
        String apellidos = prefs.getString("apellidos", "");
        nombreUsuarioTexto.setText(nombre + " " + apellidos);

        String rol = prefs.getString("rol", "usuario");

        // Referencias a cards y botones
        CardView cardAgregarClase = findViewById(R.id.cardAgregarClase);
        CardView cardEditarClase = findViewById(R.id.cardEditarClase);
        CardView cardMisClases = findViewById(R.id.cardMisClases);

        Button btnListadoClases = findViewById(R.id.btnListadoClases);
        Button btnMisClases = findViewById(R.id.btnMisClases);
        Button btnAgregarClase = findViewById(R.id.btnAgregarClase);
        Button btnEditarClase = findViewById(R.id.btnEditarClase);

        // Visibilidad condicional
        if (rol.equals("admin")) {
            cardAgregarClase.setVisibility(View.VISIBLE);
            cardEditarClase.setVisibility(View.VISIBLE);
            cardMisClases.setVisibility(View.GONE);
        } else {
            cardAgregarClase.setVisibility(View.GONE);
            cardEditarClase.setVisibility(View.GONE);
            cardMisClases.setVisibility(View.VISIBLE);
        }

        // Acciones
        btnListadoClases.setOnClickListener(v ->
                startActivity(new Intent(this, ClasesActivity.class)));

        btnMisClases.setOnClickListener(v ->
                startActivity(new Intent(this, MisClasesActivity.class)));

        btnAgregarClase.setOnClickListener(v ->
                startActivity(new Intent(this, AdminAgregarClaseActivity.class)));

        btnEditarClase.setOnClickListener(v ->
                startActivity(new Intent(this, AdminListaClasesActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.icono_modo_oscuro) {
            SharedPreferences sesionPrefs = getSharedPreferences("Sesion", MODE_PRIVATE);
            int idUsuario = sesionPrefs.getInt("id_usuario", -1);
            if (idUsuario == -1) return true;

            SharedPreferences temaPrefs = getSharedPreferences("TemaPorUsuario", MODE_PRIVATE);
            boolean actual = temaPrefs.getBoolean("modo_oscuro_" + idUsuario, false);
            boolean nuevo = !actual;

            temaPrefs.edit().putBoolean("modo_oscuro_" + idUsuario, nuevo).apply();
            AppCompatDelegate.setDefaultNightMode(
                    nuevo ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
            return true;

        } else if (id == R.id.icono_menu_usuario) {
            View anchor = findViewById(R.id.icono_menu_usuario);
            setupMenu(anchor);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
