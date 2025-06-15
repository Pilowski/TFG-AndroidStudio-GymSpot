package com.example.reservaclasesapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditarPerfilActivity extends AppCompatActivity {

    EditText inputNombre, inputApellidos, inputEdad, inputEmail;
    Spinner spinnerSexo;
    Button btnGuardar, btnVolver;
    int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        inputNombre = findViewById(R.id.inputNombre);
        inputApellidos = findViewById(R.id.inputApellidos);
        inputEdad = findViewById(R.id.inputEdad);
        inputEmail = findViewById(R.id.inputEmail);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);
        btnVolver = findViewById(R.id.btnVolverPerfilEditar);

        SharedPreferences preferences = getSharedPreferences("Sesion", MODE_PRIVATE);
        idUsuario = preferences.getInt("id_usuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar opciones del spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_sexo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSexo.setAdapter(adapter);

        cargarDatosUsuario();

        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarDatosUsuario() {
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
                        inputNombre.setText(usuario.optString("nombre"));
                        inputApellidos.setText(usuario.optString("apellidos"));
                        inputEdad.setText(String.valueOf(usuario.optInt("edad")));
                        inputEmail.setText(usuario.optString("email"));

                        String sexo = usuario.optString("sexo");
                        if (sexo.equalsIgnoreCase("hombre")) {
                            spinnerSexo.setSelection(0);
                        } else {
                            spinnerSexo.setSelection(1);
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void guardarCambios() {
        String nombre = inputNombre.getText().toString().trim();
        String apellidos = inputApellidos.getText().toString().trim();
        String edadStr = inputEdad.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String sexo = spinnerSexo.getSelectedItem().toString();

        if (nombre.isEmpty() || apellidos.isEmpty() || edadStr.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad = Integer.parseInt(edadStr);

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/actualizar_usuario.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("id_usuario", idUsuario);
                json.put("nombre", nombre);
                json.put("apellidos", apellidos);
                json.put("edad", edad);
                json.put("sexo", sexo);
                json.put("email", email);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONObject resJson = new JSONObject(response.toString());
                String status = resJson.getString("status");

                runOnUiThread(() -> {
                    if (status.equals("success")) {
                        // ✅ Actualizar SharedPreferences con los nuevos datos
                        SharedPreferences preferences = getSharedPreferences("Sesion", MODE_PRIVATE);
                        preferences.edit()
                                .putString("nombre", nombre)
                                .putString("apellidos", apellidos)
                                .putString("email", email)
                                .putString("sexo", sexo)
                                .putInt("edad", edad)
                                .apply();

                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_LONG).show();
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
