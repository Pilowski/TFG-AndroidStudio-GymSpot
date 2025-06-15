package com.example.reservaclasesapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class AdminEditarClaseActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    ImageView imagenClase;
    Button btnCambiarImagen, btnGuardar, btnVolver;
    EditText inputNombre, inputFecha, inputHora, inputCupo;

    Uri nuevaImagenUri = null;
    int idClase;
    String imagenUrlActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DEBUG_ADMIN", "Entrando a AdminEditarClaseActivity");
        setContentView(R.layout.activity_admin_editar_clase);

        imagenClase = findViewById(R.id.imagenClase);
        btnCambiarImagen = findViewById(R.id.btnCambiarImagen);
        btnGuardar = findViewById(R.id.btnGuardarCambios);
        btnVolver = findViewById(R.id.btnVolverEditar);
        inputNombre = findViewById(R.id.inputNombreEditar);
        inputFecha = findViewById(R.id.inputFechaEditar);
        inputHora = findViewById(R.id.inputHoraEditar);
        inputCupo = findViewById(R.id.inputCupoEditar);

        // Recibir datos
        Intent intent = getIntent();
        idClase = intent.getIntExtra("id", -1);
        if (idClase == -1) {
            Toast.makeText(this, "Error al recibir datos de la clase", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String nombre = intent.getStringExtra("nombre");
        String fecha = intent.getStringExtra("fecha");
        String hora = intent.getStringExtra("hora");
        int cupo = intent.getIntExtra("cupo", 0);
        imagenUrlActual = intent.getStringExtra("imagen_url");

        inputNombre.setText(nombre);
        inputFecha.setText(fecha);
        inputHora.setText(hora);
        inputCupo.setText(String.valueOf(cupo));

        try {
            Picasso.get().load(imagenUrlActual).into(imagenClase);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
        }

        inputFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        String f = String.format("%04d-%02d-%02d", year, month + 1, day);
                        inputFecha.setText(f);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        inputHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this,
                    (view, hour, minute) -> {
                        String h = String.format("%02d:%02d:00", hour, minute);
                        inputHora.setText(h);
                    },
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show();
        });

        btnCambiarImagen.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick, PICK_IMAGE_REQUEST);
        });

        btnVolver.setOnClickListener(v -> finish());

        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            nuevaImagenUri = data.getData();
            imagenClase.setImageURI(nuevaImagenUri);
        }
    }

    private void guardarCambios() {
        String nombre = inputNombre.getText().toString().trim();
        String fecha = inputFecha.getText().toString().trim();
        String hora = inputHora.getText().toString().trim();
        String cupoStr = inputCupo.getText().toString().trim();

        if (nombre.isEmpty() || fecha.isEmpty() || hora.isEmpty() || cupoStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int cupo = Integer.parseInt(cupoStr);

        new Thread(() -> {
            try {
                String boundary = "===" + System.currentTimeMillis() + "===";
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/editar_clase.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setDoOutput(true);

                DataOutputStream output = new DataOutputStream(conn.getOutputStream());

                escribirCampo(output, boundary, "id_clase", String.valueOf(idClase));
                escribirCampo(output, boundary, "nombre", nombre);
                escribirCampo(output, boundary, "fecha", fecha);
                escribirCampo(output, boundary, "hora", hora);
                escribirCampo(output, boundary, "cupo_maximo", String.valueOf(cupo));

                if (nuevaImagenUri != null) {
                    escribirArchivo(output, boundary, "imagen", nuevaImagenUri);
                }

                output.writeBytes("--" + boundary + "--\r\n");
                output.flush();
                output.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                runOnUiThread(() -> Toast.makeText(this, "Clase actualizada", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void escribirCampo(DataOutputStream stream, String boundary, String nombre, String valor) throws IOException {
        stream.writeBytes("--" + boundary + "\r\n");
        stream.writeBytes("Content-Disposition: form-data; name=\"" + nombre + "\"\r\n\r\n");
        stream.writeBytes(valor + "\r\n");
    }

    private void escribirArchivo(DataOutputStream stream, String boundary, String nombreCampo, Uri archivoUri) throws IOException {
        String nombreArchivo = System.currentTimeMillis() + ".jpg";
        stream.writeBytes("--" + boundary + "\r\n");
        stream.writeBytes("Content-Disposition: form-data; name=\"" + nombreCampo + "\"; filename=\"" + nombreArchivo + "\"\r\n");
        stream.writeBytes("Content-Type: image/jpeg\r\n\r\n");

        InputStream inputStream = getContentResolver().openInputStream(archivoUri);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            stream.write(buffer, 0, bytesRead);
        }
        stream.writeBytes("\r\n");
        inputStream.close();
    }
}
