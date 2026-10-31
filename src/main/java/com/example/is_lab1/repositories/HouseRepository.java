package com.example.is_lab1.repositories;

import com.example.is_lab1.entities.House;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HouseRepository extends JpaRepository<House, Long>, JpaSpecificationExecutor<House> {
    boolean existsById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM House h WHERE h.id = :id")
    Optional<House> findByIdForUpdate(@Param("id") Long id);
}
