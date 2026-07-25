package com.setis.estacionamento.dto.request;

import com.setis.estacionamento.domain.enums.StatusVaga;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusVagaRequest (
        @NotNull(message = "e obrigatorio")
        StatusVaga status
) {}
