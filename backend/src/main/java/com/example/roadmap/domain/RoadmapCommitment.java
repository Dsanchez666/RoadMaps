package com.example.roadmap.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Value object representing one commitment linked to a roadmap.
 *
 * <p>
 * Commitments are managed from roadmap visualization and allow dynamic
 * custom attributes through {@code informacion_adicional}.
 * </p>
 *
 * @since 1.1
 */
public class RoadmapCommitment {
    private String id;
    private String descripcion;
    private String fecha_comprometido;
    private String actor;
    private String quien_compromete;
    private Map<String, String> informacion_adicional = new LinkedHashMap<>();

    public RoadmapCommitment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha_comprometido() {
        return fecha_comprometido;
    }

    public void setFecha_comprometido(String fecha_comprometido) {
        this.fecha_comprometido = fecha_comprometido;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getQuien_compromete() {
        return quien_compromete;
    }

    public void setQuien_compromete(String quien_compromete) {
        this.quien_compromete = quien_compromete;
    }

    public Map<String, String> getInformacion_adicional() {
        return informacion_adicional;
    }

    public void setInformacion_adicional(Map<String, String> informacion_adicional) {
        if (informacion_adicional == null) {
            this.informacion_adicional = new LinkedHashMap<>();
            return;
        }
        this.informacion_adicional = new LinkedHashMap<>(informacion_adicional);
    }
}
