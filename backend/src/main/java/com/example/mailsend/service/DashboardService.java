package com.example.mailsend.service;

import com.example.mailsend.domain.entity.MailSend;
import com.example.mailsend.dto.response.DashboardResponse;
import com.example.mailsend.repository.MailSendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.mailsend.constants.AppConstants.SEND_STATUS_DONE;
import static com.example.mailsend.constants.AppConstants.SEND_STATUS_PENDING;
import static com.example.mailsend.constants.AppConstants.SEND_STATUS_SENT;

/**
 * ダッシュボード情報を提供するサービス。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    /** 最近の送付履歴の表示件数上限 */
    private static final int RECENT_HISTORY_DISPLAY_LIMIT = 5;

    private final MailSendRepository mailSendRepository;

    /**
     * ダッシュボード表示用の集計データを取得する。
     *
     * @return ダッシュボードレスポンス（サマリー・期限超過月・最近の送付履歴を含む）
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfCurrentMonth = today.withDayOfMonth(1);
        String currentMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        long pending = mailSendRepository.countByStatus(SEND_STATUS_PENDING);
        long sent = mailSendRepository.countByStatus(SEND_STATUS_SENT);
        long done = mailSendRepository.countByStatus(SEND_STATUS_DONE);

        DashboardResponse.Summary summary = DashboardResponse.Summary.builder()
                .pending(pending)
                .sent(sent)
                .done(done)
                .build();

        List<MailSend> overdueList = mailSendRepository.findByStatusAndSendMonthBefore(SEND_STATUS_PENDING, firstDayOfCurrentMonth);
        List<DashboardResponse.OverdueMonth> overdueMonths = overdueList.stream()
                .collect(Collectors.groupingBy(
                        ms -> ms.getSendMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> DashboardResponse.OverdueMonth.builder()
                        .month(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        List<MailSend> sentList = mailSendRepository.findByStatusOrderByUpdatedAtDesc(SEND_STATUS_SENT);
        List<DashboardResponse.RecentHistory> recentHistory = sentList.stream()
                .limit(RECENT_HISTORY_DISPLAY_LIMIT)
                .map(ms -> DashboardResponse.RecentHistory.builder()
                        .id(ms.getId())
                        .officeName(ms.getOffice().getName())
                        .userName(ms.getUser().getName())
                        .sendType(ms.getSendType())
                        .sentAt(ms.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .currentMonth(currentMonth)
                .summary(summary)
                .overdueMonths(overdueMonths)
                .recentHistory(recentHistory)
                .build();
    }
}
