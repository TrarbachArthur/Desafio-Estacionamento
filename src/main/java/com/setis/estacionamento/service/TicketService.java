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
import com.setis.estacionamento.exception.*;
import com.setis.estacionamento.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Transactional(readOnly = true)
    public Ticket buscarPorId(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket", id));
    }


    @Transactional(readOnly = true)
    public Page<Ticket> buscarComFiltro(
            String placa, StatusTicket status, Plano plano,
            LocalDate dataInicio, LocalDate dataFim, Pageable paginacao) {

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new BadRequestException(CodigoErro.PERIODO_INVALIDO,
                    "dataInicio (%s) nao pode ser posterior a dataFim (%s)".formatted(dataInicio, dataFim));
        }

        // Garantindo que o dia inteiro e considerado no filtro (inclusivo)
        LocalDateTime inicio = dataInicio == null ? null : dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim == null ? null : dataFim.atTime(LocalTime.MAX);

        placa = placa == null ? null : placa.trim().toUpperCase();

        return ticketRepository.buscarComFiltros(placa, status, plano, inicio, fim, paginacao);
    }

    // Funcoes auxiliares

    private Cliente verificarCliente(AbrirTicketRequest request) {
        UUID clienteId = request.clienteId();
        Plano plano = request.plano();

        if (plano == Plano.MENSALISTA && clienteId == null) {
            throw new BadRequestException(CodigoErro.CLIENTE_OBRIGATORIO,
                    "O plano %s exige um cliente cadastrado".formatted(plano));
        }
        if (plano != Plano.MENSALISTA && clienteId != null) {
            throw new BadRequestException(CodigoErro.CLIENTE_NAO_PERMITIDO,
                    "O plano %s nao permite o registro de clientes".formatted(plano));
        }

        // Retorna 404 caso clienteId != null e cliente nao exista
        return plano == Plano.MENSALISTA ? clienteService.buscarPorId(clienteId) : null;
    }

    private LocalDateTime resolverEntrada(LocalDateTime entrada) {
        if (entrada == null) {
            return LocalDateTime.now();
        }
        if (entrada.isAfter(LocalDateTime.now())) {
            throw new BadRequestException(CodigoErro.ENTRADA_FUTURA,
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
