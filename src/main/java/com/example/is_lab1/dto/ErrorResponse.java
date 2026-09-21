package com.example.is_lab1.dto;


public record ErrorResponse(int status, String error, String message) {
}