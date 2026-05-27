package com.example.mailsend.repository;

import com.example.mailsend.domain.entity.MailSend;
import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.domain.entity.Staff;
import com.example.mailsend.domain.entity.User;
import static com.example.mailsend.constants.AppConstants.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MailSendRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private MailSendRepository mailSendRepository;

    private Staff staff;
    private User user;
    private Office office;

    @BeforeEach
    void setUp() {
        staff = em.persist(Staff.builder()
                .name("管理者").email("admin@example.com")
                .passwordHash("$2a$10$hash").role(ROLE_ADMIN)
                .isActive(true).forcePasswordChange(false).build());

        user   = em.persist(User.builder().name("田中太郎").isActive(true).build());
        office = em.persist(Office.builder().name("GHさくら").isActive(true).build());
        em.flush();
    }

    private MailSend saveMailSend(String status, LocalDate sendMonth) {
        return em.persist(MailSend.builder()
                .user(user).office(office)
                .sendType(SEND_TYPE_PLAN).sendMonth(sendMonth)
                .status(status).createdBy(staff).build());
    }

    @Test
    void existsByUserIdAndOfficeIdAndSendTypeAndSendMonth_exists_returnsTrue() {
        LocalDate month = LocalDate.of(2025, 5, 1);
        saveMailSend(SEND_STATUS_PENDING, month);
        em.flush();

        assertThat(mailSendRepository.existsByUserIdAndOfficeIdAndSendTypeAndSendMonth(
                user.getId(), office.getId(), SEND_TYPE_PLAN, month)).isTrue();
    }

    @Test
    void existsByUserIdAndOfficeIdAndSendTypeAndSendMonth_notExists_returnsFalse() {
        assertThat(mailSendRepository.existsByUserIdAndOfficeIdAndSendTypeAndSendMonth(
                user.getId(), office.getId(), SEND_TYPE_PLAN, LocalDate.of(2025, 5, 1))).isFalse();
    }

    @Test
    void findByStatus_returnsPendingOnly() {
        saveMailSend(SEND_STATUS_PENDING, LocalDate.of(2025, 5, 1));
        saveMailSend(SEND_STATUS_SENT,    LocalDate.of(2025, 4, 1));
        em.flush();
        em.clear();

        List<MailSend> result = mailSendRepository.findByStatus(SEND_STATUS_PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(SEND_STATUS_PENDING);
    }

    @Test
    void countByStatus_returnsCorrectCount() {
        saveMailSend(SEND_STATUS_PENDING, LocalDate.of(2025, 5, 1));
        saveMailSend(SEND_STATUS_PENDING, LocalDate.of(2025, 4, 1));
        saveMailSend(SEND_STATUS_SENT,    LocalDate.of(2025, 3, 1));
        em.flush();

        assertThat(mailSendRepository.countByStatus(SEND_STATUS_PENDING)).isEqualTo(2);
        assertThat(mailSendRepository.countByStatus(SEND_STATUS_SENT)).isEqualTo(1);
    }

    @Test
    void findByStatusAndSendMonthBefore_returnsOnlyOverdue() {
        LocalDate thisMonth    = LocalDate.now().withDayOfMonth(1);
        LocalDate lastMonth    = thisMonth.minusMonths(1);
        LocalDate twoMonthsAgo = thisMonth.minusMonths(2);

        saveMailSend(SEND_STATUS_PENDING, thisMonth);
        saveMailSend(SEND_STATUS_PENDING, lastMonth);
        saveMailSend(SEND_STATUS_PENDING, twoMonthsAgo);
        saveMailSend(SEND_STATUS_SENT,    lastMonth);
        em.flush();

        List<MailSend> overdue = mailSendRepository.findByStatusAndSendMonthBefore(SEND_STATUS_PENDING, thisMonth);

        assertThat(overdue).hasSize(2);
        assertThat(overdue).allMatch(ms -> ms.getSendMonth().isBefore(thisMonth));
        assertThat(overdue).allMatch(ms -> SEND_STATUS_PENDING.equals(ms.getStatus()));
    }

    @Test
    void findAll_withOfficeIdSpec_returnsFiltered() {
        Office office2 = em.persist(Office.builder().name("GHひまわり").isActive(true).build());
        em.flush();

        saveMailSend(SEND_STATUS_PENDING, LocalDate.of(2025, 5, 1));
        em.persist(MailSend.builder()
                .user(user).office(office2)
                .sendType(SEND_TYPE_MONITORING).sendMonth(LocalDate.of(2025, 5, 1))
                .status(SEND_STATUS_PENDING).createdBy(staff).build());
        em.flush();
        em.clear();

        List<MailSend> result = mailSendRepository.findAll(
                MailSendSpecification.withFilters(null, null, office.getId(), null),
                org.springframework.data.domain.Sort.unsorted());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOffice().getId()).isEqualTo(office.getId());
    }
}

