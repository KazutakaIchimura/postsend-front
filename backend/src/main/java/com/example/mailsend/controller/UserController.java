package com.example.mailsend.controller;

import com.example.mailsend.dto.request.CreateUserRequest;
import com.example.mailsend.dto.request.UpdateUserRequest;
import com.example.mailsend.dto.response.OfficeResponse;
import com.example.mailsend.dto.response.UserResponse;
import com.example.mailsend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 利用者管理のRESTコントローラ。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 利用者の一覧を取得する。
     *
     * @param includeInactive true の場合は無効ユーザーも含める（デフォルト: false）
     * @return 利用者のレスポンスリスト
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(userService.getAllUsers(includeInactive));
    }

    /**
     * 新規利用者を作成する。
     *
     * @param request 作成リクエスト
     * @return 作成された利用者のレスポンス（HTTP 201）
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 指定IDの利用者情報を取得する。
     *
     * @param id 利用者ID
     * @return 利用者のレスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * 指定IDの利用者情報を更新する。
     *
     * @param id      利用者ID
     * @param request 更新リクエスト
     * @return 更新後の利用者のレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * 指定IDの利用者を論理削除する。
     *
     * @param id 利用者ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定IDの利用者を有効化する。
     *
     * @param id 利用者ID
     * @return HTTP 204 No Content
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定利用者に紐付く事業所一覧を取得する。
     *
     * @param userId 利用者ID
     * @return 事業所のレスポンスリスト
     */
    @GetMapping("/{userId}/offices")
    public ResponseEntity<List<OfficeResponse>> getUserOffices(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserOffices(userId));
    }

    /**
     * 利用者と事業所を紐付ける。
     *
     * @param userId 利用者ID
     * @param body   リクエストボディ（officeId を含む）
     * @return 紐付けた事業所のレスポンス（HTTP 201）、officeId が欠如している場合は HTTP 400
     */
    @PostMapping("/{userId}/offices")
    public ResponseEntity<OfficeResponse> addUserOffice(
            @PathVariable Long userId,
            @RequestBody Map<String, Long> body) {
        Long officeId = body.get("officeId");
        if (officeId == null) {
            return ResponseEntity.badRequest().build();
        }
        OfficeResponse response = userService.addUserOffice(userId, officeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 利用者と事業所の紐付けを解除する。
     *
     * @param userId   利用者ID
     * @param officeId 事業所ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{userId}/offices/{officeId}")
    public ResponseEntity<Void> removeUserOffice(
            @PathVariable Long userId,
            @PathVariable Long officeId) {
        userService.removeUserOffice(userId, officeId);
        return ResponseEntity.noContent().build();
    }
}
