package com.example.is_lab1.exceptions;

public class CoordinatesAlreadyExistException extends DataConflictException{
    public CoordinatesAlreadyExistException(String message){
        super(message);
    }

}
