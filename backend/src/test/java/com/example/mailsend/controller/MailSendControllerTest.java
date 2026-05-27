package com.example.mailsend.controller;

import static com.example.mailsend.constants.AppConstants.*;
import com.example.mailsend.dto.request.CreateMailSendRequest;
import com.example.mailsend.dto.response.MailSendResponse;
import com.example.mailsend.exception.DuplicateResourceException;
import com.example.mailsend.service.MailSendService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MailSendController.class)
class MailSendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MailSendService mailSendService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private MailSendResponse buildResponse() {
        return MailSendResponse.builder()
                .id(1L).userId(1L).userName("田中太郎")
                .officeId(1L).officeName("GHさくら")
                .sendType(SEND_TYPE_PLAN).sendMonth(LocalDate.of(2025, 5, 1))
                .status(SEND_STATUS_PENDING).isOverdue(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    void getMailSends_returnsOk() throws Exception {
        when(mailSendService.getMailSends(any(), any(), any(), any()))
                .thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/mail-sends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userName").value("田中太郎"));
    }

    @Test
    @WithMockUser
    void createMailSend_returnsCreated() throws Exception {
        CreateMailSendRequest request = new CreateMailSendRequest();
        request.setUserId(1L);
        request.setOfficeId(1L);
        request.setSendType(SEND_TYPE_PLAN);
        request.setSendMonth(LocalDate.of(2025, 5, 1));

        when(mailSendService.createMailSend(any(CreateMailSendRequest.class), anyString()))
                .thenReturn(buildResponse());

        mockMvc.perform(post("/api/mail-sends")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sendType").value("PLAN"));
    }

    @Test
    @WithMockUser
    void createMailSend_duplicate_returnsConflict() throws Exception {
        CreateMailSendRequest request = new CreateMailSendRequest();
        request.setUserId(1L);
        request.setOfficeId(1L);
        request.setSendType(SEND_TYPE_PLAN);
        request.setSendMonth(LocalDate.of(2025, 5, 1));

        when(mailSendService.createMailSend(any(CreateMailSendRequest.class), anyString()))
                .thenThrow(new DuplicateResourceException("重複エラー"));

        mockMvc.perform(post("/api/mail-sends")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void deleteMailSend_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/mail-sends/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}

