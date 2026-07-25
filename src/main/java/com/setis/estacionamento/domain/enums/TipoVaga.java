package com.setis.estacionamento.domain.enums;

import java.util.EnumSet;
import java.util.Set;

public enum TipoVaga {
    MOTO(EnumSet.of(TipoVeiculo.MOTO)),
    PADRAO(EnumSet.of(TipoVeiculo.MOTO, TipoVeiculo.CARRO)),
    GRANDE(EnumSet.of(TipoVeiculo.MOTO,  TipoVeiculo.CARRO, TipoVeiculo.VAN)),;

    private final Set<TipoVeiculo> veiculosAcomodados;

    TipoVaga(Set<TipoVeiculo> veiculosAcomodados) {
        this.veiculosAcomodados = veiculosAcomodados;
    }

    /** Verdadeiro se uma vaga deste tipo acomoda o veiculo informado. */
    public boolean acomoda(TipoVeiculo tipoVeiculo) {
        return veiculosAcomodados.contains(tipoVeiculo);
    }
}
