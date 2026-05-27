package com.example.mailsend.service;

import com.example.mailsend.domain.entity.Staff;
import com.example.mailsend.dto.request.CreateStaffRequest;
import com.example.mailsend.dto.request.UpdateStaffRequest;
import com.example.mailsend.dto.response.StaffResponse;
import com.example.mailsend.exception.DuplicateResourceException;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.mailsend.constants.AppConstants.ROLE_ADMIN;

/**
 * スタッフ管理に関するビジネスロジックを提供するサービス。
 */
@Service
@RequiredArgsConstructor
public class StaffService {

    /** 新規スタッフの初期パスワード */
    private static final String INITIAL_PASSWORD = "changeme";

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 全スタッフ一覧を取得する。
     *
     * @return スタッフのレスポンスリスト
     */
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaffs() {
        return staffRepository.findAll().stream()
                .map(StaffResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 指定IDのスタッフ情報を取得する。
     *
     * @param id スタッフID
     * @return スタッフのレスポンス
     * @throws ResourceNotFoundException スタッフが見つからない場合
     */
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("スタッフ", id));
        return StaffResponse.from(staff);
    }

    /**
     * 新規スタッフを作成する。初期パスワードは固定値で、次回ログイン時に変更を強制する。
     *
     * @param request 作成リクエスト
     * @return 作成されたスタッフのレスポンス
     * @throws DuplicateResourceException メールアドレスがすでに登録済みの場合
     */
    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request) {
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("このメールアドレスはすでに使用されています: " + request.getEmail());
        }

        Staff staff = Staff.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(INITIAL_PASSWORD))
                .role(request.getRole())
                .isActive(true)
                .forcePasswordChange(true)
                .build();

        Staff saved = staffRepository.save(staff);
        return StaffResponse.from(saved);
    }

    /**
     * 既存スタッフの情報を更新する。
     *
     * @param id      更新対象のスタッフID
     * @param request 更新リクエスト
     * @return 更新後のスタッフのレスポンス
     * @throws ResourceNotFoundException  スタッフが見つからない場合
     * @throws DuplicateResourceException 変更後のメールアドレスがすでに使用中の場合
     */
    @Transactional
    public StaffResponse updateStaff(Long id, UpdateStaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("スタッフ", id));

        if (!staff.getEmail().equals(request.getEmail())
                && staffRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("このメールアドレスはすでに使用されています: " + request.getEmail());
        }

        staff.setName(request.getName());
        staff.setEmail(request.getEmail());
        staff.setRole(request.getRole());
        staff.setIsActive(request.getIsActive());

        Staff saved = staffRepository.save(staff);
        return StaffResponse.from(saved);
    }

    /**
     * 指定IDのスタッフを無効化する。
     * 自己無効化や最後のADMINの無効化は不可。
     *
     * @param id 無効化対象のスタッフID
     * @return 無効化後のスタッフのレスポンス
     * @throws ResourceNotFoundException スタッフが見つからない場合
     * @throws IllegalStateException     最低1名のADMINが必要なため無効化できない場合
     */
    @Transactional
    public StaffResponse disableStaff(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("スタッフ", id));

        long activeAdminCount = staffRepository.findAll().stream()
                .filter(s -> ROLE_ADMIN.equals(s.getRole()) && Boolean.TRUE.equals(s.getIsActive()))
                .count();

        if (ROLE_ADMIN.equals(staff.getRole()) && activeAdminCount <= 1) {
            throw new IllegalStateException("最低1名のADMINが必要なため、無効化できません");
        }

        staff.setIsActive(false);
        Staff saved = staffRepository.save(staff);
        return StaffResponse.from(saved);
    }
}
