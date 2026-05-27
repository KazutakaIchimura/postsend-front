package com.example.mailsend.controller;

import com.example.mailsend.dto.request.CreateOfficeRequest;
import com.example.mailsend.dto.request.UpdateOfficeRequest;
import com.example.mailsend.dto.response.OfficeResponse;
import com.example.mailsend.service.OfficeService;
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

/**
 * 事業所管理のRESTコントローラ。
 */
@RestController
@RequestMapping("/api/offices")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService officeService;

    /**
     * 事業所の一覧を取得する。
     *
     * @param includeInactive true の場合は無効事業所も含める（デフォルト: false）
     * @return 事業所のレスポンスリスト
     */
    @GetMapping
    public ResponseEntity<List<OfficeResponse>> getAllOffices(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(officeService.getAllOffices(includeInactive));
    }

    /**
     * 新規事業所を作成する。
     *
     * @param request 作成リクエスト
     * @return 作成された事業所のレスポンス（HTTP 201）
     */
    @PostMapping
    public ResponseEntity<OfficeResponse> createOffice(@Valid @RequestBody CreateOfficeRequest request) {
        OfficeResponse response = officeService.createOffice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 指定IDの事業所情報を取得する。
     *
     * @param id 事業所ID
     * @return 事業所のレスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<OfficeResponse> getOfficeById(@PathVariable Long id) {
        return ResponseEntity.ok(officeService.getOfficeById(id));
    }

    /**
     * 指定IDの事業所情報を更新する。
     *
     * @param id      事業所ID
     * @param request 更新リクエスト
     * @return 更新後の事業所のレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<OfficeResponse> updateOffice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOfficeRequest request) {
        return ResponseEntity.ok(officeService.updateOffice(id, request));
    }

    /**
     * 指定IDの事業所を無効化する（論理削除）。
     *
     * @param id 事業所ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffice(@PathVariable Long id) {
        officeService.deleteOffice(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定IDの事業所を有効化する。
     *
     * @param id 事業所ID
     * @return HTTP 204 No Content
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateOffice(@PathVariable Long id) {
        officeService.activateOffice(id);
        return ResponseEntity.noContent().build();
    }
}
