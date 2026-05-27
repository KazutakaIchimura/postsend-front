package com.example.mailsend.repository;

import com.example.mailsend.domain.entity.MailSend;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MailSendRepository extends JpaRepository<MailSend, Long>, JpaSpecificationExecutor<MailSend> {

    boolean existsByUserIdAndOfficeIdAndSendTypeAndSendMonth(
            Long userId, Long officeId, String sendType, LocalDate sendMonth);

    @EntityGraph(attributePaths = {"user", "office"})
    List<MailSend> findByStatus(String status);

    @EntityGraph(attributePaths = {"user", "office"})
    List<MailSend> findByStatusOrderByUpdatedAtDesc(String status);

    @EntityGraph(attributePaths = {"user", "office"})
    List<MailSend> findByStatusAndSendMonthBefore(String status, LocalDate date);

    long countByStatus(String status);

    long countByBatchId(Long batchId);

    @Override
    @EntityGraph(attributePaths = {"user", "office"})
    List<MailSend> findAll(Specification<MailSend> spec, Sort sort);
}
