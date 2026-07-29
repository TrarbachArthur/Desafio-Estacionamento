package com.setis.estacionamento.domain.cobranca;

import com.setis.estacionamento.domain.enums.Plano;

public interface PoliticaCobranca {

    Plano plano();

    ResultadoCobranca calcular(ContextoCobranca contexto);
}
