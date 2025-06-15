package com.example.reservaclasesapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CambiarContrasenaActivity extends AppCompatActivity {

    EditText inputActual, inputNueva, inputConfirmar;
    Button btnGuardar, btnVolver;
    int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_contrasena);

        inputActual = findViewById(R.id.inputActual);
        inputNueva = findViewById(R.id.inputNueva);
        inputConfirmar = findViewById(R.id.inputConfirmar);
        btnGuardar = findViewById(R.id.btnGuardarCambio);
        btnVolver = findViewById(R.id.btnVolverCambiar);

        SharedPreferences preferences = getSharedPreferences("Sesion", MODE_PRIVATE);
        idUsuario = preferences.getInt("id_usuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnGuardar.setOnClickListener(v -> cambiarPassword());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cambiarPassword() {
        String actual = inputActual.getText().toString().trim();
        String nueva = inputNueva.getText().toString().trim();
        String confirmar = inputConfirmar.getText().toString().trim();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nueva.equals(confirmar)) {
            Toast.makeText(this, "Las nuevas contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarSeguridad(nueva)) {
            Toast.makeText(this, "La nueva contraseña no cumple los requisitos de seguridad", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/cambiar_contrasena.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject datos = new JSONObject();
                datos.put("id_usuario", idUsuario);
                datos.put("password_actual", actual);
                datos.put("password_nueva", nueva);

                OutputStream os = conn.getOutputStream();
                os.write(datos.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder respuesta = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    respuesta.append(linea);
                }

                JSONObject json = new JSONObject(respuesta.toString());

                runOnUiThread(() -> {
                    if (json.optString("status").equals("success")) {
                        Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + json.optString("message"), Toast.LENGTH_LONG).show();
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

    private boolean validarSeguridad(String contrasena) {
        return contrasena.length() >= 8 &&
                contrasena.matches(".*[A-Z].*") &&      // al menos una mayúscula
                contrasena.matches(".*[0-9].*") &&      // al menos un número
                contrasena.matches(".*[!@#$%^&*()\\-+=<>?{}\\[\\]~`|\\\\/:\"';.,].*");  // símbolo
    }
}
