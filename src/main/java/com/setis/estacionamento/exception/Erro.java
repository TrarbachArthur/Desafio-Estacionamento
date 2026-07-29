package com.setis.estacionamento.exception;

public record Erro(
        String campo,
        String mensagem
) {
}
