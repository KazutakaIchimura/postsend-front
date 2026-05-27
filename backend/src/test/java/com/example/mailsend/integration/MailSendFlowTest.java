package com.example.mailsend.integration;

import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.domain.entity.Staff;
import com.example.mailsend.domain.entity.User;
import static com.example.mailsend.constants.AppConstants.*;
import com.example.mailsend.dto.request.CreateMailSendBatchRequest;
import com.example.mailsend.dto.request.CreateMailSendRequest;
import com.example.mailsend.repository.MailSendRepository;
import com.example.mailsend.repository.OfficeRepository;
import com.example.mailsend.repository.StaffRepository;
import com.example.mailsend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MailSendFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private MailSendRepository mailSendRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;
    private Staff staff;
    private User user;
    private Office office;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        staff = staffRepository.save(Staff.builder()
                .name("管理者")
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("changeme"))
                .role(ROLE_ADMIN)
                .isActive(true)
                .forcePasswordChange(false)
                .build());

        user = userRepository.save(User.builder()
                .name("田中太郎")
                .nameKana("たなかたろう")
                .isActive(true)
                .build());

        office = officeRepository.save(Office.builder()
                .name("GHさくら")
                .postalCode("150-0001")
                .address("東京都渋谷区神宮前1-1-1")
                .isActive(true)
                .build());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void 月次送付フロー_登録から一括送付済みまで() throws Exception {
        // Step 1: 送付レコード登録
        CreateMailSendRequest createRequest = new CreateMailSendRequest();
        createRequest.setUserId(user.getId());
        createRequest.setOfficeId(office.getId());
        createRequest.setSendType(SEND_TYPE_PLAN);
        createRequest.setSendMonth(LocalDate.now().withDayOfMonth(1));

        String createResult = mockMvc.perform(post("/api/mail-sends")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.userName").value("田中太郎"))
                .andReturn().getResponse().getContentAsString();

        Long mailSendId = objectMapper.readTree(createResult).get("id").asLong();

        // Step 2: 重複登録は 409
        mockMvc.perform(post("/api/mail-sends")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        // Step 3: 一覧取得（PENDING が含まれる）        mockMvc.perform(get("/api/mail-sends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + mailSendId + ")].status").value("PENDING"));

        // Step 4: 送付先別一覧取得        mockMvc.perform(get("/api/mail-sends/by-office"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.office.id == " + office.getId() + ")].mailSends[0].status").value("PENDING"));

        // Step 5: 一括送付済み処理        CreateMailSendBatchRequest batchRequest = new CreateMailSendBatchRequest();
        batchRequest.setMailSendIds(List.of(mailSendId));
        batchRequest.setNotes("5月定例送付");

        mockMvc.perform(post("/api/mail-send-batches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.updatedCount").value(1))
                .andExpect(jsonPath("$.batchId").isNumber());

        // Step 6: ステータスが SENT に変わっていることを DB で確認        var updated = mailSendRepository.findById(mailSendId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SEND_STATUS_SENT);
        assertThat(updated.getBatch()).isNotNull();
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void 利用者管理フロー_登録から事業所紐付けまで() throws Exception {
        // 利用者登録
        String createBody = """
                { "name": "新規利用者", "nameKana": "しんきりようしゃ" }
                """;

        String result = mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("新規利用者"))
                .andReturn().getResponse().getContentAsString();

        Long newUserId = objectMapper.readTree(result).get("id").asLong();

        // 事業所を紐付け
        String officeBody = String.format("{\"officeId\": %d}", office.getId());
        mockMvc.perform(post("/api/users/" + newUserId + "/offices")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(officeBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(office.getId()));

        // 紐付き事業所一覧取得        mockMvc.perform(get("/api/users/" + newUserId + "/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("GHさくら"));

        // 利用者詳細取得（offices フィールド含む）        mockMvc.perform(get("/api/users/" + newUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offices[0].id").value(office.getId()));

        // 紐付け解除
        mockMvc.perform(delete("/api/users/" + newUserId + "/offices/" + office.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // 解除後は空
        mockMvc.perform(get("/api/users/" + newUserId + "/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void 送付レコード削除フロー() throws Exception {
        CreateMailSendRequest createRequest = new CreateMailSendRequest();
        createRequest.setUserId(user.getId());
        createRequest.setOfficeId(office.getId());
        createRequest.setSendType(SEND_TYPE_MONITORING);
        createRequest.setSendMonth(LocalDate.now().withDayOfMonth(1));

        String result = mockMvc.perform(post("/api/mail-sends")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long mailSendId = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(delete("/api/mail-sends/" + mailSendId).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(mailSendRepository.findById(mailSendId)).isEmpty();
    }
}

