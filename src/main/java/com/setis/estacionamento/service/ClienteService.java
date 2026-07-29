package com.setis.estacionamento.service;

import com.setis.estacionamento.domain.Cliente;
import com.setis.estacionamento.exception.CodigoErro;
import com.setis.estacionamento.exception.ConflictException;
import com.setis.estacionamento.exception.NotFoundException;
import com.setis.estacionamento.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente criar(Cliente cliente) {
        if (clienteRepository.existsByDocumento(cliente.getDocumento())) {
            throw new ConflictException(CodigoErro.DOCUMENTO_DUPLICADO,
                    "O documento informado ja esta associado a um cliente");
        }

        return clienteRepository.save(
                new Cliente(cliente.getNome(), cliente.getDocumento(), cliente.getAssinaturaValidaAte()));
    }

    @Transactional
    public Cliente buscarPorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente", id));
    }

    @Transactional
    public Cliente atualizar(UUID id, String nome, LocalDate assinaturaValidaAte) {
        Cliente cliente = this.buscarPorId(id);

        // Validade da assinatura pode ser reduzida (premissa assumida), por isso não há nenhuma verificacao

        cliente.setNome(nome);
        cliente.setAssinaturaValidaAte(assinaturaValidaAte);

        return clienteRepository.save(cliente);
    }
}
