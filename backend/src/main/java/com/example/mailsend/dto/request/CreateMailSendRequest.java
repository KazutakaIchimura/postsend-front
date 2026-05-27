package com.example.mailsend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateMailSendRequest {

    @NotNull(message = "利用者IDは必須です")
    private Long userId;

    @NotNull(message = "事業所IDは必須です")
    private Long officeId;

    @NotBlank(message = "送付種別は必須です")
    private String sendType;

    @NotNull(message = "送付月は必須です")
    private LocalDate sendMonth;
}
