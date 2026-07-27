package com.setis.estacionamento.controller;

import com.setis.estacionamento.domain.Vaga;
import com.setis.estacionamento.domain.enums.StatusVaga;
import com.setis.estacionamento.domain.enums.TipoVaga;
import com.setis.estacionamento.dto.request.AlterarStatusVagaRequest;
import com.setis.estacionamento.dto.request.CriarVagaRequest;
import com.setis.estacionamento.dto.response.VagaResponse;
import com.setis.estacionamento.mapper.VagaMapper;
import com.setis.estacionamento.service.VagaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/vagas")
@RequiredArgsConstructor
public class VagaController {

    private final VagaService vagaService;
    private final VagaMapper vagaMapper;

    @PostMapping
    public ResponseEntity<VagaResponse> criaVaga(@Valid @RequestBody CriarVagaRequest request, UriComponentsBuilder uriBuilder) {
        Vaga vaga = vagaService.criar(vagaMapper.fromDto(request));
        URI localizacao = uriBuilder.path("v1/vagas/{id}").buildAndExpand(vaga.getId()).toUri();

        return ResponseEntity.created(localizacao).body(vagaMapper.toDto(vaga));
    }

    @GetMapping
    public List<VagaResponse> listar(
            @RequestParam(required = false) TipoVaga tipo,
            @RequestParam(required = false) StatusVaga status) {

        return vagaService.listar(tipo, status).stream()
                .map(vagaMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public VagaResponse buscarPorId(@PathVariable UUID id) {
        return vagaMapper.toDto(vagaService.buscarPorId(id));
    }

    @PutMapping("/{id}/status")
    public VagaResponse atualizaStatus(@PathVariable UUID id,
                                       @Valid @RequestBody AlterarStatusVagaRequest request) {
        return vagaMapper.toDto(vagaService.atualizaStatus(id, request.status()));
    }
}
