package com.example.roadmap.domain;

/**
 * Value object representing one strategic axis in a roadmap.
 *
 * @since 1.0
 */
public class StrategicAxis {
    private String id;
    private String nombre;
    private String descripcion;
    private String color;

    public StrategicAxis() {
    }

    public StrategicAxis(String id, String nombre, String descripcion, String color) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
