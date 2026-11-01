package com.example.is_lab1.repositories.specifications;

import com.example.is_lab1.dto.flat.FlatFilterRequest;
import com.example.is_lab1.dto.flat.FlatRequest;
import com.example.is_lab1.entities.Flat;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class FlatSpecifications {

    public static Specification<Flat> build(FlatFilterRequest filter) {
        if (filter == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.name().toLowerCase() + "%"));
            }

            if (filter.creationDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("creationDate"), filter.creationDateFrom()));
            }
            if (filter.creationDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("creationDate"), filter.creationDateTo()));
            }

            if (filter.areaMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("area"), filter.areaMin()));
            }
            if (filter.areaMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("area"), filter.areaMax()));
            }

            if (filter.priceMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.priceMin()));
            }
            if (filter.priceMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.priceMax()));
            }

            if (filter.balcony() != null) {
                predicates.add(cb.equal(root.get("balcony"), filter.balcony()));
            }

            if (filter.timeToMetroOnFootMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timeToMetroOnFoot"), filter.timeToMetroOnFootMin()));
            }
            if (filter.timeToMetroOnFootMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timeToMetroOnFoot"), filter.timeToMetroOnFootMax()));
            }

            if (filter.numberOfRoomsMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("numberOfRooms"), filter.numberOfRoomsMin()));
            }
            if (filter.numberOfRoomsMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("numberOfRooms"), filter.numberOfRoomsMax()));
            }

            if (filter.furnish() != null) {
                predicates.add(cb.equal(root.get("furnish"), filter.furnish()));
            }
            if (filter.view() != null) {
                predicates.add(cb.equal(root.get("view"), filter.view()));
            }
            if (filter.transport() != null) {
                predicates.add(cb.equal(root.get("transport"), filter.transport()));
            }

            if (filter.houseId() != null){
                predicates.add(cb.equal(root.get("house").get("id"), filter.houseId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Flat> withNamesUsedByHouse(
            List<FlatRequest> flatRequests) {

        return (root, query, cb) -> {
            if (flatRequests == null || flatRequests.isEmpty()) {
                return cb.disjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            for (FlatRequest flatRequest : flatRequests) {
                predicates.add(
                        cb.and(
                                cb.equal(root.get("name"), flatRequest.name()),
                                cb.equal(
                                        root.get("house").get("id"),
                                        flatRequest.houseId()
                                )
                        )
                );
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Flat> withDifferentTimeToMetro(
            List<FlatRequest> flatRequests) {

        return (root, query, cb) -> {
            if (flatRequests == null || flatRequests.isEmpty()) {
                return cb.disjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            for (FlatRequest flatRequest : flatRequests) {
                predicates.add(
                        cb.and(
                                cb.equal(
                                        root.get("house").get("id"),
                                        flatRequest.houseId()
                                ),
                                cb.notEqual(
                                        root.get("timeToMetroOnFoot"),
                                        flatRequest.timeToMetroOnFoot()
                                )
                        )
                );
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}