package cc.sars.controller.api;

import cc.sars.model.Grupo;
import cc.sars.model.Role;
import cc.sars.model.User;
import cc.sars.service.UsuarioService;
import cc.sars.controller.api.dto.UserCreateDTO;
import cc.sars.controller.api.dto.UserUpdateDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserRestController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        // Given
        User user1 = new User("user1", "pass", Role.ROLE_USER);
        user1.setGrupos(Set.of(new Grupo("Group A")));
        User user2 = new User("user2", "pass", Role.ROLE_ADMIN);
        user2.setGrupos(Set.of(new Grupo("Group B")));
        List<User> users = Arrays.asList(user1, user2);

        when(usuarioService.getTodosLosUsuarios()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[0].role", is("ROLE_USER")))
                .andExpect(jsonPath("$[0].grupos[0]", is("Group A")))
                .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    void getUserByUsername_shouldReturnUser() throws Exception {
        // Given
        User user = new User("user1", "pass", Role.ROLE_USER);
        user.setGrupos(Set.of(new Grupo("Group A")));
        when(usuarioService.findByUsername("user1")).thenReturn(Optional.of(user));

        // When & Then
        mockMvc.perform(get("/api/usuarios/{username}", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("user1")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));
    }

    @Test
    void getUserByUsername_shouldReturnNotFound() throws Exception {
        // Given
        when(usuarioService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/usuarios/{username}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_shouldCreateAndReturnUser() throws Exception {
        // Given
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("newUser");
        createDTO.setPassword("password");
        createDTO.setRole(Role.ROLE_USER);
        createDTO.setNombreGrupo("Group A");

        User createdUser = new User("newUser", "encodedpass", Role.ROLE_USER);
        createdUser.setGrupos(Set.of(new Grupo("Group A")));

        when(usuarioService.crearUsuario("newUser", "password", Role.ROLE_USER, "Group A")).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("newUser")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")))
                .andExpect(jsonPath("$.grupos[0]", is("Group A")));
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() throws Exception {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setRole(Role.ROLE_ADMIN);
        updateDTO.setNombreGrupo("Group B");

        User updatedUser = new User("user1", "pass", Role.ROLE_ADMIN);
        updatedUser.setGrupos(Set.of(new Grupo("Group B")));

        when(usuarioService.actualizarUsuario("user1", Role.ROLE_ADMIN, "Group B")).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/usuarios/{username}", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("user1")))
                .andExpect(jsonPath("$.role", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$.grupos[0]", is("Group B")));
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(usuarioService).eliminarUsuario("user1");

        // When & Then
        mockMvc.perform(delete("/api/usuarios/{username}", "user1"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).eliminarUsuario("user1");
    }
}