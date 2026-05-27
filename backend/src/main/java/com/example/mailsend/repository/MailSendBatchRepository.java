package com.example.mailsend.repository;

import com.example.mailsend.domain.entity.MailSendBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailSendBatchRepository extends JpaRepository<MailSendBatch, Long> {
}
