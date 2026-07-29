package com.setis.estacionamento.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends GeneralException{

    public ConflictException(CodigoErro codigo, String mensagem){
        super(codigo, HttpStatus.CONFLICT, mensagem);
    }
}
