package com.example.mailsend.controller;

import com.example.mailsend.dto.request.ChangePasswordRequest;
import com.example.mailsend.dto.response.StaffResponse;
import com.example.mailsend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認証・パスワード管理のRESTコントローラ。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 現在ログイン中のスタッフ情報を取得する。
     *
     * @param userDetails 認証済みユーザー情報
     * @return ログイン中のスタッフのレスポンス
     */
    @GetMapping("/me")
    public ResponseEntity<StaffResponse> getCurrentStaff(
            @AuthenticationPrincipal UserDetails userDetails) {
        StaffResponse response = authService.getCurrentStaff(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * ログイン中のスタッフのパスワードを変更する。
     *
     * @param userDetails 認証済みユーザー情報
     * @param request     パスワード変更リクエスト
     * @return HTTP 200 OK
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}
