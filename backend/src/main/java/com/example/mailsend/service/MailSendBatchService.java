package com.example.mailsend.service;

import com.example.mailsend.domain.entity.MailSend;
import com.example.mailsend.domain.entity.MailSendBatch;
import com.example.mailsend.domain.entity.Staff;
import com.example.mailsend.dto.request.CreateMailSendBatchRequest;
import com.example.mailsend.dto.response.MailSendBatchResponse;
import com.example.mailsend.exception.ResourceNotFoundException;
import com.example.mailsend.repository.MailSendBatchRepository;
import com.example.mailsend.repository.MailSendRepository;
import com.example.mailsend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.mailsend.constants.AppConstants.SEND_STATUS_SENT;

/**
 * 送付バッチ（一括送付済み処理）に関するビジネスロジックを提供するサービス。
 */
@Service
@RequiredArgsConstructor
public class MailSendBatchService {

    private final MailSendBatchRepository mailSendBatchRepository;
    private final MailSendRepository mailSendRepository;
    private final StaffRepository staffRepository;

    /**
     * 送付バッチを作成し、指定された送付レコードを一括で送付済みに更新する。
     *
     * @param request     バッチ作成リクエスト（送付レコードIDリストと備考）
     * @param sentByEmail 送付操作を行ったスタッフのメールアドレス
     * @return 作成されたバッチのレスポンス
     * @throws ResourceNotFoundException スタッフが見つからない場合
     */
    @Transactional
    public MailSendBatchResponse createBatch(CreateMailSendBatchRequest request, String sentByEmail) {
        Staff sentBy = staffRepository.findByEmail(sentByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("スタッフが見つかりません"));

        LocalDateTime sentAt = LocalDateTime.now();

        MailSendBatch batch = MailSendBatch.builder()
                .sentBy(sentBy)
                .sentAt(sentAt)
                .notes(request.getNotes())
                .build();
        MailSendBatch savedBatch = mailSendBatchRepository.save(batch);

        List<MailSend> mailSends = mailSendRepository.findAllById(request.getMailSendIds());

        for (MailSend ms : mailSends) {
            ms.setStatus(SEND_STATUS_SENT);
            ms.setBatch(savedBatch);
        }
        mailSendRepository.saveAll(mailSends);

        return MailSendBatchResponse.builder()
                .batchId(savedBatch.getId())
                .sentAt(sentAt)
                .updatedCount(mailSends.size())
                .notes(savedBatch.getNotes())
                .build();
    }

    /**
     * 指定IDの送付バッチ情報を取得する。
     *
     * @param id バッチID
     * @return 送付バッチのレスポンス
     * @throws ResourceNotFoundException 送付バッチが見つからない場合
     */
    @Transactional(readOnly = true)
    public MailSendBatchResponse getBatchById(Long id) {
        MailSendBatch batch = mailSendBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("送付バッチ", id));

        int updatedCount = (int) mailSendRepository.countByBatchId(id);

        return MailSendBatchResponse.builder()
                .batchId(batch.getId())
                .sentAt(batch.getSentAt())
                .updatedCount(updatedCount)
                .notes(batch.getNotes())
                .build();
    }
}
