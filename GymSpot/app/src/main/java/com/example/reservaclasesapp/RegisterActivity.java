package com.example.reservaclasesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    EditText nameInput, lastnameInput, ageInput, emailInput, passwordInput;
    Spinner genderSpinner;
    Button registerButton, backToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.nameInput);
        lastnameInput = findViewById(R.id.lastnameInput);
        ageInput = findViewById(R.id.ageInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        registerButton = findViewById(R.id.registerButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opciones_sexo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        registerButton.setOnClickListener(v -> registrarUsuario());
        backToLoginButton.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        String nombre = nameInput.getText().toString().trim();
        String apellidos = lastnameInput.getText().toString().trim();
        String edadStr = ageInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String sexo = genderSpinner.getSelectedItem().toString();

        if (nombre.isEmpty() || apellidos.isEmpty() || edadStr.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validarContrasena(password)) {
            Toast.makeText(this, "La contraseña no cumple los requisitos de seguridad", Toast.LENGTH_LONG).show();
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Edad no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/registro.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject datos = new JSONObject();
                datos.put("nombre", nombre);
                datos.put("apellidos", apellidos);
                datos.put("edad", edad);
                datos.put("sexo", sexo);
                datos.put("email", email);
                datos.put("password", password);

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
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
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

    private boolean validarContrasena(String contrasena) {
        return contrasena.length() >= 8 &&
                contrasena.matches(".*[A-Z].*") &&
                contrasena.matches(".*[0-9].*") &&
                contrasena.matches(".*[!@#$%^&*()\\-+=<>?{}\\[\\]~`|\\\\/:\"';.,].*");
    }
}
