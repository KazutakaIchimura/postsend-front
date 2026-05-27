package com.example.mailsend.repository;

import com.example.mailsend.domain.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {

    List<Office> findByIsActiveTrue();
}
