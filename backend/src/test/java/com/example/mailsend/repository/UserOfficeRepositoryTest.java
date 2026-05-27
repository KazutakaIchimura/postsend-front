package com.example.mailsend.repository;

import com.example.mailsend.domain.entity.Office;
import com.example.mailsend.domain.entity.User;
import com.example.mailsend.domain.entity.UserOffice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserOfficeRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserOfficeRepository userOfficeRepository;

    private User user;
    private Office office1;
    private Office office2;

    @BeforeEach
    void setUp() {
        user = em.persist(User.builder().name("田中太郎").isActive(true).build());
        office1 = em.persist(Office.builder().name("GHさくら").isActive(true).build());
        office2 = em.persist(Office.builder().name("GHひまわり").isActive(true).build());
        em.flush();
    }

    private UserOffice saveUserOffice(User u, Office o) {
        UserOffice uo = UserOffice.builder().user(u).office(o).build();
        return em.persist(uo);
    }

    @Test
    void findByUserId_returnsAllRelatedOffices() {
        saveUserOffice(user, office1);
        saveUserOffice(user, office2);
        em.flush();

        List<UserOffice> result = userOfficeRepository.findByUserId(user.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void findByUserIdWithOffice_fetchesOffice() {
        saveUserOffice(user, office1);
        em.flush();
        em.clear();

        List<UserOffice> result = userOfficeRepository.findByUserIdWithOffice(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOffice()).isNotNull();
        assertThat(result.get(0).getOffice().getName()).isEqualTo("GHさくら");
    }

    @Test
    void findByUserIdAndOfficeId_found_returnsOptional() {
        saveUserOffice(user, office1);
        em.flush();

        Optional<UserOffice> result = userOfficeRepository.findByUserIdAndOfficeId(
                user.getId(), office1.getId());

        assertThat(result).isPresent();
    }

    @Test
    void findByUserIdAndOfficeId_notFound_returnsEmpty() {
        Optional<UserOffice> result = userOfficeRepository.findByUserIdAndOfficeId(
                user.getId(), office1.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUserIdAndOfficeId_exists_returnsTrue() {
        saveUserOffice(user, office1);
        em.flush();

        boolean exists = userOfficeRepository.existsByUserIdAndOfficeId(
                user.getId(), office1.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndOfficeId_notExists_returnsFalse() {
        boolean exists = userOfficeRepository.existsByUserIdAndOfficeId(
                user.getId(), office1.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void delete_removesUserOffice() {
        UserOffice uo = saveUserOffice(user, office1);
        em.flush();

        userOfficeRepository.delete(uo);
        em.flush();

        boolean exists = userOfficeRepository.existsByUserIdAndOfficeId(
                user.getId(), office1.getId());
        assertThat(exists).isFalse();
    }
}
