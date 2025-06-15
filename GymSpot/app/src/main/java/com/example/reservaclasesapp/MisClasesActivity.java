package com.example.reservaclasesapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class MisClasesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Clase> listaClases = new ArrayList<>();
    private int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clases); // Reutiliza el layout con recyclerViewClases y btnVolver

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

        cargarMisClases();
    }

    private void cargarMisClases() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/mis_clases.php?id_usuario=" + idUsuario);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                JSONObject json = new JSONObject(sb.toString());
                JSONArray data = json.getJSONArray("data");

                listaClases.clear();

                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    Clase clase = new Clase();
                    clase.id = obj.getInt("id");
                    clase.nombre = obj.getString("nombre");
                    clase.fecha = obj.getString("fecha");
                    clase.hora = obj.getString("hora");
                    clase.imagen_url = obj.getString("imagen_url"); // ✅ Imagen real
                    clase.inscrito = true;

                    Log.d("IMAGEN_URL", clase.imagen_url); // 🔍 Log de comprobación

                    listaClases.add(clase);
                }

                runOnUiThread(() ->
                        recyclerView.setAdapter(new MisClasesAdapter(this, listaClases, idUsuario))
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error cargando clases: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
