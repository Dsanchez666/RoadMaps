package com.example.roadmap.adapters.in.web;

import com.example.roadmap.domain.RoadmapConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoadmapControllerImportRoadmapDtoTest {

    @Test
    void toRoadmapConfigUsesProductoWhenPresent() {
        RoadmapController.ImportRoadmapDto dto = new RoadmapController.ImportRoadmapDto();
        dto.title = "Titulo";
        dto.producto = "Producto";
        dto.organizacion = "Org";

        RoadmapConfig config = dto.toRoadmapConfig();

        assertEquals("Producto", config.getProducto());
        assertEquals("Org", config.getOrganizacion());
        assertNotNull(config.getHorizonte_base());
        assertNotNull(config.getEjes_estrategicos());
        assertNotNull(config.getIniciativas());
    }

    @Test
    void toRoadmapConfigFallsBackToTitleWhenProductoMissing() {
        RoadmapController.ImportRoadmapDto dto = new RoadmapController.ImportRoadmapDto();
        dto.title = "SoloTitulo";
        dto.producto = "";

        RoadmapConfig config = dto.toRoadmapConfig();

        assertEquals("SoloTitulo", config.getProducto());
    }

    @Test
    void toRoadmapConfigMapsLegacyInitiativeFieldsIntoAdditionalInfo() throws Exception {
        String json = """
            {
              "title": "ETNA",
              "iniciativas": [
                {
                  "id": "I1",
                  "nombre": "Init",
                  "eje": "E1",
                  "inicio": "2026-T1",
                  "fin": "2026-T2",
                  "certeza": "planificado",
                  "tipo": "mantenimiento",
                  "expediente": "DNA-1"
                }
              ]
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        RoadmapController.ImportRoadmapDto dto = mapper.readValue(json, RoadmapController.ImportRoadmapDto.class);
        RoadmapConfig config = dto.toRoadmapConfig();

        assertEquals("mantenimiento", config.getIniciativas().get(0).getInformacion_adicional().get("tipo"));
        assertEquals("DNA-1", config.getIniciativas().get(0).getInformacion_adicional().get("expediente"));
    }
}
