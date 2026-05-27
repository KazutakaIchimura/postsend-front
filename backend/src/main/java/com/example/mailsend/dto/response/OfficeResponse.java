package com.example.mailsend.dto.response;

import com.example.mailsend.domain.entity.Office;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OfficeResponse {

    private Long id;
    private String name;
    private String postalCode;
    private String address;
    private String building;
    private String phone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OfficeResponse from(Office office) {
        return OfficeResponse.builder()
                .id(office.getId())
                .name(office.getName())
                .postalCode(office.getPostalCode())
                .address(office.getAddress())
                .building(office.getBuilding())
                .phone(office.getPhone())
                .isActive(office.getIsActive())
                .createdAt(office.getCreatedAt())
                .updatedAt(office.getUpdatedAt())
                .build();
    }
}
