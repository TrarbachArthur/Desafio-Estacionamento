package com.setis.estacionamento.domain.cobranca;

import com.setis.estacionamento.domain.Cliente;
import com.setis.estacionamento.domain.enums.Plano;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PoliticaMensalista implements PoliticaCobranca{
    private static final BigDecimal ISENTO = new BigDecimal("0.00");

    private final PoliticaAvulso politicaAvulso;

    @Override
    public Plano plano() {
        return Plano.MENSALISTA;
    }

    @Override
    public ResultadoCobranca calcular(ContextoCobranca contexto) {
        Cliente cliente = contexto.cliente();

        if (cliente.isAssinaturaVigenteEm(contexto.entrada().toLocalDate())) {
            return new ResultadoCobranca(ISENTO, Plano.MENSALISTA);
        }

        return politicaAvulso.calcular(contexto);
    }
}
