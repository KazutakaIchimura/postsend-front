package com.example.mailsend.controller;

import com.example.mailsend.dto.request.CreateUserRequest;
import com.example.mailsend.dto.request.UpdateUserRequest;
import com.example.mailsend.dto.response.OfficeResponse;
import com.example.mailsend.dto.response.UserResponse;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private UserResponse buildUserResponse() {
        return UserResponse.builder()
                .id(1L)
                .name("田中太郎")
                .nameKana("たなかたろう")
                .birthDate(LocalDate.of(1960, 4, 15))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    void getAllUsers_returnsOk() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(buildUserResponse()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("田中太郎"));
    }

    @Test
    @WithMockUser
    void getUserById_found_returnsOk() throws Exception {
        UserResponse response = buildUserResponse();
        response.setOffices(List.of(
                OfficeResponse.builder().id(1L).name("GHさくら").isActive(true).build()
        ));
        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("田中太郎"));
    }

    @Test
    @WithMockUser
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("利用者", 99L));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createUser_returnsCreated() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("田中太郎");
        request.setNameKana("たなかたろう");
        request.setBirthDate(LocalDate.of(1960, 4, 15));

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(buildUserResponse());

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void updateUser_returnsOk() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("田中太郎（更新）");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(buildUserResponse());

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteUser_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("利用者", 99L))
                .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getUserOffices_returnsOk() throws Exception {
        OfficeResponse office = OfficeResponse.builder()
                .id(1L).name("GHさくら").isActive(true).build();
        when(userService.getUserOffices(1L)).thenReturn(List.of(office));

        mockMvc.perform(get("/api/users/1/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("GHさくら"));
    }

    @Test
    @WithMockUser
    void addUserOffice_returnsCreated() throws Exception {
        OfficeResponse office = OfficeResponse.builder()
                .id(1L).name("GHさくら").isActive(true).build();
        when(userService.addUserOffice(eq(1L), eq(1L))).thenReturn(office);

        mockMvc.perform(post("/api/users/1/offices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("officeId", 1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void removeUserOffice_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1/offices/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
