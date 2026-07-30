package com.setis.estacionamento.service;

import com.setis.estacionamento.domain.Vaga;
import com.setis.estacionamento.domain.enums.StatusVaga;
import com.setis.estacionamento.domain.enums.TipoVaga;
import com.setis.estacionamento.exception.BadRequestException;
import com.setis.estacionamento.exception.CodigoErro;
import com.setis.estacionamento.exception.ConflictException;
import com.setis.estacionamento.exception.NotFoundException;
import com.setis.estacionamento.repository.VagaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VagaService {

    private final VagaRepository vagaRepository;

    @Transactional
    public Vaga criar(Vaga vaga) {
        if (vagaRepository.existsByCodigo(vaga.getCodigo())) {
            throw new ConflictException(CodigoErro.CODIGO_VAGA_DUPLICADO,
                    "Ja existe uma vaga com o codigo %s".formatted(vaga.getCodigo()));
        }

        return vagaRepository.save(new Vaga(vaga.getCodigo(), vaga.getTipoVaga()));
    }

    @Transactional
    public List<Vaga> listar(TipoVaga tipoVaga, StatusVaga statusVaga) {
        return vagaRepository.buscaComFiltros(tipoVaga, statusVaga);
    }

    @Transactional
    public Vaga buscarPorId(UUID id) {
        return vagaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vaga", id));
    }

    @Transactional
    public Vaga atualizaStatus(UUID id, StatusVaga novoStatus) {
        Vaga vaga = this.buscarPorId(id);

        if (novoStatus == StatusVaga.OCUPADA) {
            throw new BadRequestException(CodigoErro.STATUS_NAO_PERMITIDO,
                    "O status %s nao pode ser definido manualmente"
                            .formatted(StatusVaga.OCUPADA));
        }
        if (novoStatus == StatusVaga.MANUTENCAO && vaga.getStatusVaga() == StatusVaga.OCUPADA) {
            throw new ConflictException(CodigoErro.VAGA_OCUPADA,
                    "A vaga %s esta %s e nao pode ir para %s"
                            .formatted(vaga.getCodigo(), StatusVaga.OCUPADA,  StatusVaga.MANUTENCAO));
        }

        vaga.setStatusVaga(novoStatus);
        return vagaRepository.save(vaga);
    }
}
