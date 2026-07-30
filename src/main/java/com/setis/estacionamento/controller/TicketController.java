package com.setis.estacionamento.controller;

import com.setis.estacionamento.domain.Ticket;
import com.setis.estacionamento.domain.enums.Plano;
import com.setis.estacionamento.domain.enums.StatusTicket;
import com.setis.estacionamento.dto.request.AbrirTicketRequest;
import com.setis.estacionamento.dto.request.EncerrarTicketRequest;
import com.setis.estacionamento.dto.response.TicketPageResponse;
import com.setis.estacionamento.dto.response.TicketResponse;
import com.setis.estacionamento.mapper.TicketMapper;
import com.setis.estacionamento.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketMapper ticketMapper;
    private final int tamanhoPadraoPag;
    private final int tamanhoMaximoPag;

    public  TicketController(TicketService ticketService, TicketMapper ticketMapper,
                             @Value("${app.paginacao.tamanho-padrao}") int tamanhoPadraoPag,
                             @Value("${app.paginacao.tamanho-maximo}") int tamanhoMaximoPag) {

        this.ticketService = ticketService;
        this.ticketMapper = ticketMapper;
        this.tamanhoPadraoPag = tamanhoPadraoPag;
        this.tamanhoMaximoPag = tamanhoMaximoPag;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> abrirTicket(@Valid @RequestBody AbrirTicketRequest request, UriComponentsBuilder uriBuilder) {
        Ticket ticket = ticketService.abrir(request);
        URI localizacao = uriBuilder.path("v1/tickets/{id}").buildAndExpand(ticket.getId()).toUri();

        return ResponseEntity.created(localizacao).body(ticketMapper.toDto(ticket));
    }

    @PostMapping("/{id}/encerramento")
    public TicketResponse encerrarTicket(@PathVariable UUID id, @RequestBody(required = false) EncerrarTicketRequest request) {
        LocalDateTime saida = request == null ? null : request.saida();

        return ticketMapper.toDto(ticketService.encerrar(id, saida));
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable UUID id) {
        return ticketMapper.toDto(ticketService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<TicketPageResponse<TicketResponse>> getTickets(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) StatusTicket status,
            @RequestParam(required = false) Plano plano,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {

        Pageable paginacao = PageRequest.of(
                Math.max(page, 0),
                handleTamanhoPagina(size),
                Sort.by(Sort.Direction.DESC, "entrada"));

        Page<Ticket> ticketPage = ticketService.buscarComFiltro(placa, status, plano,
                dataInicio, dataFim, paginacao);

        return ResponseEntity.ok(new TicketPageResponse<>(ticketPage, ticketMapper::toDto));
    }

    private int handleTamanhoPagina(Integer size) {
        if (size == null) {
            return this.tamanhoPadraoPag;
        }

        return Math.clamp(size, 1, tamanhoMaximoPag);
    }
}
