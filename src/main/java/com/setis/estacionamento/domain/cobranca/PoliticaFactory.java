package com.setis.estacionamento.domain.cobranca;

import com.setis.estacionamento.domain.enums.Plano;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PoliticaFactory {

    private final Map<Plano, PoliticaCobranca> politicasMap;

    public PoliticaFactory(List<PoliticaCobranca> politicas) {
        this.politicasMap = new EnumMap<>(Plano.class);

        for (PoliticaCobranca p : politicas) {
            politicasMap.put(p.plano(), p);
        }
    }

    public PoliticaCobranca obterPolitica(Plano plano) {
        PoliticaCobranca politica = politicasMap.get(plano);

        if (politica == null) {
            throw new IllegalArgumentException("Nenhuma politica de pagamento para o plano: " + plano);
        }

        return politica;
    }
}
