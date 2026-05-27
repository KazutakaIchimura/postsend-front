package com.example.mailsend.repository;

import com.example.mailsend.domain.entity.UserOffice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOfficeRepository extends JpaRepository<UserOffice, Long> {

    @EntityGraph(attributePaths = {"office"})
    List<UserOffice> findByUserId(Long userId);

    Optional<UserOffice> findByUserIdAndOfficeId(Long userId, Long officeId);

    boolean existsByUserIdAndOfficeId(Long userId, Long officeId);
}
