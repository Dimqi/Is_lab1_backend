package com.example.is_lab1.repositories;

import com.example.is_lab1.entities.House;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseRepository extends JpaRepository<House, Long> {
}
