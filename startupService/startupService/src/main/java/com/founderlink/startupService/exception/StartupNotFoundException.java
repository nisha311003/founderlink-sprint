package com.founderlink.startupService.exception;

public class StartupNotFoundException extends RuntimeException{
    public StartupNotFoundException(String message){
        super(message);
    }
}
