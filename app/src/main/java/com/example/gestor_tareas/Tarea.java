package com.example.gestor_tareas;

public class Tarea {
    private int id;
    private String titulo;
    private String descripcion;
    private String estado;   // "Pendiente" o "Completada"

    public Tarea(int id, String titulo, String descripcion, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
}