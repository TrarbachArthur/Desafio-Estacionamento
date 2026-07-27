package com.setis.estacionamento.dto.response;

import com.setis.estacionamento.domain.enums.TipoVaga;

import java.util.UUID;

public record TicketVagaResponse(
        UUID id,
        String codigo,
        TipoVaga tipo
) {
}
