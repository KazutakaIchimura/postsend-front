package com.example.mailsend.service;

import com.example.mailsend.domain.entity.MailSend;
import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.domain.entity.Staff;
import com.example.mailsend.domain.entity.User;
import static com.example.mailsend.constants.AppConstants.*;
import com.example.mailsend.dto.response.DashboardResponse;
import com.example.mailsend.repository.MailSendRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private MailSendRepository mailSendRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User user;
    private Office office;
    private Staff staff;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("田中太郎").isActive(true).build();
        office = Office.builder().id(1L).name("GHさくら").isActive(true).build();
        staff = Staff.builder()
                .id(1L).name("管理者").email("admin@example.com")
                .passwordHash("hash").role(ROLE_ADMIN).isActive(true).build();
    }

    @Test
    void getDashboard_returnsSummaryWithCounts() {
        when(mailSendRepository.countByStatus(SEND_STATUS_PENDING)).thenReturn(4L);
        when(mailSendRepository.countByStatus(SEND_STATUS_SENT)).thenReturn(3L);
        when(mailSendRepository.countByStatus(SEND_STATUS_DONE)).thenReturn(0L);
        when(mailSendRepository.findByStatusAndSendMonthBefore(eq(SEND_STATUS_PENDING), any(LocalDate.class)))
                .thenReturn(List.of());
        when(mailSendRepository.findByStatusOrderByUpdatedAtDesc(SEND_STATUS_SENT))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response).isNotNull();
        assertThat(response.getSummary().getPending()).isEqualTo(4L);
        assertThat(response.getSummary().getSent()).isEqualTo(3L);
        assertThat(response.getSummary().getDone()).isEqualTo(0L);
        assertThat(response.getCurrentMonth()).isNotNull();
    }

    @Test
    void getDashboard_overdueMonths_groupedCorrectly() {
        LocalDate lastMonth    = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2).withDayOfMonth(1);

        MailSend overdue1 = MailSend.builder()
                .id(1L).user(user).office(office)
                .sendType(SEND_TYPE_PLAN).sendMonth(lastMonth)
                .status(SEND_STATUS_PENDING).createdBy(staff)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        MailSend overdue2 = MailSend.builder()
                .id(2L).user(user).office(office)
                .sendType(SEND_TYPE_MONITORING).sendMonth(twoMonthsAgo)
                .status(SEND_STATUS_PENDING).createdBy(staff)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(mailSendRepository.countByStatus(any())).thenReturn(0L);
        when(mailSendRepository.findByStatusAndSendMonthBefore(eq(SEND_STATUS_PENDING), any(LocalDate.class)))
                .thenReturn(List.of(overdue1, overdue2));
        when(mailSendRepository.findByStatusOrderByUpdatedAtDesc(SEND_STATUS_SENT))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.getOverdueMonths()).hasSize(2);
    }

    @Test
    void getDashboard_recentHistory_limitedToFive() {
        MailSend sent = MailSend.builder()
                .id(1L).user(user).office(office)
                .sendType(SEND_TYPE_PLAN).sendMonth(LocalDate.now().withDayOfMonth(1))
                .status(SEND_STATUS_SENT).createdBy(staff)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(mailSendRepository.countByStatus(any())).thenReturn(0L);
        when(mailSendRepository.findByStatusAndSendMonthBefore(eq(SEND_STATUS_PENDING), any(LocalDate.class)))
                .thenReturn(List.of());
        when(mailSendRepository.findByStatusOrderByUpdatedAtDesc(SEND_STATUS_SENT))
                .thenReturn(List.of(sent, sent, sent, sent, sent, sent));

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.getRecentHistory()).hasSize(5);
    }
}

