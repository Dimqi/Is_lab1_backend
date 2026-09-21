package com.example.is_lab1.dto.user;

import com.example.is_lab1.entities.User;

public record UserResponse(
        Long id,
        String userName,
        String token

) {
    public static UserResponse of(User user, String token){
        return new UserResponse(user.getId(), user.getUsername(), token);
    }
}
