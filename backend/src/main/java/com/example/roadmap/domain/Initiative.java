package com.example.roadmap.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Value object representing one roadmap initiative.
 *
 * @since 1.0
 */
public class Initiative {
    private String id;
    private String nombre;
    private String eje;
    private String inicio;
    private String fin;
    private String certeza;
    private Map<String, String> informacion_adicional = new LinkedHashMap<>();
    private List<InitiativeExpediente> expedientes = new ArrayList<>();
    private List<InitiativeDependency> dependencias = new ArrayList<>();

    public Initiative() {
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

    public String getEje() {
        return eje;
    }

    public void setEje(String eje) {
        this.eje = eje;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public String getCerteza() {
        return certeza;
    }

    public void setCerteza(String certeza) {
        this.certeza = certeza;
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

    public List<InitiativeExpediente> getExpedientes() {
        return expedientes;
    }

    public void setExpedientes(List<InitiativeExpediente> expedientes) {
        if (expedientes == null) {
            this.expedientes = new ArrayList<>();
            return;
        }
        this.expedientes = expedientes;
    }

    public List<InitiativeDependency> getDependencias() {
        return dependencias;
    }

    public void setDependencias(List<InitiativeDependency> dependencias) {
        this.dependencias = dependencias;
    }

    /**
     * Backward-compatible setter mapping legacy JSON fields to the dynamic map.
     *
     * @param value Legacy field value.
     */
    @JsonSetter("tipo")
    public void setLegacyTipo(String value) {
        putAdditionalIfPresent("tipo", value);
    }

    /**
     * Backward-compatible setter mapping legacy JSON fields to the dynamic map.
     *
     * @param value Legacy field value.
     */
    @JsonSetter("expediente")
    public void setLegacyExpediente(String value) {
        putAdditionalIfPresent("expediente", value);
    }

    /**
     * Backward-compatible setter mapping legacy JSON fields to the dynamic map.
     *
     * @param value Legacy field value.
     */
    @JsonSetter("objetivo")
    public void setLegacyObjetivo(String value) {
        putAdditionalIfPresent("objetivo", value);
    }

    /**
     * Backward-compatible setter mapping legacy JSON fields to the dynamic map.
     *
     * @param value Legacy field value.
     */
    @JsonSetter("impacto_principal")
    public void setLegacyImpactoPrincipal(String value) {
        putAdditionalIfPresent("impacto_principal", value);
    }

    /**
     * Backward-compatible setter mapping legacy JSON fields to the dynamic map.
     *
     * @param value Legacy field value.
     */
    @JsonSetter("usuarios_afectados")
    public void setLegacyUsuariosAfectados(String value) {
        putAdditionalIfPresent("usuarios_afectados", value);
    }

    @JsonIgnore
    private void putAdditionalIfPresent(String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (informacion_adicional == null) {
            informacion_adicional = new LinkedHashMap<>();
        }
        informacion_adicional.put(key, value);
    }
}
