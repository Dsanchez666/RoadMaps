package com.example.roadmap.domain;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Value object representing one expediente associated to an initiative.
 *
 * <p>
 * This object is persisted as JSON inside the initiative row, enabling
 * multiple expedientes per initiative with optional custom fields.
 * </p>
 *
 * @since 1.1
 */
public class InitiativeExpediente {
    private String tipo;
    private String empresa;
    private String expediente;
    private String impacto;
    private String precio_licitacion;
    private String precio_adjudicacion;
    private String fecha_fin_expediente;
    private Map<String, String> informacion_adicional = new LinkedHashMap<>();

    public InitiativeExpediente() {
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getExpediente() {
        return expediente;
    }

    public void setExpediente(String expediente) {
        this.expediente = expediente;
    }

    public String getImpacto() {
        return impacto;
    }

    public void setImpacto(String impacto) {
        this.impacto = impacto;
    }

    public String getPrecio_licitacion() {
        return precio_licitacion;
    }

    public void setPrecio_licitacion(String precio_licitacion) {
        this.precio_licitacion = precio_licitacion;
    }

    public String getPrecio_adjudicacion() {
        return precio_adjudicacion;
    }

    public void setPrecio_adjudicacion(String precio_adjudicacion) {
        this.precio_adjudicacion = precio_adjudicacion;
    }

    public String getFecha_fin_expediente() {
        return fecha_fin_expediente;
    }

    public void setFecha_fin_expediente(String fecha_fin_expediente) {
        this.fecha_fin_expediente = fecha_fin_expediente;
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
