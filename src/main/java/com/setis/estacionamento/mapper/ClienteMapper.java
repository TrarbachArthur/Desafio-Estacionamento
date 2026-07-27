package com.setis.estacionamento.mapper;

import com.setis.estacionamento.domain.Cliente;
import com.setis.estacionamento.dto.request.CriarClienteRequest;
import com.setis.estacionamento.dto.response.ClienteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {
    @Mapping(target = "assinaturaVigente", expression = "java(cliente.isAssinaturaVigente())")
    ClienteResponse toDto(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    Cliente fromDto(CriarClienteRequest criarClienteRequest);
}
