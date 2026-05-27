package com.example.mailsend.service;

import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.dto.request.CreateOfficeRequest;
import com.example.mailsend.dto.request.UpdateOfficeRequest;
import com.example.mailsend.dto.response.OfficeResponse;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.repository.OfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事業所管理に関するビジネスロジックを提供するサービス。
 */
@Service
@RequiredArgsConstructor
public class OfficeService {

    private final OfficeRepository officeRepository;

    /**
     * 事業所の一覧を取得する。
     *
     * @param includeInactive true の場合は無効事業所も含める。false の場合は有効事業所のみ
     * @return 事業所のレスポンスリスト
     */
    @Transactional(readOnly = true)
    public List<OfficeResponse> getAllOffices(boolean includeInactive) {
        List<Office> offices = includeInactive
                ? officeRepository.findAll()
                : officeRepository.findByIsActiveTrue();
        return offices.stream()
                .map(OfficeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 指定IDの事業所情報を取得する。
     *
     * @param id 事業所ID
     * @return 事業所のレスポンス
     * @throws ResourceNotFoundException 事業所が見つからない場合
     */
    @Transactional(readOnly = true)
    public OfficeResponse getOfficeById(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("事業所", id));
        return OfficeResponse.from(office);
    }

    /**
     * 新規事業所を作成する。
     *
     * @param request 作成リクエスト
     * @return 作成された事業所のレスポンス
     */
    @Transactional
    public OfficeResponse createOffice(CreateOfficeRequest request) {
        Office office = Office.builder()
                .name(request.getName())
                .postalCode(request.getPostalCode())
                .address(request.getAddress())
                .building(request.getBuilding())
                .phone(request.getPhone())
                .build();
        Office saved = officeRepository.save(office);
        return OfficeResponse.from(saved);
    }

    /**
     * 既存事業所の情報を更新する。
     *
     * @param id      更新対象の事業所ID
     * @param request 更新リクエスト
     * @return 更新後の事業所のレスポンス
     * @throws ResourceNotFoundException 事業所が見つからない場合
     */
    @Transactional
    public OfficeResponse updateOffice(Long id, UpdateOfficeRequest request) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("事業所", id));

        office.setName(request.getName());
        office.setPostalCode(request.getPostalCode());
        office.setAddress(request.getAddress());
        office.setBuilding(request.getBuilding());
        office.setPhone(request.getPhone());

        Office saved = officeRepository.save(office);
        return OfficeResponse.from(saved);
    }

    /**
     * 指定IDの事業所を無効化する（論理削除）。
     *
     * @param id 無効化対象の事業所ID
     * @throws ResourceNotFoundException 事業所が見つからない場合
     */
    @Transactional
    public void deleteOffice(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("事業所", id));
        office.setIsActive(false);
        officeRepository.save(office);
    }

    /**
     * 指定IDの事業所を有効化する。
     *
     * @param id 有効化対象の事業所ID
     * @throws ResourceNotFoundException 事業所が見つからない場合
     */
    @Transactional
    public void activateOffice(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("事業所", id));
        office.setIsActive(true);
        officeRepository.save(office);
    }
}
