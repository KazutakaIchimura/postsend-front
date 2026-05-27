package com.example.mailsend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOfficeRequest {

    @NotBlank(message = "事業所名は必須です")
    @Size(max = 200, message = "事業所名は200文字以内で入力してください")
    private String name;

    @Size(max = 8, message = "郵便番号は8文字以内で入力してください")
    private String postalCode;

    @Size(max = 200, message = "住所は200文字以内で入力してください")
    private String address;

    @Size(max = 200, message = "建物名は200文字以内で入力してください")
    private String building;

    @Size(max = 20, message = "電話番号は20文字以内で入力してください")
    private String phone;
}
