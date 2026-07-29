package com.setis.estacionamento.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class GeneralException extends RuntimeException {

    private final CodigoErro codigo;
    private final HttpStatus status;

    protected GeneralException(CodigoErro codigo, HttpStatus status, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
        this.status = status;
    }
}
