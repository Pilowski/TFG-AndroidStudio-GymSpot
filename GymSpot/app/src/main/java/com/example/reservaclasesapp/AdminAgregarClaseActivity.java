package com.example.reservaclasesapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class AdminAgregarClaseActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    ImageView imagenSeleccionada;
    Button btnSeleccionarImagen, btnSubirClase, btnVolver;
    EditText inputNombre, inputFecha, inputHora, inputCupo;

    Uri imagenUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_agregar_clase);

        imagenSeleccionada = findViewById(R.id.imagenSeleccionada);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnSubirClase = findViewById(R.id.btnSubirClase);
        btnVolver = findViewById(R.id.btnVolver);
        inputNombre = findViewById(R.id.inputNombre);
        inputFecha = findViewById(R.id.inputFecha);
        inputHora = findViewById(R.id.inputHora);
        inputCupo = findViewById(R.id.inputCupo);

        btnSeleccionarImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        inputFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        String fecha = String.format("%04d-%02d-%02d", year, month + 1, day);
                        inputFecha.setText(fecha);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        inputHora.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        String hora = String.format("%02d:%02d:00", hourOfDay, minute);
                        inputHora.setText(hora);
                    },
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
            ).show();
        });

        btnSubirClase.setOnClickListener(v -> subirClase());
        btnVolver.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imagenUri = data.getData();
            imagenSeleccionada.setImageURI(imagenUri);
        }
    }

    private void subirClase() {
        String nombre = inputNombre.getText().toString().trim();
        String fecha = inputFecha.getText().toString().trim();
        String hora = inputHora.getText().toString().trim();
        String cupoStr = inputCupo.getText().toString().trim();

        if (nombre.isEmpty() || fecha.isEmpty() || hora.isEmpty() || cupoStr.isEmpty() || imagenUri == null) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int cupo = Integer.parseInt(cupoStr);

        new Thread(() -> {
            try {
                String boundary = "===" + System.currentTimeMillis() + "===";
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/subir_clase.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setDoOutput(true);

                DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());

                escribirCampo(outputStream, boundary, "nombre", nombre);
                escribirCampo(outputStream, boundary, "fecha", fecha);
                escribirCampo(outputStream, boundary, "hora", hora);
                escribirCampo(outputStream, boundary, "cupo_maximo", String.valueOf(cupo));

                escribirArchivo(outputStream, boundary, "imagen", imagenUri);

                outputStream.writeBytes("--" + boundary + "--\r\n");
                outputStream.flush();
                outputStream.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    response.append(linea);
                }

                runOnUiThread(() -> Toast.makeText(this, "Clase subida correctamente", Toast.LENGTH_LONG).show());

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
        String nombreArchivo = getFileName(archivoUri);
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

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx != -1) {
                        result = cursor.getString(idx);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return System.currentTimeMillis() + "_" + result;
    }
}
