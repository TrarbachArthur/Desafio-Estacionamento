package com.setis.estacionamento.dto.response;

import com.setis.estacionamento.domain.enums.Plano;
import com.setis.estacionamento.domain.enums.StatusTicket;
import com.setis.estacionamento.domain.enums.TipoVeiculo;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String placa,
        TipoVeiculo tipoVeiculo,
        Plano plano,
        Plano planoAplicado,
        TicketClienteResponse cliente,
        TicketVagaResponse vaga,
        StatusTicket status,
        LocalDateTime entrada,
        LocalDateTime saida,
        long permanenciaMinutos,
        String valorTotal
) {
}
