package com.example.is_lab1.repositories.specifications;

import com.example.is_lab1.dto.house.HouseRequest;
import com.example.is_lab1.entities.Coordinates;
import com.example.is_lab1.entities.House;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class CoordinatesSpecifications {

    public static Specification<Coordinates> withCoordinatesUsedByHouse(
            List<HouseRequest> houseRequests) {

        return (root, query, cb) -> {
            if (houseRequests == null || houseRequests.isEmpty()) {
                return cb.disjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            for (HouseRequest house : houseRequests) {
                predicates.add(
                        cb.and(
                                cb.equal(root.get("x"), house.coordinates().x()),
                                cb.equal(root.get("y"), house.coordinates().y())
                        )
                );
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<House> houseRoot = subquery.from(House.class);

            subquery.select(houseRoot.get("id"));
            subquery.where(
                    cb.equal(
                            houseRoot.get("coordinates"),
                            root
                    )
            );

            return cb.and(
                    cb.or(predicates.toArray(new Predicate[0])),
                    cb.exists(subquery)
            );
        };
    }
}