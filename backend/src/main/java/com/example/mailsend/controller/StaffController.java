package com.example.mailsend.controller;

import com.example.mailsend.dto.request.CreateStaffRequest;
import com.example.mailsend.dto.request.UpdateStaffRequest;
import com.example.mailsend.dto.response.StaffResponse;
import com.example.mailsend.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * スタッフ管理のRESTコントローラ。
 */
@RestController
@RequestMapping("/api/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    /**
     * 全スタッフの一覧を取得する。
     *
     * @return スタッフのレスポンスリスト
     */
    @GetMapping
    public ResponseEntity<List<StaffResponse>> getAllStaffs() {
        return ResponseEntity.ok(staffService.getAllStaffs());
    }

    /**
     * 新規スタッフを作成する。
     *
     * @param request 作成リクエスト
     * @return 作成されたスタッフのレスポンス（HTTP 201）
     */
    @PostMapping
    public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        StaffResponse response = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 指定IDのスタッフ情報を取得する。
     *
     * @param id スタッフID
     * @return スタッフのレスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> getStaffById(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getStaffById(id));
    }

    /**
     * 指定IDのスタッフ情報を更新する。
     *
     * @param id      スタッフID
     * @param request 更新リクエスト
     * @return 更新後のスタッフのレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<StaffResponse> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request) {
        return ResponseEntity.ok(staffService.updateStaff(id, request));
    }

    /**
     * 指定IDのスタッフを無効化する。
     *
     * @param id スタッフID
     * @return 無効化後のスタッフのレスポンス
     */
    @PatchMapping("/{id}/disable")
    public ResponseEntity<StaffResponse> disableStaff(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.disableStaff(id));
    }
}
