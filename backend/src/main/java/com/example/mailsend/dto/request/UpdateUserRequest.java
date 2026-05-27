package com.example.mailsend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "氏名は必須です")
    @Size(max = 100, message = "氏名は100文字以内で入力してください")
    private String name;

    @Size(max = 100, message = "フリガナは100文字以内で入力してください")
    private String nameKana;

    private LocalDate birthDate;

    private String notes;
}
