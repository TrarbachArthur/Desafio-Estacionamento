package com.setis.estacionamento.dto.request;

import com.setis.estacionamento.domain.enums.Plano;
import com.setis.estacionamento.domain.enums.TipoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.UUID;

public record AbrirTicketRequest(
        @NotBlank(message = "e obrigatorio")
        @Pattern(
                regexp = "[A-Z]{3}(\\d{4}|\\d[A-Z]\\d{2})",
                message = "deve seguir o padrao antigo (ABC1234) ou mercosul (ABC1D23)"
        )
        String placa,

        @NotNull(message = "e obrigatorio")
        TipoVeiculo tipoVeiculo,

        @NotNull(message = "e obrigatorio")
        UUID vagaId,

        @NotNull(message = "e obrigatorio")
        Plano plano,

        UUID clienteId,

        LocalDateTime entrada
) {

    public AbrirTicketRequest {
        placa = normalizar(placa);
    }

    private static String normalizar(String placa) {
        return placa == null ? null : placa.trim().toUpperCase();
    }

}
