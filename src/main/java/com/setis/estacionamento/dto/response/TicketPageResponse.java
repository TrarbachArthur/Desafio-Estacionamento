package com.setis.estacionamento.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record TicketPageResponse<T>(
        List<T> conteudo,
        int paginaAtual,
        int tamanho,
        long totalElementos,
        int totalPaginas,
        boolean ultimaPagina
) {

    // Mapper e responsavel por converter o objeto JPA para DTO
    // Necessario ja que, por decisao tecnica, o controller e responsavel por mapear o objeto para DTO

    public <E> TicketPageResponse(Page<E> page, Function<E, T> mapper) {
        this(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
