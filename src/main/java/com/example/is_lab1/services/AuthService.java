package com.example.is_lab1.services;


import com.example.is_lab1.dto.user.UserRequest;
import com.example.is_lab1.dto.user.UserResponse;
import com.example.is_lab1.entities.User;
import com.example.is_lab1.exceptions.UserAlreadyExistException;
import com.example.is_lab1.jwt.JWTService;
import com.example.is_lab1.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    @Transactional(readOnly = true)
    public UserResponse login(UserRequest request){
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("invalid password or username"));


        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("invalid password or username");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getId());

        return  UserResponse.of(user, token);
    }


    @Transactional
    public UserResponse register(UserRequest request){
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistException("User" + request.username() + "already exists");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.saveAndFlush(user);

        String token = jwtService.generateToken(user.getUsername(), user.getId());

        return  UserResponse.of(user, token);

    }



}