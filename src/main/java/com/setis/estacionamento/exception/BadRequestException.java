package com.setis.estacionamento.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends GeneralException{

    public BadRequestException(CodigoErro codigo, String mensagem){
        super(codigo, HttpStatus.BAD_REQUEST, mensagem);
    }
}
