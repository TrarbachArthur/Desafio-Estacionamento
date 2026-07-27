package com.setis.estacionamento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CriarClienteRequest(
    @NotBlank(message = "e obrigatorio")
    @Size(min = 3, max = 120, message = "e obrigatorio")
    String nome,

    @NotBlank(message = "e obrigatorio")
    @Pattern(regexp = "\\d{11}", message = "deve conter exatamente 11 digitos numericos")
    String documento,

    @NotNull(message = "e obrigatorio")
    LocalDate assinaturaValidaAte
) {
}
