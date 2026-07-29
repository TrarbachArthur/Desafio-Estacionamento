package com.setis.estacionamento.domain.cobranca;

import com.setis.estacionamento.domain.enums.Plano;

import java.math.BigDecimal;

public record ResultadoCobranca(
        BigDecimal valor,
        Plano plano
) {
}
