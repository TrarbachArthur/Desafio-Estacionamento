package com.setis.estacionamento.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

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

    // 404 - Rota inexistente
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {

        ExceptionResponse corpo = new ExceptionResponse(
                ex.getStatusCode().value(), CodigoErro.RECURSO_NAO_ENCONTRADO,
                "Rota nao encontrada", request.getRequestURI()
        );

        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    // 405 - verbo HTTP nao suportado
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        ExceptionResponse corpo = new ExceptionResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(), CodigoErro.METODO_NAO_SUPORTADO,
                "O metodo %s nao e suportado neste recurso".formatted(ex.getMethod()),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(corpo);
    }

    // 415 - Content-type nao suportado
    @ExceptionHandler(HttpMediaTypeException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMediaTypeException(
            HttpMediaTypeException ex, HttpServletRequest request) {

        ExceptionResponse corpo = new ExceptionResponse(
                ex.getStatusCode().value(), CodigoErro.MIDIA_NAO_SUPORTADA,
                "Content-type nao suportado. Utilize application/json.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(corpo);
    }

    // Fallback - Caso alguma excecao nao tenha sido devidamente tratada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(
            Exception ex, HttpServletRequest request) {

        log.error("Excecao nao tratada em {} {}", request.getMethod(), request.getRequestURI(), ex);

        ExceptionResponse corpo = new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), CodigoErro.ERRO_INTERNO,
                "Erro interno. Tente novamente ou contate um admin", request.getRequestURI()
        );

        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
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
