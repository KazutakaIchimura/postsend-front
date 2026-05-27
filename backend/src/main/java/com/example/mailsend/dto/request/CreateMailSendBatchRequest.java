package com.example.mailsend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateMailSendBatchRequest {

    @NotEmpty(message = "送付IDリストは必須です")
    private List<Long> mailSendIds;

    private String notes;
}
