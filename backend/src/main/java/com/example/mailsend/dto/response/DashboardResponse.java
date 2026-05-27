package com.example.mailsend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private String currentMonth;
    private Summary summary;
    private List<OverdueMonth> overdueMonths;
    private List<RecentHistory> recentHistory;

    @Data
    @Builder
    public static class Summary {
        private long pending;
        private long sent;
        private long done;
    }

    @Data
    @Builder
    public static class OverdueMonth {
        private String month;
        private long count;
    }

    @Data
    @Builder
    public static class RecentHistory {
        private Long id;
        private String officeName;
        private String userName;
        private String sendType;
        private LocalDateTime sentAt;
    }
}
