package com.founderlink.investmentService.exception;

public class InvestmentNotFoundException extends RuntimeException{

    public InvestmentNotFoundException(String message){
        super(message);
    }
}
