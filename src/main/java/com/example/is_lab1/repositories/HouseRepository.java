package com.example.is_lab1.repositories;

import com.example.is_lab1.entities.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface HouseRepository extends JpaRepository<House, Long>,
        JpaSpecificationExecutor<House> {

    @Query("SELECT h.id FROM House h WHERE h.id IN :houseIds")
    List<Long> findExistingIds(@Param("houseIds") Collection<Long> houseIds);
}