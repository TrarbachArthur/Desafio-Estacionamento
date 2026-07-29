package com.setis.estacionamento.dto.request;

import java.time.LocalDateTime;

public record EncerrarTicketRequest(
        LocalDateTime saida
) {
}
