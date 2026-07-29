package com.setis.estacionamento.repository;

import com.setis.estacionamento.domain.Ticket;
import com.setis.estacionamento.domain.enums.Plano;
import com.setis.estacionamento.domain.enums.StatusTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    boolean existsByPlacaAndStatus(String placa, StatusTicket status);


    @Query(value = """
            select t from Ticket t
            left join fetch t.vaga
            left join fetch t.cliente
            where (:placa is null or t.placa = :placa)
              and (:status is null or t.status = :status)
              and (:plano is null or t.plano = :plano)
              and (:inicio is null or t.entrada >= :inicio)
              and (:fim is null or t.entrada <= :fim)
            """,
            countQuery = """
            select count(t) from Ticket t
            where (:placa is null or t.placa = :placa)
              and (:status is null or t.status = :status)
              and (:plano is null or t.plano = :plano)
              and (:inicio is null or t.entrada >= :inicio)
              and (:fim is null or t.entrada <= :fim)
            """)
    Page<Ticket> buscarComFiltros(
            String placa,
            StatusTicket status,
            Plano plano,
            LocalDateTime inicio,
            LocalDateTime fim,
            Pageable pageable);
}
