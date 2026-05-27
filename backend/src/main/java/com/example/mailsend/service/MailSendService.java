package com.example.mailsend.service;

import com.example.mailsend.domain.entity.MailSend;
import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.domain.entity.Staff;
import com.example.mailsend.domain.entity.User;
import com.example.mailsend.dto.request.CreateMailSendRequest;
import com.example.mailsend.dto.response.MailSendByOfficeResponse;
import com.example.mailsend.dto.response.MailSendResponse;
import com.example.mailsend.dto.response.OfficeResponse;
import com.example.mailsend.exception.DuplicateResourceException;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.repository.MailSendRepository;
import com.example.mailsend.repository.MailSendSpecification;
import com.example.mailsend.repository.OfficeRepository;
import com.example.mailsend.repository.StaffRepository;
import com.example.mailsend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.mailsend.constants.AppConstants.SEND_STATUS_PENDING;

/**
 * 送付レコードに関するビジネスロジックを提供するサービス。
 */
@Service
@RequiredArgsConstructor
public class MailSendService {

    private final MailSendRepository mailSendRepository;
    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;
    private final StaffRepository staffRepository;

    /**
     * フィルタ条件に基づいて送付レコード一覧を取得する。
     *
     * @param dateFrom 更新日の下限（任意）
     * @param dateTo   更新日の上限（任意）
     * @param officeId 事業所IDによる絞り込み（任意）
     * @param userId   利用者IDによる絞り込み（任意）
     * @return 送付レコードのレスポンスリスト
     */
    @Transactional(readOnly = true)
    public List<MailSendResponse> getMailSends(LocalDate dateFrom, LocalDate dateTo,
                                                Long officeId, Long userId) {
        return mailSendRepository
                .findAll(MailSendSpecification.withFilters(dateFrom, dateTo, officeId, userId),
                         Sort.by(Sort.Direction.DESC, "updatedAt"))
                .stream()
                .map(MailSendResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 未送付の送付レコードを事業所ごとにグループ化して取得する。
     *
     * @return 事業所ごとにグループ化された送付レコードのリスト
     */
    @Transactional(readOnly = true)
    public List<MailSendByOfficeResponse> getMailSendsByOffice() {
        List<MailSend> pendingList = mailSendRepository.findByStatus(SEND_STATUS_PENDING);

        return pendingList.stream()
                .collect(Collectors.groupingBy(
                        ms -> ms.getOffice().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Office office = entry.getValue().get(0).getOffice();
                    List<MailSendResponse> mailSendResponses = entry.getValue().stream()
                            .map(MailSendResponse::from)
                            .collect(Collectors.toList());
                    return MailSendByOfficeResponse.builder()
                            .office(OfficeResponse.from(office))
                            .mailSends(mailSendResponses)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 送付レコードを新規作成する。同一の利用者・事業所・送付種別・送付月の組み合わせは登録不可。
     *
     * @param request        作成リクエスト
     * @param createdByEmail 作成者のメールアドレス
     * @return 作成された送付レコードのレスポンス
     * @throws DuplicateResourceException 同一レコードがすでに存在する場合
     * @throws ResourceNotFoundException  利用者・事業所・スタッフが見つからない場合
     */
    @Transactional
    public MailSendResponse createMailSend(CreateMailSendRequest request, String createdByEmail) {
        LocalDate sendMonth = request.getSendMonth().withDayOfMonth(1);

        boolean exists = mailSendRepository.existsByUserIdAndOfficeIdAndSendTypeAndSendMonth(
                request.getUserId(), request.getOfficeId(), request.getSendType(), sendMonth);
        if (exists) {
            throw new DuplicateResourceException("同じ利用者・事業所・送付種別・送付月の送付レコードがすでに存在します");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("利用者", request.getUserId()));
        Office office = officeRepository.findById(request.getOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("事業所", request.getOfficeId()));
        Staff createdBy = staffRepository.findByEmail(createdByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("スタッフが見つかりません"));

        MailSend mailSend = MailSend.builder()
                .user(user)
                .office(office)
                .sendType(request.getSendType())
                .sendMonth(sendMonth)
                .createdBy(createdBy)
                .build();

        MailSend saved = mailSendRepository.save(mailSend);
        return MailSendResponse.from(saved);
    }

    /**
     * 指定IDの送付レコードを削除する。
     *
     * @param id 削除対象の送付レコードID
     * @throws ResourceNotFoundException 送付レコードが見つからない場合
     */
    @Transactional
    public void deleteMailSend(Long id) {
        MailSend mailSend = mailSendRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("送付レコード", id));
        mailSendRepository.delete(mailSend);
    }
}
