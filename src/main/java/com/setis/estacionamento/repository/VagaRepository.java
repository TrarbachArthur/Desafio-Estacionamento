package com.setis.estacionamento.repository;

import com.setis.estacionamento.domain.Vaga;
import com.setis.estacionamento.domain.enums.StatusVaga;
import com.setis.estacionamento.domain.enums.TipoVaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VagaRepository extends JpaRepository<Vaga, UUID> {
    boolean existsByCodigo(String codigo);

    @Query("""
            select v from Vaga v
            where (:tipoVaga is null or v.tipoVaga = :tipoVaga)
            and (:statusVaga is null or v.statusVaga = :statusVaga)
            order by v.codigo
            """)
    List<Vaga> buscaComFiltros(@Param("tipoVaga") TipoVaga tipoVaga,
                               @Param("statusVaga") StatusVaga statusVaga);
}

