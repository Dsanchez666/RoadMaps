package com.example.roadmap.domain;

/**
 * Value object representing roadmap base horizon.
 *
 * It stores start/end quarter identifiers using the format `YYYY-TQ`.
 *
 * @since 1.0
 */
public class RoadmapHorizon {
    private String inicio;
    private String fin;

    public RoadmapHorizon() {
    }

    public RoadmapHorizon(String inicio, String fin) {
        this.inicio = inicio;
        this.fin = fin;
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
}
