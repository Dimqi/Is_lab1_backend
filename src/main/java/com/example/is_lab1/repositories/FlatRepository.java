package com.example.is_lab1.repositories;

import com.example.is_lab1.entities.Flat;
import com.example.is_lab1.entities.Transport;
import com.example.is_lab1.entities.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.JpqlQueryBuilder;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static org.eclipse.persistence.jpa.jpql.parser.Expression.SELECT;

public interface FlatRepository extends JpaRepository<Flat, Long> {

    @Modifying
    @Query("DELETE FROM Flat f where f.transport = :transport")
    int deleteByTransport(@Param("transport") Transport transport);

    int countByView(View view);

    @Query("SELECT DISTINCT f.view FROM Flat f WHERE f.view IS NOT NULL")
    List<View> findDistinctView();

    @Query("SELECT SUM(f.price) FROM Flat f")
    long sumAllPrices();
}
