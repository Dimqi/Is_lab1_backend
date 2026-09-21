package com.example.is_lab1.restApi;

import com.example.is_lab1.dto.user.UserRequest;
import com.example.is_lab1.dto.user.UserResponse;
import com.example.is_lab1.jwt.JWTService;
import com.example.is_lab1.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserRequest reqDto){
        UserResponse response = authService.login(reqDto);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.token()).
                body(response);


    }


    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest reqDto){
        UserResponse response = authService.register(reqDto);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.token()).
                body(response);


    }





}