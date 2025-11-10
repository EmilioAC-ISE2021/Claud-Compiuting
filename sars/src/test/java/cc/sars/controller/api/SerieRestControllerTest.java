package cc.sars.controller.api;

import cc.sars.config.SecurityConfig;
import cc.sars.model.Serie;
import cc.sars.service.SerieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

@WebMvcTest(controllers = SerieRestController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false) // Deshabilita los filtros de seguridad para este test
public class SerieRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SerieService serieService;

    @Test
    void getAllSeries_shouldReturnListOfSeries() throws Exception {
        // Given
        Serie serie1 = new Serie("Serie A", "Description A");
        Serie serie2 = new Serie("Serie B", "Description B");
        List<Serie> allSeries = Arrays.asList(serie1, serie2);

        when(serieService.buscarTodas()).thenReturn(allSeries);

        // When & Then
        mockMvc.perform(get("/api/series"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombreSerie", is("Serie A")))
                .andExpect(jsonPath("$[0].descripcion", is("Description A")))
                .andExpect(jsonPath("$[1].nombreSerie", is("Serie B")))
                .andExpect(jsonPath("$[1].descripcion", is("Description B")));
    }

    @Test
    void getSerieByNombre_shouldReturnSerie() throws Exception {
        // Given
        Serie serie = new Serie("Serie A", "Description A");
        when(serieService.getSerieByNombre("Serie A")).thenReturn(Optional.of(serie));

        // When & Then
        mockMvc.perform(get("/api/series/Serie A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreSerie", is("Serie A")))
                .andExpect(jsonPath("$.descripcion", is("Description A")));
    }

    @Test
    void getSerieByNombre_shouldReturnNotFound() throws Exception {
        // Given
        when(serieService.getSerieByNombre("NonExistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/series/NonExistent"))
                .andExpect(status().isNotFound());
    }
}
