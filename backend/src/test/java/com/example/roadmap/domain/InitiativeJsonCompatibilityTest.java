package com.example.roadmap.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InitiativeJsonCompatibilityTest {

    @Test
    void shouldMapLegacyFieldsIntoInformacionAdicional() throws Exception {
        String json = """
            {
              "id": "INI-1",
              "nombre": "Iniciativa 1",
              "eje": "E1",
              "inicio": "2026-T1",
              "fin": "2026-T2",
              "certeza": "planificado",
              "tipo": "mantenimiento",
              "expediente": "DNA-123",
              "objetivo": "Mejorar disponibilidad"
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        Initiative initiative = mapper.readValue(json, Initiative.class);

        assertNotNull(initiative.getInformacion_adicional());
        assertEquals("mantenimiento", initiative.getInformacion_adicional().get("tipo"));
        assertEquals("DNA-123", initiative.getInformacion_adicional().get("expediente"));
        assertEquals("Mejorar disponibilidad", initiative.getInformacion_adicional().get("objetivo"));
    }
}
