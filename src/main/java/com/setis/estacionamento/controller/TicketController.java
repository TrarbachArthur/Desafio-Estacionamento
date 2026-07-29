package com.setis.estacionamento.controller;

import com.setis.estacionamento.domain.Ticket;
import com.setis.estacionamento.dto.request.AbrirTicketRequest;
import com.setis.estacionamento.dto.request.EncerrarTicketRequest;
import com.setis.estacionamento.dto.response.TicketResponse;
import com.setis.estacionamento.mapper.TicketMapper;
import com.setis.estacionamento.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketMapper ticketMapper;

    @PostMapping
    public ResponseEntity<TicketResponse> abrirTicket(@Valid @RequestBody AbrirTicketRequest request, UriComponentsBuilder uriBuilder) {
        Ticket ticket = ticketService.abrir(request);
        URI localizacao = uriBuilder.path("/tickets/{id}").buildAndExpand(ticket.getId()).toUri();

        return ResponseEntity.created(localizacao).body(ticketMapper.toDto(ticket));
    }

    @PostMapping("/{id}/encerramento")
    public TicketResponse encerrarTicket(@PathVariable UUID id, @RequestBody(required = false) EncerrarTicketRequest request) {
        LocalDateTime saida = request == null ? null : request.saida();

        return ticketMapper.toDto(ticketService.encerrar(id, saida));
    }
}
