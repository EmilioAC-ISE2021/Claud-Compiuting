package cc.sars.controller.api;

import cc.sars.controller.api.dto.CapituloCreateDTO;
import cc.sars.controller.api.dto.CapituloBulkCreateDTO;
import cc.sars.controller.api.dto.TareaCreateDTO;
import cc.sars.model.Capitulo;
import cc.sars.model.Serie;
import cc.sars.model.Tarea;
import cc.sars.model.EstadosTareas;
import cc.sars.service.SerieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CapituloRestController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class CapituloRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SerieService serieService;

    @MockBean
    private cc.sars.service.UsuarioService usuarioService;

    private static final String TEST_GROUP = "TestGroup";



    @Test
    void getCapitulosBySerie_shouldReturnListOfCapitulos() throws Exception {
        // Given
        String nombreSerie = "SerieTest";
        Serie serie = new Serie(nombreSerie, "Description Test");

        Tarea tarea1 = new Tarea("Tarea1");
        tarea1.setEstadoTarea(EstadosTareas.NoAsignado);
        tarea1.setUsuarioAsignado("NADIE");

        Capitulo capitulo1 = new Capitulo("Capitulo1");
        capitulo1.anyadirTarea(tarea1);
        capitulo1.setSerie(serie);

        Capitulo capitulo2 = new Capitulo("Capitulo2");
        capitulo2.setSerie(serie);

        serie.addCapitulo(capitulo1);
        serie.addCapitulo(capitulo2);

        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, nombreSerie)).thenReturn(Optional.of(serie));

        // When & Then
        mockMvc.perform(get("/api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos", TEST_GROUP, nombreSerie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombreCapitulo", is("Capitulo1")))
                .andExpect(jsonPath("$[0].nombreSerie", is(nombreSerie)))
                .andExpect(jsonPath("$[0].tareas", hasSize(1)))
                .andExpect(jsonPath("$[0].tareas[0].nombre", is("Tarea1")))
                .andExpect(jsonPath("$[1].nombreCapitulo", is("Capitulo2")))
                .andExpect(jsonPath("$[1].nombreSerie", is(nombreSerie)))
                .andExpect(jsonPath("$[1].tareas", hasSize(0)));
    }

    @Test
    void getCapituloByNombre_shouldReturnCapitulo() throws Exception {
        // Given
        String nombreSerie = "SerieTest";
        String nombreCapitulo = "Capitulo1";
        Serie serie = new Serie(nombreSerie, "Description Test");

        Tarea tarea1 = new Tarea("Tarea1");
        tarea1.setEstadoTarea(EstadosTareas.NoAsignado);
        tarea1.setUsuarioAsignado("NADIE");

        Capitulo capitulo = new Capitulo(nombreCapitulo);
        capitulo.anyadirTarea(tarea1);
        capitulo.setSerie(serie);
        serie.addCapitulo(capitulo);

        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, nombreSerie)).thenReturn(Optional.of(serie));

        // When & Then
        mockMvc.perform(get("/api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}", TEST_GROUP, nombreSerie, nombreCapitulo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCapitulo", is(nombreCapitulo)))
                .andExpect(jsonPath("$.nombreSerie", is(nombreSerie)))
                .andExpect(jsonPath("$.tareas", hasSize(1)))
                .andExpect(jsonPath("$.tareas[0].nombre", is("Tarea1")));
    }

    @Test
    void getCapituloByNombre_shouldReturnNotFound() throws Exception {
        // Given
        String nombreSerie = "SerieTest";
        String nombreCapitulo = "NonExistentCapitulo";
        Serie serie = new Serie(nombreSerie, "Description Test");
        serie.addCapitulo(new Capitulo("ExistingCapitulo")); // Añadir uno existente para asegurar que el filtro funciona

        when(serieService.getSerieByNombreAndGrupo(TEST_GROUP, nombreSerie)).thenReturn(Optional.of(serie));

        // When & Then
        mockMvc.perform(get("/api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}", TEST_GROUP, nombreSerie, nombreCapitulo))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCapitulo_shouldCreateCapitulo() throws Exception {
        // Given
        String nombreSerie = "SerieTest";
        String nombreCapitulo = "NewCapitulo";
        CapituloCreateDTO capituloCreateDTO = new CapituloCreateDTO();
        capituloCreateDTO.setNombreCapitulo(nombreCapitulo);

        Serie serie = new Serie(nombreSerie, "Description Test");
        Capitulo newCapitulo = new Capitulo(nombreCapitulo);
        newCapitulo.setSerie(serie);
        newCapitulo.anyadirTarea(new Tarea("CC")); // Add the default task to the mock
        serie.addCapitulo(newCapitulo); // Add to the series that will be returned by the service

        when(serieService.addCapituloToSerie(TEST_GROUP, nombreSerie, nombreCapitulo)).thenReturn(serie);

        // When & Then
        mockMvc.perform(post("/api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos", TEST_GROUP, nombreSerie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(capituloCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreCapitulo", is(nombreCapitulo)))
                .andExpect(jsonPath("$.nombreSerie", is(nombreSerie)))
                .andExpect(jsonPath("$.tareas", hasSize(1))); // Now it has the default 'CC' task
    }

    @Test
    void createBulkCapitulos_shouldCreateMultipleCapitulos() throws Exception {
        // Given
        String nombreSerie = "SerieTest";
        String nombresCapitulos = "CapituloBulk1\nCapituloBulk2";
        
        TareaCreateDTO tarea1 = new TareaCreateDTO("TareaBulk1", EstadosTareas.Asignado, "user1");
        TareaCreateDTO tarea2 = new TareaCreateDTO("TareaBulk2", EstadosTareas.NoAsignado, "NADIE");
        List<TareaCreateDTO> tareasEnMasa = Arrays.asList(tarea1, tarea2);

        CapituloBulkCreateDTO bulkCreateDTO = new CapituloBulkCreateDTO();
        bulkCreateDTO.setNombresCapitulos(nombresCapitulos);
        bulkCreateDTO.setTareasEnMasa(tareasEnMasa);

        Serie serie = new Serie(nombreSerie, "Description Test");
        Capitulo capituloBulk1 = new Capitulo("CapituloBulk1");
        capituloBulk1.anyadirTarea(new Tarea("TareaBulk1")); // Simplified for mock
        capituloBulk1.setSerie(serie);
        Capitulo capituloBulk2 = new Capitulo("CapituloBulk2");
        capituloBulk2.anyadirTarea(new Tarea("TareaBulk2")); // Simplified for mock
        capituloBulk2.setSerie(serie);
        serie.addCapitulo(capituloBulk1);
        serie.addCapitulo(capituloBulk2);

        String[] tareasEnMasaArray = tareasEnMasa.stream()
                .map(tareaDTO -> String.format("%s###%s###%s",
                        tareaDTO.getNombre(),
                        tareaDTO.getEstadoTarea() != null ? tareaDTO.getEstadoTarea().name() : "NoAsignado",
                        tareaDTO.getUsuarioAsignado() != null ? tareaDTO.getUsuarioAsignado() : "NADIE"))
                .toArray(String[]::new);

        when(serieService.addCapitulosToSerie(eq(TEST_GROUP), eq(nombreSerie), eq(nombresCapitulos), any(String[].class))).thenReturn(serie);

        // When & Then
        mockMvc.perform(post("/api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/bulk", TEST_GROUP, nombreSerie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombreCapitulo", is("CapituloBulk1")))
                .andExpect(jsonPath("$[0].nombreSerie", is(nombreSerie)))
                .andExpect(jsonPath("$[0].tareas", hasSize(1)))
                .andExpect(jsonPath("$[0].tareas[0].nombre", is("TareaBulk1")))
                .andExpect(jsonPath("$[1].nombreCapitulo", is("CapituloBulk2")))
                .andExpect(jsonPath("$[1].nombreSerie", is(nombreSerie)))
                .andExpect(jsonPath("$[1].tareas", hasSize(1)))
                .andExpect(jsonPath("$[1].tareas[0].nombre", is("TareaBulk2")));
    }

    @Test
    void deleteCapitulo_shouldDeleteCapitulo() throws Exception {
        // Given
        String nombreSerie = "SerieTest";
        String nombreCapitulo = "CapituloToDelete";
        doNothing().when(serieService).deleteCapitulo(TEST_GROUP, nombreSerie, nombreCapitulo);

        // When & Then
        mockMvc.perform(delete("/api/grupos/{nombreGrupo}/series/{nombreSerie}/capitulos/{nombreCapitulo}", TEST_GROUP, nombreSerie, nombreCapitulo))
                .andExpect(status().isNoContent());

        verify(serieService).deleteCapitulo(TEST_GROUP, nombreSerie, nombreCapitulo);
    }
}
