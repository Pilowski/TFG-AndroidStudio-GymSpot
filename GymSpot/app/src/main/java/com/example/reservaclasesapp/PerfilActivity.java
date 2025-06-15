package com.example.reservaclasesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PerfilActivity extends AppCompatActivity {

    TextView tvNombre, tvApellidos, tvEdad, tvSexo, tvEmail;
    int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        SharedPreferences preferences = getSharedPreferences("Sesion", MODE_PRIVATE);
        idUsuario = preferences.getInt("id_usuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Sesión no iniciada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvNombre = findViewById(R.id.tvNombre);
        tvApellidos = findViewById(R.id.tvApellidos);
        tvEdad = findViewById(R.id.tvEdad);
        tvSexo = findViewById(R.id.tvSexo);
        tvEmail = findViewById(R.id.tvEmail);

        Button btnEditar = findViewById(R.id.btnEditarPerfil);
        Button btnVolver = findViewById(R.id.btnVolverPerfil);

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarPerfilActivity.class);
            startActivity(intent);

        });
        Button btnCambiar = findViewById(R.id.btnCambiarContrasena);
        btnCambiar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CambiarContrasenaActivity.class);
            startActivity(intent);
        });

        btnVolver.setOnClickListener(v -> finish());

        cargarDatosPerfil();  // primera carga
    }

    private void cargarDatosPerfil() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/obtener_usuario.php?id_usuario=" + idUsuario);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                JSONObject response = new JSONObject(sb.toString());

                if (response.getString("status").equals("success")) {
                    JSONObject usuario = response.getJSONObject("data");

                    runOnUiThread(() -> {
                        tvNombre.setText("Nombre: " + usuario.optString("nombre"));
                        tvApellidos.setText("Apellidos: " + usuario.optString("apellidos"));
                        tvEdad.setText("Edad: " + usuario.optInt("edad"));
                        tvSexo.setText("Sexo: " + usuario.optString("sexo"));
                        tvEmail.setText("Email: " + usuario.optString("email"));
                    });

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error cargando datos", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosPerfil(); // recarga automática al volver desde edición
    }
}
