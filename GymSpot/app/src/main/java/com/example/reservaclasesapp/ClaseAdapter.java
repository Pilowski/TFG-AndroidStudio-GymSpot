package com.example.reservaclasesapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ClaseAdapter extends RecyclerView.Adapter<ClaseAdapter.ClaseViewHolder> {

    private final Context context;
    private final List<Clase> clases;
    private final int idUsuario;

    public ClaseAdapter(Context context, List<Clase> clases, int idUsuario) {
        this.context = context;
        this.clases = clases;
        this.idUsuario = idUsuario;
    }

    @NonNull
    @Override
    public ClaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clase, parent, false);
        return new ClaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClaseViewHolder holder, int position) {
        Clase clase = clases.get(position);

        int cupoDisponible = clase.getCupoDisponible();
        boolean estaInscrito = clase.inscrito;

        holder.nombreClase.setText(clase.nombre);
        holder.fechaHora.setText("Fecha y hora: " + clase.fecha + " " + clase.hora);
        holder.cupoInfo.setText("Cupo: " + clase.cupo_actual + " / " + clase.cupo_maximo);

        // Cargar imagen desde URL usando Picasso
        Picasso.get()
                .load(clase.imagen_url)
                .error(R.drawable.clase_default)
                .into(holder.imagen);

        if (estaInscrito) {
            holder.btnReservar.setVisibility(View.GONE);
            holder.estadoRegistro.setVisibility(View.VISIBLE);
            holder.estadoRegistro.setText("Registrado");
        } else if (cupoDisponible <= 0) {
            holder.btnReservar.setVisibility(View.GONE);
            holder.estadoRegistro.setVisibility(View.VISIBLE);
            holder.estadoRegistro.setText("Aforo completo");
        } else {
            holder.btnReservar.setVisibility(View.VISIBLE);
            holder.estadoRegistro.setVisibility(View.GONE);
            holder.btnReservar.setText("Registrarse");

            holder.btnReservar.setOnClickListener(v -> registrarUsuario(clase, holder));
        }
    }

    private void registrarUsuario(Clase clase, ClaseViewHolder holder) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2/ProyectoReservaClases/backend/api/reservar.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("id_usuario", idUsuario);
                json.put("id_clase", clase.id);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("utf-8"));
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

                conn.disconnect();

                ((ClasesActivity) context).runOnUiThread(() -> {
                    if (status.equals("success")) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        clase.inscrito = true;
                        clase.cupo_actual += 1;
                        notifyItemChanged(holder.getAdapterPosition());
                    } else {
                        Toast.makeText(context, "Error al registrar: " + message, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                ((ClasesActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return clases.size();
    }

    public static class ClaseViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombreClase, fechaHora, cupoInfo, estadoRegistro;
        Button btnReservar;

        public ClaseViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagenClase);
            nombreClase = itemView.findViewById(R.id.nombreClase);
            fechaHora = itemView.findViewById(R.id.fechaHoraClase);
            cupoInfo = itemView.findViewById(R.id.cupoInfo);
            btnReservar = itemView.findViewById(R.id.btnReservar);
            estadoRegistro = itemView.findViewById(R.id.estadoRegistro);
        }
    }
}
