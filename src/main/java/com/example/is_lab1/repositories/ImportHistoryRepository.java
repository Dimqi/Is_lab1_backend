package com.example.is_lab1.repositories;

import com.example.is_lab1.entities.importHistory.ImportHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {

    Page<ImportHistory> findAllByUser_IdOrderByStartedAtDesc(Long userId, Pageable pageable);

    Page<ImportHistory> findAllByOrderByStartedAtDesc(Pageable pageable);
}