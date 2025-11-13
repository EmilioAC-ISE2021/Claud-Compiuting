package cc.sars.controller.api;

import cc.sars.controller.api.dto.UserRoleUpdateDTO;
import cc.sars.model.Role;
import cc.sars.service.GrupoService;
import cc.sars.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioGrupoRestController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsuarioGrupoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private GrupoService grupoService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String USERNAME = "testUser";
    private final String GROUP_NAME = "testGroup";

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(usuarioService, grupoService);
    }

    @Test
    void testAgregarUsuarioAGrupo_Success() throws Exception {
        doNothing().when(grupoService).agregarUsuarioAGrupo(USERNAME, GROUP_NAME);

        mockMvc.perform(post("/api/usuarios/{username}/grupos/{nombreGrupo}", USERNAME, GROUP_NAME))
                .andExpect(status().isOk());

        verify(grupoService, times(1)).agregarUsuarioAGrupo(USERNAME, GROUP_NAME);
    }

    @Test
    void testAgregarUsuarioAGrupo_BadRequest() throws Exception {
        doThrow(new RuntimeException("Error adding user to group")).when(grupoService).agregarUsuarioAGrupo(USERNAME, GROUP_NAME);

        mockMvc.perform(post("/api/usuarios/{username}/grupos/{nombreGrupo}", USERNAME, GROUP_NAME))
                .andExpect(status().isBadRequest());

        verify(grupoService, times(1)).agregarUsuarioAGrupo(USERNAME, GROUP_NAME);
    }

    @Test
    void testEliminarUsuarioDeGrupo_Success() throws Exception {
        doNothing().when(grupoService).eliminarUsuarioDeGrupo(GROUP_NAME, USERNAME);

        mockMvc.perform(delete("/api/usuarios/{username}/grupos/{nombreGrupo}", USERNAME, GROUP_NAME))
                .andExpect(status().isNoContent());

        verify(grupoService, times(1)).eliminarUsuarioDeGrupo(GROUP_NAME, USERNAME);
    }

    @Test
    void testEliminarUsuarioDeGrupo_BadRequest() throws Exception {
        doThrow(new RuntimeException("Error removing user from group")).when(grupoService).eliminarUsuarioDeGrupo(GROUP_NAME, USERNAME);

        mockMvc.perform(delete("/api/usuarios/{username}/grupos/{nombreGrupo}", USERNAME, GROUP_NAME))
                .andExpect(status().isBadRequest());

        verify(grupoService, times(1)).eliminarUsuarioDeGrupo(GROUP_NAME, USERNAME);
    }

    @Test
    void testCambiarRolUsuarioEnGrupo_Success() throws Exception {
        UserRoleUpdateDTO dto = new UserRoleUpdateDTO();
        dto.setRol(Role.ROLE_LIDER);

        doNothing().when(usuarioService).changeUserRole(USERNAME, Role.ROLE_LIDER, GROUP_NAME);

        mockMvc.perform(put("/api/usuarios/{username}/grupos/{nombreGrupo}/rol", USERNAME, GROUP_NAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).changeUserRole(USERNAME, Role.ROLE_LIDER, GROUP_NAME);
    }

    @Test
    void testCambiarRolUsuarioEnGrupo_BadRequest() throws Exception {
        UserRoleUpdateDTO dto = new UserRoleUpdateDTO();
        dto.setRol(Role.ROLE_USER);

        doThrow(new RuntimeException("Error changing user role")).when(usuarioService).changeUserRole(USERNAME, Role.ROLE_USER, GROUP_NAME);

        mockMvc.perform(put("/api/usuarios/{username}/grupos/{nombreGrupo}/rol", USERNAME, GROUP_NAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, times(1)).changeUserRole(USERNAME, Role.ROLE_USER, GROUP_NAME);
    }
}
