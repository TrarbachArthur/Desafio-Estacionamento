package com.setis.estacionamento.mapper;

import com.setis.estacionamento.domain.Ticket;
import com.setis.estacionamento.dto.response.TicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = VagaMapper.class)
public interface TicketMapper {
    @Mapping(target = "permanenciaMinutos", defaultExpression = "java(ticket.getPermanenciaMinutos())")
    TicketResponse toDto(Ticket ticket);
}
