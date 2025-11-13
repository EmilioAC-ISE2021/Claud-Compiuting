package cc.sars.controller.api;

import cc.sars.controller.api.dto.GrupoCreateDTO;
import cc.sars.model.Grupo;
import cc.sars.service.GrupoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GrupoRestController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class GrupoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GrupoService grupoService;

    private Grupo grupo1;
    private Grupo grupo2;

    @BeforeEach
    void setUp() {
        grupo1 = new Grupo("GrupoA");
        grupo2 = new Grupo("GrupoB");
    }

    @Test
    void obtenerTodosLosGrupos_deberiaRetornarListaDeGrupos() throws Exception {
        when(grupoService.getAllGrupos()).thenReturn(Arrays.asList(grupo1, grupo2));

        mockMvc.perform(get("/api/grupos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("GrupoA"))
                .andExpect(jsonPath("$[1].nombre").value("GrupoB"));
    }

    @Test
    void obtenerGrupoPorNombre_deberiaRetornarGrupoExistente() throws Exception {
        when(grupoService.getGrupoPorNombre("GrupoA")).thenReturn(grupo1);

        mockMvc.perform(get("/api/grupos/GrupoA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("GrupoA"));
    }

    @Test
    void obtenerGrupoPorNombre_deberiaRetornarNotFoundParaGrupoInexistente() throws Exception {
        when(grupoService.getGrupoPorNombre(anyString())).thenThrow(new RuntimeException("Grupo no encontrado"));

        mockMvc.perform(get("/api/grupos/GrupoC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearGrupo_deberiaCrearNuevoGrupo() throws Exception {
        GrupoCreateDTO createDTO = new GrupoCreateDTO("NuevoGrupo");
        Grupo nuevoGrupo = new Grupo("NuevoGrupo");
        when(grupoService.crearGrupo(anyString())).thenReturn(nuevoGrupo);

        mockMvc.perform(post("/api/grupos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("NuevoGrupo"));
    }

    @Test
    void crearGrupo_deberiaRetornarConflictSiGrupoYaExiste() throws Exception {
        GrupoCreateDTO createDTO = new GrupoCreateDTO("GrupoA");
        when(grupoService.crearGrupo(anyString())).thenThrow(new RuntimeException("Error: El grupo con el nombre 'GrupoA' ya existe."));

        mockMvc.perform(post("/api/grupos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarGrupo_deberiaEliminarGrupoExistente() throws Exception {
        doNothing().when(grupoService).deleteGrupo("GrupoA");

        mockMvc.perform(delete("/api/grupos/GrupoA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarGrupo_deberiaRetornarNotFoundParaGrupoInexistente() throws Exception {
        when(grupoService.getGrupoPorNombre(anyString())).thenThrow(new RuntimeException("Grupo no encontrado"));
        doThrow(new RuntimeException("Grupo no encontrado")).when(grupoService).deleteGrupo(anyString());

        mockMvc.perform(delete("/api/grupos/GrupoC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
