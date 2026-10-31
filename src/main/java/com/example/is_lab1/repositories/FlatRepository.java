package com.example.is_lab1.repositories;

import com.example.is_lab1.entities.Flat;
import com.example.is_lab1.entities.House;
import com.example.is_lab1.entities.Transport;
import com.example.is_lab1.entities.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlatRepository extends JpaRepository<Flat, Long>, JpaSpecificationExecutor<Flat> {

    @Modifying
    @Query("DELETE FROM Flat f where f.transport = :transport")
    int deleteByTransport(@Param("transport") Transport transport);

    int countByView(View view);

    @Query("SELECT DISTINCT f.view FROM Flat f WHERE f.view IS NOT NULL")
    List<View> findDistinctView();

    @Query("SELECT SUM(f.price) FROM Flat f")
    long sumAllPrices();

    @Modifying
    @Query("UPDATE Flat f SET f.house = :newHouse WHERE f.house.id = :oldHouseId")
    void updateHouseForFlats(@Param("oldHouseId") Long oldHouseId, @Param("newHouse") House newHouse);

    boolean existsByNameAndHouse(@Param("name") String name, @Param("house") House house);


    Optional<Flat> findFirstByHouse_Id(@Param("id") Long id);
}
