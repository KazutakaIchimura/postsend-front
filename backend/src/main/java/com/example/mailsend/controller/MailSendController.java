package com.example.mailsend.controller;

import com.example.mailsend.dto.request.CreateMailSendRequest;
import com.example.mailsend.dto.response.MailSendByOfficeResponse;
import com.example.mailsend.dto.response.MailSendResponse;
import com.example.mailsend.service.MailSendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 送付レコード管理のRESTコントローラ。
 */
@RestController
@RequestMapping("/api/mail-sends")
@RequiredArgsConstructor
public class MailSendController {

    private final MailSendService mailSendService;

    /**
     * フィルタ条件に基づいて送付レコード一覧を取得する。
     *
     * @param dateFrom 更新日の下限（任意）
     * @param dateTo   更新日の上限（任意）
     * @param officeId 事業所IDによる絞り込み（任意）
     * @param userId   利用者IDによる絞り込み（任意）
     * @return 送付レコードのレスポンスリスト
     */
    @GetMapping
    public ResponseEntity<List<MailSendResponse>> getMailSends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(mailSendService.getMailSends(dateFrom, dateTo, officeId, userId));
    }

    /**
     * 未送付の送付レコードを事業所ごとにグループ化して取得する。
     *
     * @return 事業所ごとにグループ化された送付レコードのリスト
     */
    @GetMapping("/by-office")
    public ResponseEntity<List<MailSendByOfficeResponse>> getMailSendsByOffice() {
        return ResponseEntity.ok(mailSendService.getMailSendsByOffice());
    }

    /**
     * 送付レコードを新規作成する。
     *
     * @param request     作成リクエスト
     * @param userDetails 認証済みユーザー情報
     * @return 作成された送付レコードのレスポンス（HTTP 201）
     */
    @PostMapping
    public ResponseEntity<MailSendResponse> createMailSend(
            @Valid @RequestBody CreateMailSendRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MailSendResponse response = mailSendService.createMailSend(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 指定IDの送付レコードを削除する。
     *
     * @param id 削除対象の送付レコードID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMailSend(@PathVariable Long id) {
        mailSendService.deleteMailSend(id);
        return ResponseEntity.noContent().build();
    }
}
