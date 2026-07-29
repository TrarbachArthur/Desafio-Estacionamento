package com.setis.estacionamento.domain;

import com.setis.estacionamento.domain.enums.StatusVaga;
import com.setis.estacionamento.domain.enums.TipoVaga;
import com.setis.estacionamento.domain.enums.TipoVeiculo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vaga")
public class Vaga {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false, unique = true, length = 10)
    private String codigo;

    @Column (name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoVaga tipoVaga;

    @Column (name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusVaga statusVaga;

    public Vaga(String codigo, TipoVaga tipoVaga){
        this.codigo = codigo;
        this.tipoVaga = tipoVaga;
        this.statusVaga = StatusVaga.LIVRE;
    }

}
