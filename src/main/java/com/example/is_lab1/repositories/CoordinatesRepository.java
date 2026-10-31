package com.example.is_lab1.repositories;

import com.example.is_lab1.entities.Coordinates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoordinatesRepository extends JpaRepository<Coordinates, Long>, JpaSpecificationExecutor<Coordinates> {

    Optional<Coordinates> findByXAndY(@Param("x") Double x, @Param("н") Long y);

    boolean existsByXAndY(@Param("x") Double x, @Param("н") Long y);
}
