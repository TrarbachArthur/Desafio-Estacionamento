package com.setis.estacionamento.domain.cobranca;

import com.setis.estacionamento.domain.Cliente;
import com.setis.estacionamento.domain.enums.Plano;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaCobrancaTest {
    private final PoliticaAvulso politicaAvulso = new PoliticaAvulso();
    private final PoliticaDiaria politicaDiaria = new PoliticaDiaria();
    private final PoliticaMensalista politicaMensalista = new PoliticaMensalista(politicaAvulso);

    // Plano AVULSO
    @ParameterizedTest(name = "AVULSO: {0} ate {1} -> {2} ({3})")
    @CsvSource({
            "2026-03-10T08:00:00, 2026-03-10T08:10:00,   0.00, AVULSO",
            "2026-03-10T08:00:00, 2026-03-10T08:15:00,   0.00, AVULSO",
            "2026-03-10T08:00:00, 2026-03-10T08:16:00,  10.00, AVULSO",
            "2026-03-10T08:00:00, 2026-03-10T09:00:00,  10.00, AVULSO",
            "2026-03-10T08:00:00, 2026-03-10T09:01:00,  15.00, AVULSO",
            "2026-03-10T08:00:00, 2026-03-10T11:30:00,  25.00, AVULSO",
            "2026-03-10T08:00:00, 2026-03-10T18:00:00,  50.00, AVULSO",
            "2026-03-10T08:00:00, 2026-03-11T10:00:00, 100.00, AVULSO",
    })
    void avulso(LocalDateTime entrada, LocalDateTime saida, BigDecimal valorEsperado, Plano planoEsperado) {
        ResultadoCobranca r = politicaAvulso.calcular(ContextoCobranca.de(entrada, saida));

        verificaValor(r, valorEsperado);
        verificaEscala(r);
        verificaPlanoAplicado(r, planoEsperado);
    }

    // Plano DIARIA
    @ParameterizedTest(name = "DIARIA: {0} ate {1} -> {2} ({3})")
    @CsvSource({
            "2026-03-10T08:00:00, 2026-03-10T20:00:00, 35.00, DIARIA",
            "2026-03-10T23:00:00, 2026-03-11T01:00:00, 70.00, DIARIA",
    })
    void diaria(LocalDateTime entrada, LocalDateTime saida, BigDecimal valorEsperado, Plano planoEsperado) {
        ResultadoCobranca r = politicaDiaria.calcular(ContextoCobranca.de(entrada, saida));

        verificaValor(r, valorEsperado);
        verificaEscala(r);
        verificaPlanoAplicado(r, planoEsperado);
    }

    // Plano MENSALISTA - entrada, saida, esperado, planoEsperado, assinaturaValidaAte
    @ParameterizedTest(name = "MENSALISTA: {0} ate {1} -> {2} ({3})")
    @CsvSource({
            "2026-03-10T08:00:00, 2026-03-10T20:00:00, 0.00, MENSALISTA, 2026-03-10",
            "2026-03-10T08:00:00, 2026-03-10T09:01:00, 15.00, AVULSO, 2026-03-09",
    })
    void mensalista(LocalDateTime entrada, LocalDateTime saida, BigDecimal valorEsperado, Plano planoEsperado,
                LocalDate assinaturaValidaAte) {

        Cliente cliente = new Cliente("Nome Teste", "11111111111", assinaturaValidaAte);

        ResultadoCobranca r = politicaMensalista.calcular(new ContextoCobranca(entrada, saida, cliente));

        verificaValor(r, valorEsperado);
        verificaEscala(r);
        verificaPlanoAplicado(r, planoEsperado);
    }

    void verificaValor(ResultadoCobranca r, BigDecimal valorEsperado) {
        assertThat(r.valor()).isEqualByComparingTo(valorEsperado);
    }

    void verificaEscala(ResultadoCobranca r) {
        assertThat(r.valor().scale()).isEqualTo(2);
    }

    void verificaPlanoAplicado(ResultadoCobranca r, Plano planoEsperado) {
        assertThat(r.plano()).isEqualTo(planoEsperado);
    }
}
