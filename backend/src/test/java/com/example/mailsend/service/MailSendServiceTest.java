package com.example.mailsend.service;

import com.example.mailsend.domain.entity.MailSend;
import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.domain.entity.Staff;
import com.example.mailsend.domain.entity.User;
import static com.example.mailsend.constants.AppConstants.*;
import com.example.mailsend.dto.request.CreateMailSendRequest;
import com.example.mailsend.dto.response.MailSendResponse;
import com.example.mailsend.exception.DuplicateResourceException;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.repository.MailSendRepository;
import com.example.mailsend.repository.OfficeRepository;
import com.example.mailsend.repository.StaffRepository;
import com.example.mailsend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailSendServiceTest {

    @Mock private MailSendRepository mailSendRepository;
    @Mock private UserRepository userRepository;
    @Mock private OfficeRepository officeRepository;
    @Mock private StaffRepository staffRepository;

    @InjectMocks
    private MailSendService mailSendService;

    private User user;
    private Office office;
    private Staff staff;

    @BeforeEach
    void setUp() {
        user  = User.builder().id(1L).name("田中太郎").isActive(true).build();
        office = Office.builder().id(1L).name("GHさくら").isActive(true).build();
        staff = Staff.builder()
                .id(1L).name("管理者").email("admin@example.com")
                .passwordHash("hash").role(ROLE_ADMIN).isActive(true).build();
    }

    @Test
    void createMailSend_success() {
        CreateMailSendRequest request = new CreateMailSendRequest();
        request.setUserId(1L);
        request.setOfficeId(1L);
        request.setSendType(SEND_TYPE_PLAN);
        request.setSendMonth(LocalDate.of(2025, 5, 15));

        LocalDate expectedMonth = LocalDate.of(2025, 5, 1);

        when(mailSendRepository.existsByUserIdAndOfficeIdAndSendTypeAndSendMonth(
                1L, 1L, SEND_TYPE_PLAN, expectedMonth)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(staffRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(staff));

        MailSend saved = MailSend.builder()
                .id(1L).user(user).office(office)
                .sendType(SEND_TYPE_PLAN).sendMonth(expectedMonth)
                .status(SEND_STATUS_PENDING).createdBy(staff)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(mailSendRepository.save(any(MailSend.class))).thenReturn(saved);

        MailSendResponse response = mailSendService.createMailSend(request, "admin@example.com");

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getSendType()).isEqualTo(SEND_TYPE_PLAN);
        assertThat(response.getSendMonth()).isEqualTo(expectedMonth);
        assertThat(response.getStatus()).isEqualTo(SEND_STATUS_PENDING);
        verify(mailSendRepository).save(any(MailSend.class));
    }

    @Test
    void createMailSend_sendMonthNormalized_toFirstDay() {
        CreateMailSendRequest request = new CreateMailSendRequest();
        request.setUserId(1L);
        request.setOfficeId(1L);
        request.setSendType(SEND_TYPE_PLAN);
        request.setSendMonth(LocalDate.of(2025, 5, 20));

        LocalDate firstDay = LocalDate.of(2025, 5, 1);

        when(mailSendRepository.existsByUserIdAndOfficeIdAndSendTypeAndSendMonth(
                eq(1L), eq(1L), eq(SEND_TYPE_PLAN), eq(firstDay))).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(office));
        when(staffRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(staff));

        MailSend saved = MailSend.builder()
                .id(1L).user(user).office(office)
                .sendType(SEND_TYPE_PLAN).sendMonth(firstDay)
                .status(SEND_STATUS_PENDING).createdBy(staff)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(mailSendRepository.save(any(MailSend.class))).thenReturn(saved);

        MailSendResponse response = mailSendService.createMailSend(request, "admin@example.com");

        assertThat(response.getSendMonth()).isEqualTo(firstDay);
    }

    @Test
    void createMailSend_duplicate_throwsDuplicateResourceException() {
        CreateMailSendRequest request = new CreateMailSendRequest();
        request.setUserId(1L);
        request.setOfficeId(1L);
        request.setSendType(SEND_TYPE_PLAN);
        request.setSendMonth(LocalDate.of(2025, 5, 1));

        when(mailSendRepository.existsByUserIdAndOfficeIdAndSendTypeAndSendMonth(
                1L, 1L, SEND_TYPE_PLAN, LocalDate.of(2025, 5, 1))).thenReturn(true);

        assertThatThrownBy(() -> mailSendService.createMailSend(request, "admin@example.com"))
                .isInstanceOf(DuplicateResourceException.class);
        verify(mailSendRepository, never()).save(any());
    }

    @Test
    void createMailSend_userNotFound_throwsResourceNotFoundException() {
        CreateMailSendRequest request = new CreateMailSendRequest();
        request.setUserId(999L);
        request.setOfficeId(1L);
        request.setSendType(SEND_TYPE_PLAN);
        request.setSendMonth(LocalDate.of(2025, 5, 1));

        when(mailSendRepository.existsByUserIdAndOfficeIdAndSendTypeAndSendMonth(
                anyLong(), anyLong(), any(), any())).thenReturn(false);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mailSendService.createMailSend(request, "admin@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteMailSend_success() {
        MailSend mailSend = MailSend.builder()
                .id(1L).user(user).office(office)
                .sendType(SEND_TYPE_PLAN).sendMonth(LocalDate.of(2025, 5, 1))
                .status(SEND_STATUS_PENDING).createdBy(staff).build();

        when(mailSendRepository.findById(1L)).thenReturn(Optional.of(mailSend));
        mailSendService.deleteMailSend(1L);
        verify(mailSendRepository).delete(mailSend);
    }

    @Test
    void deleteMailSend_notFound_throwsResourceNotFoundException() {
        when(mailSendRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mailSendService.deleteMailSend(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

