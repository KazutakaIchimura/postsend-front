package com.example.mailsend.service;

import com.example.mailsend.domain.entity.MailSend;
import com.example.mailsend.domain.entity.MailSendBatch;
import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.domain.entity.Staff;
import com.example.mailsend.domain.entity.User;
import static com.example.mailsend.constants.AppConstants.*;
import com.example.mailsend.dto.request.CreateMailSendBatchRequest;
import com.example.mailsend.dto.response.MailSendBatchResponse;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.repository.MailSendBatchRepository;
import com.example.mailsend.repository.MailSendRepository;
import com.example.mailsend.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailSendBatchServiceTest {

    @Mock private MailSendBatchRepository mailSendBatchRepository;
    @Mock private MailSendRepository mailSendRepository;
    @Mock private StaffRepository staffRepository;

    @InjectMocks
    private MailSendBatchService mailSendBatchService;

    private Staff staff;
    private User user;
    private Office office;
    private MailSend mailSend1;
    private MailSend mailSend2;

    @BeforeEach
    void setUp() {
        staff = Staff.builder()
                .id(1L).name("管理者").email("admin@example.com")
                .passwordHash("hash").role(ROLE_ADMIN).isActive(true).build();

        user   = User.builder().id(1L).name("田中太郎").isActive(true).build();
        office = Office.builder().id(1L).name("GHさくら").isActive(true).build();

        mailSend1 = MailSend.builder()
                .id(10L).user(user).office(office)
                .sendType(SEND_TYPE_PLAN).sendMonth(LocalDate.of(2025, 5, 1))
                .status(SEND_STATUS_PENDING).createdBy(staff)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        mailSend2 = MailSend.builder()
                .id(11L).user(user).office(office)
                .sendType(SEND_TYPE_MONITORING).sendMonth(LocalDate.of(2025, 5, 1))
                .status(SEND_STATUS_PENDING).createdBy(staff)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    @Test
    void createBatch_success() {
        CreateMailSendBatchRequest request = new CreateMailSendBatchRequest();
        request.setMailSendIds(List.of(10L, 11L));
        request.setNotes("テストバッチ");

        MailSendBatch savedBatch = MailSendBatch.builder()
                .id(1L).sentBy(staff).sentAt(LocalDateTime.now())
                .notes("テストバッチ").createdAt(LocalDateTime.now()).build();

        when(staffRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(staff));
        when(mailSendBatchRepository.save(any(MailSendBatch.class))).thenReturn(savedBatch);
        when(mailSendRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(mailSend1, mailSend2));
        when(mailSendRepository.saveAll(anyList())).thenReturn(List.of(mailSend1, mailSend2));

        MailSendBatchResponse response = mailSendBatchService.createBatch(request, "admin@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getBatchId()).isEqualTo(1L);
        assertThat(response.getUpdatedCount()).isEqualTo(2);
        assertThat(mailSend1.getStatus()).isEqualTo(SEND_STATUS_SENT);
        assertThat(mailSend2.getStatus()).isEqualTo(SEND_STATUS_SENT);
        assertThat(mailSend1.getBatch()).isEqualTo(savedBatch);
        assertThat(mailSend2.getBatch()).isEqualTo(savedBatch);
    }

    @Test
    void createBatch_staffNotFound_throwsResourceNotFoundException() {
        CreateMailSendBatchRequest request = new CreateMailSendBatchRequest();
        request.setMailSendIds(List.of(10L));

        when(staffRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mailSendBatchService.createBatch(request, "unknown@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

