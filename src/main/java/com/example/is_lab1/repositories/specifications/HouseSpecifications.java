package com.example.is_lab1.repositories.specifications;

import com.example.is_lab1.entities.House;
import com.example.is_lab1.dto.house.HouseFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class HouseSpecifications {

    public static Specification<House> withFilter(HouseFilterRequest filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();


            if (filter.yearMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("year"), filter.yearMin()));
            }

            if (filter.yearMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("year"), filter.yearMax()));
            }

            if (filter.numberOfFlatsOnFloorMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("numberOfFlatsOnFloor"), filter.numberOfFlatsOnFloorMin()));
            }

            if (filter.numberOfFlatsOnFloorMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("numberOfFlatsOnFloor"), filter.numberOfFlatsOnFloorMax()));
            }

            if (filter.name() != null && !filter.name().isBlank()) {
                String pattern = "%" + filter.name().toLowerCase().trim() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}