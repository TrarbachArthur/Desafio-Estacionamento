package com.setis.estacionamento.mapper;

import com.setis.estacionamento.domain.Vaga;
import com.setis.estacionamento.dto.request.CriarVagaRequest;
import com.setis.estacionamento.dto.response.VagaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VagaMapper {

    VagaResponse toDto(Vaga vaga);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoVaga", source = "tipo")
    @Mapping(target = "statusVaga", ignore = true)
    Vaga fromDto(CriarVagaRequest request);
}
