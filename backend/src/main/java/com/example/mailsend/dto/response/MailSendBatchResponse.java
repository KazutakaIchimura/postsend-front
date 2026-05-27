package com.example.mailsend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MailSendBatchResponse {

    private Long batchId;
    private LocalDateTime sentAt;
    private Integer updatedCount;
    private String notes;
}
