package com.example.mailsend.controller;

import com.example.mailsend.dto.request.CreateMailSendBatchRequest;
import com.example.mailsend.dto.response.MailSendBatchResponse;
import com.example.mailsend.service.MailSendBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 送付バッチ（一括送付済み処理）のRESTコントローラ。
 */
@RestController
@RequestMapping("/api/mail-send-batches")
@RequiredArgsConstructor
public class MailSendBatchController {

    private final MailSendBatchService mailSendBatchService;

    /**
     * 送付バッチを作成し、指定された送付レコードを一括で送付済みに更新する。
     *
     * @param request     バッチ作成リクエスト
     * @param userDetails 認証済みユーザー情報
     * @return 作成されたバッチのレスポンス（HTTP 201）
     */
    @PostMapping
    public ResponseEntity<MailSendBatchResponse> createBatch(
            @Valid @RequestBody CreateMailSendBatchRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MailSendBatchResponse response = mailSendBatchService.createBatch(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 指定IDの送付バッチ情報を取得する。
     *
     * @param id バッチID
     * @return 送付バッチのレスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<MailSendBatchResponse> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(mailSendBatchService.getBatchById(id));
    }
}
