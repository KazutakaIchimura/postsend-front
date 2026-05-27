package com.example.mailsend.dto.response;

import com.example.mailsend.domain.entity.MailSend;
import static com.example.mailsend.constants.AppConstants.SEND_STATUS_PENDING;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MailSendResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long officeId;
    private String officeName;
    private String sendType;
    private LocalDate sendMonth;
    private String status;
    private Boolean isOverdue;
    private Long batchId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MailSendResponse from(MailSend mailSend) {
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        boolean overdue = mailSend.getSendMonth().isBefore(firstDayOfCurrentMonth)
                && SEND_STATUS_PENDING.equals(mailSend.getStatus());

        return MailSendResponse.builder()
                .id(mailSend.getId())
                .userId(mailSend.getUser().getId())
                .userName(mailSend.getUser().getName())
                .officeId(mailSend.getOffice().getId())
                .officeName(mailSend.getOffice().getName())
                .sendType(mailSend.getSendType())
                .sendMonth(mailSend.getSendMonth())
                .status(mailSend.getStatus())
                .isOverdue(overdue)
                .batchId(mailSend.getBatch() != null ? mailSend.getBatch().getId() : null)
                .createdAt(mailSend.getCreatedAt())
                .updatedAt(mailSend.getUpdatedAt())
                .build();
    }
}

