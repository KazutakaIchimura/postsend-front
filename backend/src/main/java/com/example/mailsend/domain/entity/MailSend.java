package com.example.mailsend.domain.entity;

import static com.example.mailsend.constants.AppConstants.SEND_STATUS_PENDING;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mail_sends")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailSend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "send_type", nullable = false, length = 20)
    private String sendType;

    @Column(name = "send_month", nullable = false)
    private LocalDate sendMonth;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = SEND_STATUS_PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private MailSendBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Staff createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

