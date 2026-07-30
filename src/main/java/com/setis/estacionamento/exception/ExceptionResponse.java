package com.setis.estacionamento.exception;

import java.util.List;

public record ExceptionResponse(
        int status,
        CodigoErro codigo,
        String mensagem,
        String path,
        List<Erro> erros
) {
    // Construtor para os casos em que nao existe erros -> []
    public ExceptionResponse(int status, CodigoErro codigo, String mensagem, String path) {
        this(status, codigo, mensagem, path, List.of());
    }
}
