package com.founderlink.authService.exception;

public class InvalidCredentialsException extends RuntimeException{

    public InvalidCredentialsException(String message){
        super(message);
    }
}
