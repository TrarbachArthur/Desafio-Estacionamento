package com.setis.estacionamento.domain.cobranca;

import com.setis.estacionamento.domain.Cliente;
import org.hibernate.grammars.hql.HqlParser;

import java.time.LocalDateTime;
import java.util.Objects;

public record ContextoCobranca(
        LocalDateTime entrada,
        LocalDateTime saida,
        Cliente cliente
) {
    public ContextoCobranca {
        Objects.requireNonNull(entrada, "entrada");
        Objects.requireNonNull(saida, "saida");
    }

    public static ContextoCobranca de(LocalDateTime entrada, LocalDateTime saida) {
        return new ContextoCobranca(entrada, saida, null);
    }
}
