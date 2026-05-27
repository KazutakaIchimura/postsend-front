package com.example.mailsend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "現在のパスワードは必須です")
    private String currentPassword;

    @NotBlank(message = "新しいパスワードは必須です")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    private String newPassword;
}
