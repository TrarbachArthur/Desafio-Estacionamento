package com.setis.estacionamento.domain;

import com.setis.estacionamento.domain.enums.Plano;
import com.setis.estacionamento.domain.enums.StatusTicket;
import com.setis.estacionamento.domain.enums.TipoVeiculo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table (name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 7)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_veiculo", nullable = false, length = 10)
    private TipoVeiculo tipoVeiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Plano plano;

    @Enumerated(EnumType.STRING)
    @Column(name="plano_aplicado", length = 20)
    private Plano planoAplicado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="vaga_id", nullable = false)
    private Vaga vaga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTicket status;

    @Column(nullable = false)
    private LocalDateTime entrada;

    @Column
    private LocalDateTime saida;

    @Column(name = "valor_total", precision = 19, scale = 2)
    private BigDecimal valorTotal;

    public Ticket(String placa, TipoVeiculo tipoVeiculo, Plano plano,
                  Vaga vaga, Cliente cliente, LocalDateTime entrada) {
        this.placa = placa;
        this.tipoVeiculo = tipoVeiculo;
        this.plano = plano;
        this.vaga = vaga;
        this.cliente = cliente;
        this.entrada = entrada;
        this.status = StatusTicket.ABERTO;
    }

}
