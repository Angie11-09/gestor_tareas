package com.example.gestor_tareas;

import com.google.firebase.firestore.DocumentId;

public class Tarea {

    @DocumentId
    private String documentId;
    private String titulo;
    private String descripcion;
    private String estado;

    // Constructor vacío OBLIGATORIO para Firestore
    public Tarea() {}

    public Tarea(String titulo, String descripcion, String estado) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}