package com.setis.estacionamento.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class NotFoundException extends GeneralException {

    public NotFoundException(String recurso, UUID id) {
        super(CodigoErro.RECURSO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND,
                "%s com id %s nao encontrado".formatted(recurso, id));
    }
}
