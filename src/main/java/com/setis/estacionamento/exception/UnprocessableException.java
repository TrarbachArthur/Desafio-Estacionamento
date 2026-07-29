package com.setis.estacionamento.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableException extends GeneralException{

    public UnprocessableException(CodigoErro codigo, String mensagem) {
        super(codigo, HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
