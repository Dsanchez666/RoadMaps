package com.example.roadmap.domain;

/**
 * Value object representing one initiative dependency link.
 *
 * @since 1.0
 */
public class InitiativeDependency {
    private String iniciativa;
    private String tipo;

    public InitiativeDependency() {
    }

    public InitiativeDependency(String iniciativa, String tipo) {
        this.iniciativa = iniciativa;
        this.tipo = tipo;
    }

    public String getIniciativa() {
        return iniciativa;
    }

    public void setIniciativa(String iniciativa) {
        this.iniciativa = iniciativa;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
