package com.example.is_lab1.exceptions;

public class FlatAlreadyExistsException extends DataConflictException{
    public FlatAlreadyExistsException(String message){
        super(message);
    }

}
