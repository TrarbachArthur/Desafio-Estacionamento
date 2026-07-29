package com.setis.estacionamento.exception;

public enum CodigoErro {
    // 404
    RECURSO_NAO_ENCONTRADO,

    // 409
    CODIGO_VAGA_DUPLICADO,
    VAGA_OCUPADA,
    DOCUMENTO_DUPLICADO,
    VAGA_INDISPONIVEL,
    TICKET_ABERTO_EXISTENTE,
    TICKET_NAO_ABERTO
}
