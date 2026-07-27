package com.setis.estacionamento.controller;

import com.setis.estacionamento.domain.Cliente;
import com.setis.estacionamento.dto.request.AtualizarClienteRequest;
import com.setis.estacionamento.dto.request.CriarClienteRequest;
import com.setis.estacionamento.dto.response.ClienteResponse;
import com.setis.estacionamento.mapper.ClienteMapper;
import com.setis.estacionamento.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrarCliente(@Valid @RequestBody CriarClienteRequest request, UriComponentsBuilder uriBuilder) {

        Cliente cliente = clienteService.criar(clienteMapper.fromDto(request));
        URI localizacao = uriBuilder.path("/v1/clientes/{id}").buildAndExpand(cliente.getId()).toUri();

        return ResponseEntity.created(localizacao).body(clienteMapper.toDto(cliente));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@PathVariable UUID id) {
        return clienteMapper.toDto(clienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarClienteRequest request) {
        return clienteMapper.toDto(clienteService.atualizar(id, request.nome(), request.assinaturaValidaAte()));
    }
}
