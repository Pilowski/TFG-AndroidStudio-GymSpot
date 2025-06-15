package com.example.reservaclasesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setTheme(R.style.Theme_ReservaClasesApp_Light_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/login.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setDoOutput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("email", email);
                    jsonParam.put("password", password);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonParam.toString().getBytes("utf-8"));
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line.trim());
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");

                    runOnUiThread(() -> {
                        try {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            if (status.equals("success")) {
                                int idUsuario = jsonResponse.getInt("id_usuario");

                                SharedPreferences preferences = getSharedPreferences("Sesion", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("id_usuario", idUsuario);
                                editor.putString("nombre", jsonResponse.getString("nombre"));
                                editor.putString("email", jsonResponse.getString("email"));
                                editor.putString("rol", jsonResponse.getString("rol"));
                                editor.apply();

                                // 🔄 Aplicar tema según preferencia del usuario
                                SharedPreferences temaPrefs = getSharedPreferences("TemaPorUsuario", MODE_PRIVATE);
                                boolean modoOscuro = temaPrefs.getBoolean("modo_oscuro_" + idUsuario, false);
                                AppCompatDelegate.setDefaultNightMode(
                                        modoOscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                                );

                                Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error procesando respuesta del servidor", Toast.LENGTH_LONG).show();
                        }
                    });

                    conn.disconnect();

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }).start();
        });

        registerButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
