package com.setis.estacionamento.service;

import com.setis.estacionamento.domain.Cliente;
import com.setis.estacionamento.domain.Ticket;
import com.setis.estacionamento.domain.Vaga;
import com.setis.estacionamento.domain.cobranca.ContextoCobranca;
import com.setis.estacionamento.domain.cobranca.PoliticaFactory;
import com.setis.estacionamento.domain.cobranca.ResultadoCobranca;
import com.setis.estacionamento.domain.enums.Plano;
import com.setis.estacionamento.domain.enums.StatusTicket;
import com.setis.estacionamento.domain.enums.StatusVaga;
import com.setis.estacionamento.domain.enums.TipoVeiculo;
import com.setis.estacionamento.dto.request.AbrirTicketRequest;
import com.setis.estacionamento.exception.CodigoErro;
import com.setis.estacionamento.exception.ConflictException;
import com.setis.estacionamento.exception.NotFoundException;
import com.setis.estacionamento.exception.UnprocessableException;
import com.setis.estacionamento.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ClienteService clienteService;
    private final VagaService vagaService;
    private final PoliticaFactory politicaFactory;

    @Transactional
    public Ticket abrir(AbrirTicketRequest request) {
        // Garante regras relacionadas a cliente
        Cliente cliente = verificarCliente(request);

        // Garante regras relacionadas a entrada
        LocalDateTime entrada = resolverEntrada(request.entrada());

        if (ticketRepository.existsByPlacaAndStatus(request.placa(), StatusTicket.ABERTO)) {
            throw new ConflictException(CodigoErro.TICKET_ABERTO_EXISTENTE,
                    "Ja existe um ticket aberto para a placa %s".formatted(request.placa()));
        }

        Vaga vaga = vagaService.buscarPorId(request.vagaId());

        // Garante regras relacionadas a vaga
        verificarVaga(vaga, request.tipoVeiculo());

        Ticket ticket = new Ticket(request.placa(), request.tipoVeiculo(), request.plano(),
                vaga, cliente, entrada);

        // Ocupa a vaga apos criacao do ticket
        vaga.setStatusVaga(StatusVaga.OCUPADA);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket encerrar(UUID id, LocalDateTime saidaInformada) {
        Ticket ticket = this.buscarPorId(id);

        if (!ticket.getStatus().equals(StatusTicket.ABERTO)) {
            throw new ConflictException(CodigoErro.TICKET_NAO_ABERTO,
                    "O ticket %s esta %s e nao pode ser encerrado".formatted(id, ticket.getStatus()));
        }

        LocalDateTime saida = saidaInformada == null ? LocalDateTime.now() : saidaInformada;

        if (saida.isBefore(ticket.getEntrada())) {
            throw new UnprocessableException(CodigoErro.SAIDA_ANTERIOR_ENTRADA,
                    "A saida nao pode ser anterior a entrada.");
        }

        ContextoCobranca contexto = new ContextoCobranca(ticket.getEntrada(), saida, ticket.getCliente());
        ResultadoCobranca cobranca = politicaFactory.obterPolitica(ticket.getPlano())
                .calcular(contexto);

        ticket.setSaida(saida);
        ticket.setPlanoAplicado(cobranca.plano());
        ticket.setValorTotal(cobranca.valor());

        ticket.setStatus(StatusTicket.ENCERRADO);

        ticket.getVaga().setStatusVaga(StatusVaga.LIVRE);

        ticketRepository.save(ticket);
        return ticket;
    }

    @Transactional
    public Ticket buscarPorId(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket", id));
    }


    // Funcoes auxiliares

    private Cliente verificarCliente(AbrirTicketRequest request) {
        UUID clienteId = request.clienteId();
        Plano plano = request.plano();

        if (plano == Plano.MENSALISTA && clienteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O plano mensalista exige um cliente cadastrado");
        }
        if (plano != Plano.MENSALISTA && clienteId != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Apenas o plano mensalista registra clientes");
        }

        // Retorna 404 caso clienteId != null e cliente nao exista
        return plano == Plano.MENSALISTA ? clienteService.buscarPorId(clienteId) : null;
    }

    private LocalDateTime resolverEntrada(LocalDateTime entrada) {
        if (entrada == null) {
            return LocalDateTime.now();
        }
        if (entrada.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O horario de entrada nao pode ser futuro");
        }

        return entrada;
    }

    private void verificarVaga(Vaga vaga, TipoVeiculo tipoVeiculo) {
        if (vaga.getStatusVaga() != StatusVaga.LIVRE) {
            throw new ConflictException(CodigoErro.VAGA_INDISPONIVEL,
                    "A vaga %s tem status %s e nao esta disponivel".formatted(vaga.getCodigo(), vaga.getStatusVaga()));
        }
        if (!vaga.getTipoVaga().acomoda(tipoVeiculo)) {
            throw new UnprocessableException(CodigoErro.VAGA_INCOMPATIVEL,
                    "A vaga %s nao atende ao tipo de veiculo %s".formatted(vaga.getCodigo(), tipoVeiculo));
        }
    }
}
