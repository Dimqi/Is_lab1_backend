package com.example.is_lab1.dto;

public record ChangeEvent<T>(
        EntityType entityType,
        ActionType action,
        Long id,
        T payload
) {
    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }

    public enum EntityType {
        FLAT,
        HOUSE
    }

    public static <T> ChangeEvent<T> of(EntityType entityType, ActionType action, Long id, T payload) {
        return new ChangeEvent<>(entityType, action, id, payload);
    }

    public static <T> ChangeEvent<T> of(EntityType entityType, ActionType action, Long id) {
        return new ChangeEvent<>(entityType, action, id, null);
    }
}