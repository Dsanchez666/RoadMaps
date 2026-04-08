package com.example.roadmap.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate with editable roadmap configuration details.
 *
 * This structure maps the JSON contract used by the Angular configurator.
 *
 * @since 1.0
 */
public class RoadmapConfig {
    private String producto;
    private String organizacion;
    private RoadmapHorizon horizonte_base;
    private List<StrategicAxis> ejes_estrategicos = new ArrayList<>();
    private List<Initiative> iniciativas = new ArrayList<>();
    private List<InitiativeExpediente> expedientes_catalogo = new ArrayList<>();
    private List<RoadmapCommitment> compromisos = new ArrayList<>();

    public RoadmapConfig() {
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getOrganizacion() {
        return organizacion;
    }

    public void setOrganizacion(String organizacion) {
        this.organizacion = organizacion;
    }

    public RoadmapHorizon getHorizonte_base() {
        return horizonte_base;
    }

    public void setHorizonte_base(RoadmapHorizon horizonte_base) {
        this.horizonte_base = horizonte_base;
    }

    public List<StrategicAxis> getEjes_estrategicos() {
        return ejes_estrategicos;
    }

    public void setEjes_estrategicos(List<StrategicAxis> ejes_estrategicos) {
        this.ejes_estrategicos = ejes_estrategicos;
    }

    public List<Initiative> getIniciativas() {
        return iniciativas;
    }

    public void setIniciativas(List<Initiative> iniciativas) {
        this.iniciativas = iniciativas;
    }

    public List<InitiativeExpediente> getExpedientes_catalogo() {
        return expedientes_catalogo;
    }

    public void setExpedientes_catalogo(List<InitiativeExpediente> expedientes_catalogo) {
        this.expedientes_catalogo = expedientes_catalogo;
    }

    public List<RoadmapCommitment> getCompromisos() {
        return compromisos;
    }

    public void setCompromisos(List<RoadmapCommitment> compromisos) {
        this.compromisos = compromisos;
    }
}
