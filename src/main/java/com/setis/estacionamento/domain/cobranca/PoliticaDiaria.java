package com.setis.estacionamento.domain.cobranca;

import com.setis.estacionamento.domain.enums.Plano;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Component
public class PoliticaDiaria implements PoliticaCobranca{

    private static final BigDecimal VALOR_DIARIA = new BigDecimal("35.00");

    @Override
    public Plano plano() {
        return Plano.DIARIA;
    }

    @Override
    public ResultadoCobranca calcular(ContextoCobranca contexto) {
        long diasPassados = ChronoUnit.DAYS.between(
                contexto.entrada().toLocalDate(), contexto.saida().toLocalDate());
        long diasIniciados = diasPassados + 1;

        BigDecimal valor = VALOR_DIARIA.multiply(BigDecimal.valueOf(diasIniciados));

        return new ResultadoCobranca(valor, Plano.DIARIA);
    }
}
