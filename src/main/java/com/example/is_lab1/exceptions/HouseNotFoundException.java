package com.example.is_lab1.exceptions;

public class HouseNotFoundException extends RuntimeException{
    public HouseNotFoundException(String message){
        super(message);
    }
}
