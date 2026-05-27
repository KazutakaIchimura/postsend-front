package com.example.mailsend.service;

import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.domain.entity.User;
import com.example.mailsend.domain.entity.UserOffice;
import com.example.mailsend.dto.request.CreateUserRequest;
import com.example.mailsend.dto.request.UpdateUserRequest;
import com.example.mailsend.dto.response.OfficeResponse;
import com.example.mailsend.dto.response.UserResponse;
import com.example.mailsend.exception.DuplicateResourceException;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.repository.OfficeRepository;
import com.example.mailsend.repository.UserOfficeRepository;
import com.example.mailsend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 利用者管理に関するビジネスロジックを提供するサービス。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;
    private final UserOfficeRepository userOfficeRepository;

    /**
     * 有効な利用者の一覧を取得する。
     *
     * @return 利用者のレスポンスリスト
     */
    /**
     * 利用者の一覧を取得する。
     *
     * @param includeInactive true の場合は無効ユーザーも含める。false の場合は有効ユーザーのみ
     * @return 利用者のレスポンスリスト
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(boolean includeInactive) {
        List<User> users = includeInactive
                ? userRepository.findAll()
                : userRepository.findByIsActiveTrue();
        return users.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 指定IDの利用者情報を関連する事業所情報とともに取得する。
     *
     * @param id 利用者ID
     * @return 事業所一覧を含む利用者のレスポンス
     * @throws ResourceNotFoundException 利用者が見つからない場合
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("利用者", id));

        List<UserOffice> userOffices = userOfficeRepository.findByUserId(id);
        List<OfficeResponse> offices = userOffices.stream()
                .map(uo -> OfficeResponse.from(uo.getOffice()))
                .collect(Collectors.toList());

        return UserResponse.fromWithOffices(user, offices);
    }

    /**
     * 新規利用者を作成する。
     *
     * @param request 作成リクエスト
     * @return 作成された利用者のレスポンス
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .nameKana(request.getNameKana())
                .birthDate(request.getBirthDate())
                .notes(request.getNotes())
                .build();
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    /**
     * 既存利用者の情報を更新する。
     *
     * @param id      更新対象の利用者ID
     * @param request 更新リクエスト
     * @return 更新後の利用者のレスポンス
     * @throws ResourceNotFoundException 利用者が見つからない場合
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("利用者", id));

        user.setName(request.getName());
        user.setNameKana(request.getNameKana());
        user.setBirthDate(request.getBirthDate());
        user.setNotes(request.getNotes());

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    /**
     * 指定IDの利用者を論理削除する（isActive = false）。
     *
     * @param id 削除対象の利用者ID
     * @throws ResourceNotFoundException 利用者が見つからない場合
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("利用者", id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * 指定IDの利用者を有効化する。
     *
     * @param id 有効化対象の利用者ID
     * @throws ResourceNotFoundException 利用者が見つからない場合
     */
    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("利用者", id));
        user.setIsActive(true);
        userRepository.save(user);
    }

    /**
     * 指定利用者に紐付く事業所一覧を取得する。
     *
     * @param userId 利用者ID
     * @return 事業所のレスポンスリスト
     * @throws ResourceNotFoundException 利用者が見つからない場合
     */
    @Transactional(readOnly = true)
    public List<OfficeResponse> getUserOffices(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("利用者", userId));
        List<UserOffice> userOffices = userOfficeRepository.findByUserId(userId);
        return userOffices.stream()
                .map(uo -> OfficeResponse.from(uo.getOffice()))
                .collect(Collectors.toList());
    }

    /**
     * 利用者と事業所を紐付ける。
     *
     * @param userId   利用者ID
     * @param officeId 事業所ID
     * @return 紐付けた事業所のレスポンス
     * @throws ResourceNotFoundException  利用者または事業所が見つからない場合
     * @throws DuplicateResourceException すでに紐付け済みの場合
     */
    @Transactional
    public OfficeResponse addUserOffice(Long userId, Long officeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("利用者", userId));
        Office office = officeRepository.findById(officeId)
                .orElseThrow(() -> new ResourceNotFoundException("事業所", officeId));

        if (userOfficeRepository.existsByUserIdAndOfficeId(userId, officeId)) {
            throw new DuplicateResourceException("この利用者はすでにこの事業所に紐付いています");
        }

        UserOffice userOffice = UserOffice.builder()
                .user(user)
                .office(office)
                .build();
        userOfficeRepository.save(userOffice);
        return OfficeResponse.from(office);
    }

    /**
     * 利用者と事業所の紐付けを解除する。
     *
     * @param userId   利用者ID
     * @param officeId 事業所ID
     * @throws ResourceNotFoundException 紐付けが見つからない場合
     */
    @Transactional
    public void removeUserOffice(Long userId, Long officeId) {
        UserOffice userOffice = userOfficeRepository.findByUserIdAndOfficeId(userId, officeId)
                .orElseThrow(() -> new ResourceNotFoundException("利用者と事業所の紐付けが見つかりません"));
        userOfficeRepository.delete(userOffice);
    }
}
