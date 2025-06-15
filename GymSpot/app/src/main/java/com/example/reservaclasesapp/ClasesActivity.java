package com.example.reservaclasesapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ClasesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Clase> listaClases = new ArrayList<>();
    int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clases);

        SharedPreferences preferences = getSharedPreferences("Sesion", MODE_PRIVATE);
        idUsuario = preferences.getInt("id_usuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewClases);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        obtenerClases();
    }

    private void obtenerClases() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/clases.php?id_usuario=" + idUsuario);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray data = jsonResponse.getJSONArray("data");

                listaClases.clear();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    Clase clase = new Clase();
                    clase.id = obj.getInt("id");
                    clase.nombre = obj.getString("nombre");
                    clase.fecha = obj.getString("fecha");
                    clase.hora = obj.getString("hora");
                    clase.cupo_maximo = obj.getInt("cupo_maximo");
                    clase.cupo_actual = obj.getInt("cupo_actual");
                    clase.inscrito = obj.getBoolean("inscrito");
                    clase.imagen_url = obj.getString("imagen_url");
                    listaClases.add(clase);
                }

                runOnUiThread(() -> recyclerView.setAdapter(new ClaseAdapter(this, listaClases, idUsuario)));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al cargar clases: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
