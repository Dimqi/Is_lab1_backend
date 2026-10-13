package com.example.is_lab1.repositories;

import com.example.is_lab1.entities.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HouseRepository extends JpaRepository<House, Long>, JpaSpecificationExecutor<House> {
}
