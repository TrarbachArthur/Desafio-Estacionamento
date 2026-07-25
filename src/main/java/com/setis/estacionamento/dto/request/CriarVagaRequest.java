package com.setis.estacionamento.dto.request;


import com.setis.estacionamento.domain.enums.TipoVaga;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarVagaRequest (
    @NotBlank(message = "e obrigatorio")
    @Size(min = 1, max = 10, message = "deve ter entre 1 e 10 caracteres")
    String codigo,

    @NotNull(message = "e obrigatorio")
    TipoVaga tipo) {}
