package com.example.reservaclasesapp;

public class Clase {
    public int id;
    public String nombre;
    public String fecha;
    public String hora;
    public int cupo_maximo;
    public int cupo_actual;
    public boolean inscrito;
    public String imagen_url;

    public int getCupoDisponible() {
        return cupo_maximo - cupo_actual;
    }
}
