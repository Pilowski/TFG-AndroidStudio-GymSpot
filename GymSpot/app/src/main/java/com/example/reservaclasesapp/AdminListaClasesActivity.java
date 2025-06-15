package com.example.reservaclasesapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminListaClasesActivity extends AppCompatActivity {

    LinearLayout contenedor;
    LayoutInflater inflater;
    int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lista_clases);

        SharedPreferences preferences = getSharedPreferences("Sesion", MODE_PRIVATE);
        idUsuario = preferences.getInt("id_usuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        contenedor = findViewById(R.id.contenedorAdminClases);
        inflater = LayoutInflater.from(this);

        cargarClases();

        Button btnVolver = findViewById(R.id.btnVolverLista);
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarClases() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/clases.php?id_usuario=" + idUsuario);
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

                runOnUiThread(() -> {
                    contenedor.removeAllViews();

                    for (int i = 0; i < data.length(); i++) {
                        try {
                            JSONObject obj = data.getJSONObject(i);
                            int idClase = obj.getInt("id");
                            String nombre = obj.getString("nombre");
                            String fecha = obj.getString("fecha");
                            String hora = obj.getString("hora");
                            int cupoMax = obj.getInt("cupo_maximo");
                            String imagenUrl = obj.getString("imagen_url");

                            View view = inflater.inflate(R.layout.item_admin_clase, contenedor, false);

                            TextView tvNombre = view.findViewById(R.id.tvAdminNombre);
                            TextView tvFecha = view.findViewById(R.id.tvAdminFecha);
                            TextView tvHora = view.findViewById(R.id.tvAdminHora);
                            ImageView imgClase = view.findViewById(R.id.imgAdminClase);
                            Button btnEditar = view.findViewById(R.id.btnEditar);
                            Button btnEliminar = view.findViewById(R.id.btnEliminar);

                            tvNombre.setText(nombre);
                            tvFecha.setText("Fecha: " + fecha);
                            tvHora.setText("Hora: " + hora);

                            Picasso.get()
                                    .load(imagenUrl)
                                    .placeholder(R.drawable.clase_default)
                                    .into(imgClase);

                            final int idFinal = idClase;
                            final String nombreFinal = nombre;
                            final String fechaFinal = fecha;
                            final String horaFinal = hora;
                            final int cupoFinal = cupoMax;
                            final String imagenFinal = imagenUrl;

                            btnEditar.setOnClickListener(v -> {
                                Intent intent = new Intent(AdminListaClasesActivity.this, AdminEditarClaseActivity.class);
                                intent.putExtra("id", idFinal);
                                intent.putExtra("nombre", nombreFinal);
                                intent.putExtra("fecha", fechaFinal);
                                intent.putExtra("hora", horaFinal);
                                intent.putExtra("cupo", cupoFinal);
                                intent.putExtra("imagen_url", imagenFinal);
                                startActivity(intent);
                            });

                            btnEliminar.setOnClickListener(v -> confirmarEliminarClase(idFinal));

                            contenedor.addView(view);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error cargando clases: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void confirmarEliminarClase(int idClase) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar esta clase?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarClase(idClase))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarClase(int idClase) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/eliminar_clase.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String postData = "id_clase=" + idClase;

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                conn.disconnect();

                // Agregar log para verificar qué devuelve el servidor
                String respuesta = sb.toString();
                System.out.println("RESPUESTA_ELIMINAR: " + respuesta);

                JSONObject json = new JSONObject(respuesta);
                String status = json.optString("status");

                runOnUiThread(() -> {
                    if (status.equals("success")) {
                        Toast.makeText(this, "Clase eliminada correctamente", Toast.LENGTH_SHORT).show();
                        cargarClases();
                    } else {
                        String mensaje = json.optString("message", "Error desconocido");
                        Toast.makeText(this, "Error: " + mensaje, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al eliminar clase: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarClases();
    }
}
