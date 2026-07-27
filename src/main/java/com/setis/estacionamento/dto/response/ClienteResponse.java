package com.setis.estacionamento.dto.response;

import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String documento,
        String assinaturaValidaAte,
        boolean assinaturaVigente
) {
}
