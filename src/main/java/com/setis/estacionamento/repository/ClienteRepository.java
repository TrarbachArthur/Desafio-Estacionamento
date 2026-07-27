package com.setis.estacionamento.repository;

import com.setis.estacionamento.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    boolean existsByDocumento(String documento);
}
