package com.example.lab6_20203266.models;

public class Tarea {
    private String id;
    private String titulo;
    private String descripcion;
    private long fechaLimiteMillis; // guardamos Date como epoch ms
    private boolean estado; // false = pendiente, true = completada

    public Tarea() {} // requerido por Firebase
    public Tarea(String id, String titulo, String descripcion, long fechaLimiteMillis, boolean estado) {
        this.id = id; this.titulo = titulo; this.descripcion = descripcion;
        this.fechaLimiteMillis = fechaLimiteMillis; this.estado = estado;
    }
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public long getFechaLimiteMillis() { return fechaLimiteMillis; }
    public boolean isEstado() { return estado; }

    public void setId(String id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFechaLimiteMillis(long v) { this.fechaLimiteMillis = v; }
    public void setEstado(boolean estado) { this.estado = estado; }
}