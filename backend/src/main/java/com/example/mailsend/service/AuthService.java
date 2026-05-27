package com.example.mailsend.service;

import com.example.mailsend.domain.entity.Staff;
import com.example.mailsend.dto.request.ChangePasswordRequest;
import com.example.mailsend.dto.response.StaffResponse;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 認証・パスワード変更に関するビジネスロジックを提供するサービス。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 指定メールアドレスのスタッフのパスワードを変更する。
     *
     * @param email   対象スタッフのメールアドレス
     * @param request パスワード変更リクエスト（現在のパスワードと新しいパスワード）
     * @throws ResourceNotFoundException 対象スタッフが見つからない場合
     * @throws IllegalArgumentException  現在のパスワードが一致しない場合
     */
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("スタッフ", 0L));

        if (!passwordEncoder.matches(request.getCurrentPassword(), staff.getPasswordHash())) {
            throw new IllegalArgumentException("現在のパスワードが正しくありません");
        }

        staff.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        staff.setForcePasswordChange(false);
        staffRepository.save(staff);
    }

    /**
     * 指定メールアドレスに対応する現在ログイン中のスタッフ情報を取得する。
     *
     * @param email スタッフのメールアドレス
     * @return スタッフのレスポンス
     * @throws ResourceNotFoundException スタッフが見つからない場合
     */
    public StaffResponse getCurrentStaff(String email) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("スタッフが見つかりません"));
        return StaffResponse.from(staff);
    }
}
