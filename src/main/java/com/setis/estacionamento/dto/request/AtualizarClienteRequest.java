package com.setis.estacionamento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AtualizarClienteRequest(
    @NotBlank(message = "e obrigatorio")
    @Size(min = 3, max = 120, message = "deve ter entre 3 e 120 caracteres")
    String nome,

    @NotNull(message = "e obrigatorio")
    LocalDate assinaturaValidaAte
) {
}
