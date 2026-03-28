package com.founderlink.messagingService.exception;

public class ConversationNotFoundException extends RuntimeException{

    public ConversationNotFoundException(String message){
        super(message);
    }
}
