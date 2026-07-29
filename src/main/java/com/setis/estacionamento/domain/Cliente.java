package com.setis.estacionamento.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String documento;

    @Column(name = "assinatura_valida_ate", nullable = false)
    private LocalDate assinaturaValidaAte;

    public Cliente(String nome, String documento, LocalDate assinaturaValidaAte) {
        this.nome = nome;
        this.documento = documento;
        this.assinaturaValidaAte = assinaturaValidaAte;
    }

    public boolean isAssinaturaVigenteEm(LocalDate data) {
        return !assinaturaValidaAte.isBefore(data);
    }

    public boolean isAssinaturaVigente() { return isAssinaturaVigenteEm(LocalDate.now());
    }
}
