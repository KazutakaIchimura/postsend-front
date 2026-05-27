package com.example.mailsend.dto.response;

import com.example.mailsend.domain.entity.Staff;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StaffResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private Boolean isActive;
    private Boolean forcePasswordChange;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StaffResponse from(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .name(staff.getName())
                .email(staff.getEmail())
                .role(staff.getRole())
                .isActive(staff.getIsActive())
                .forcePasswordChange(staff.getForcePasswordChange())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
