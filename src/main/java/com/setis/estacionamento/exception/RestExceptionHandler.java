package com.setis.estacionamento.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ExceptionResponse> handleGeneralException(GeneralException ex, HttpServletRequest request) {
        ExceptionResponse corpo = new ExceptionResponse(
                ex.getStatus().value(), ex.getCodigo(), ex.getMessage(), request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(corpo);
    }

    // 400 - Falhou Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<Erro> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toErro)
                .sorted(Comparator.comparing(Erro::campo).thenComparing(Erro::mensagem))
                .toList();

        ExceptionResponse corpo = new ExceptionResponse(
                ex.getStatusCode().value(), CodigoErro.VALIDACAO,
                "Requisicao invalida", request.getRequestURI(), erros);

        return ResponseEntity.status(ex.getStatusCode()).body(corpo);
    }

    // 400 - Corpo ilegivel

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,  HttpServletRequest request) {
        Erro erro = null;

        if (ex.getCause() instanceof InvalidFormatException ife) {
            String path = ife.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : String.valueOf(ref.getIndex()))
                    .collect(Collectors.joining("."));

            Class<?> tipoEsperado = ife.getTargetType();

            erro = new Erro(path, handleEsperado(tipoEsperado));
        }

        ExceptionResponse corpo = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(), CodigoErro.CORPO_INVALIDO,
                "Corpo invalido: JSON malformado ou valor incompativel",
                request.getRequestURI(), erro != null ? List.of(erro) : List.of()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    // 400 - Valor invalido

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        Erro erro = null;

        if (ex.getCause() instanceof InvalidFormatException ife) {
            String path = ife.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : String.valueOf(ref.getIndex()))
                    .collect(Collectors.joining("."));

            Class<?> tipoEsperado = ife.getTargetType();

            erro = new Erro(path, handleEsperado(tipoEsperado));
        }


        ExceptionResponse corpo = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(), CodigoErro.PARAMETRO_INVALIDO,
                "Valor invalido para %s: %s".formatted(ex.getName(), ex.getValue())
                , request.getRequestURI(), erro != null ? List.of(erro) : List.of()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    // 400 - Parametros obrigatorios ausentes
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        ExceptionResponse corpo = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(), CodigoErro.PARAMETRO_OBRIGATORIO_AUSENTE,
                "O parametro %s e obrigatorio".formatted(ex.getParameterName()),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    private Erro toErro(FieldError fieldError) {
        if (fieldError.isBindingFailure()) {
            return new Erro(fieldError.getField(),
                    "valor invalido: %s".formatted(fieldError.getRejectedValue()));
        }

        String mensagem = fieldError.getDefaultMessage() == null ?
                "valor invalido" : fieldError.getDefaultMessage();

        return new Erro(fieldError.getField(), mensagem);
    }

    private String handleEsperado(Class<?> tipoEsperado) {
        if (tipoEsperado == null) {
            return "";
        }
        if (tipoEsperado.isEnum()) {
            String aceitos = Arrays.stream(tipoEsperado.getEnumConstants())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            return " Valores aceitos: %s.".formatted(aceitos);
        }
        if (tipoEsperado == LocalDate.class) {
            return " Formato esperado: yyyy-MM-dd.";
        }
        if (tipoEsperado == LocalDateTime.class) {
            return " Formato esperado: yyyy-MM-ddTHH:mm:ss.";
        }
        if (tipoEsperado == UUID.class) {
            return " Formato esperado: UUID.";
        }
        return "";
    }
}
