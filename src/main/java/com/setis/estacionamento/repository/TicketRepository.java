package com.setis.estacionamento.repository;

import com.setis.estacionamento.domain.Ticket;
import com.setis.estacionamento.domain.enums.StatusTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    boolean existsByPlacaAndStatus(String placa, StatusTicket status);

}
