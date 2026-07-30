package com.setis.estacionamento.dto.response;

import com.setis.estacionamento.domain.enums.StatusVaga;
import com.setis.estacionamento.domain.enums.TipoVaga;

import java.util.UUID;

public record VagaResponse(
    UUID id,
    String codigo,
    TipoVaga tipo,
    StatusVaga status) {
}
