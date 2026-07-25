package com.setis.estacionamento.dto.response;

import com.setis.estacionamento.domain.enums.StatusVaga;
import com.setis.estacionamento.domain.enums.TipoVaga;

import java.util.UUID;

public record CriarVagaResponse (
    UUID id,
    String codigo,
    TipoVaga tipoVaga,
    StatusVaga statusVaga) {
}
