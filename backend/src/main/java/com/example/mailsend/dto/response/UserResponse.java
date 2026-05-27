package com.example.mailsend.dto.response;

import com.example.mailsend.domain.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String nameKana;
    private LocalDate birthDate;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OfficeResponse> offices;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .nameKana(user.getNameKana())
                .birthDate(user.getBirthDate())
                .notes(user.getNotes())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static UserResponse fromWithOffices(User user, List<OfficeResponse> offices) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .nameKana(user.getNameKana())
                .birthDate(user.getBirthDate())
                .notes(user.getNotes())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .offices(offices)
                .build();
    }
}
