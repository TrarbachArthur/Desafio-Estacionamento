package com.setis.estacionamento.dto.response;

import java.util.UUID;

public record TicketClienteResponse(
        UUID id,
        String nome,
        String documento
) {
}
