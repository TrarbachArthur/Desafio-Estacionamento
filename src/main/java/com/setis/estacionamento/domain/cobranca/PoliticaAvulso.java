package com.setis.estacionamento.domain.cobranca;

import com.setis.estacionamento.domain.enums.Plano;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class PoliticaAvulso implements PoliticaCobranca{

    private static final long TOLERANCIA_MINUTOS = 15;

    private static final BigDecimal PRIMEIRA_HORA = new BigDecimal("10.00");
    private static final BigDecimal HORA_ADICIONAL = new BigDecimal("5.00");
    private static final BigDecimal TETO_POR_PERIODO = new BigDecimal("50.00");
    private static final BigDecimal ISENTO = new BigDecimal("0.00");

    @Override
    public Plano plano() {
        return Plano.AVULSO;
    }

    @Override
    public ResultadoCobranca calcular(ContextoCobranca contexto) {
        return new ResultadoCobranca(calcularValor(contexto), Plano.AVULSO);
    }

    private BigDecimal calcularValor(ContextoCobranca contexto) {
        long minutos = Duration.between(contexto.entrada(), contexto.saida()).toMinutes();

        if (minutos <= TOLERANCIA_MINUTOS) {
            return ISENTO;
        }

        long horasCobradas = Math.ceilDiv(minutos, 60);
        BigDecimal custoAdicional = HORA_ADICIONAL.multiply(BigDecimal.valueOf(horasCobradas - 1));
        BigDecimal valor = PRIMEIRA_HORA.add(custoAdicional);

        long periodos = Math.ceilDiv(horasCobradas, 24);
        BigDecimal teto = TETO_POR_PERIODO.multiply(BigDecimal.valueOf(periodos));

        // minimo(valor, teto)
        return valor.min(teto);
    }
}
