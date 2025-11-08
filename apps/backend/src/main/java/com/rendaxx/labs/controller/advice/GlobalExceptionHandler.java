package com.rendaxx.labs.controller.advice;

import com.rendaxx.labs.exceptions.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException exception) {
        return plainText(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(RuntimeException exception) {
        return plainText(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(cv -> {
                    String path =
                            cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "";
                    if (!path.isBlank()) {
                        return path + ": " + cv.getMessage();
                    }
                    return cv.getMessage();
                })
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Validation failed";
        }
        return plainText(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleBindingErrors(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Validation failed";
        }
        return plainText(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleUnreadablePayload(HttpMessageNotReadableException exception) {
        return plainText(HttpStatus.BAD_REQUEST, extractMessage(exception));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParam(MissingServletRequestParameterException exception) {
        return plainText(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        HttpStatus status = isPathVariable(exception) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        String message = exception.getMessage();
        if (status == HttpStatus.NOT_FOUND) {
            message = "Resource not found";
        }
        return plainText(status, message);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<String> handleInvalidSort(PropertyReferenceException exception) {
        String message = String.format(
                "Cannot sort by '%s' for resource '%s'",
                exception.getPropertyName(), exception.getType().getType().getSimpleName());
        return plainText(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleIntegrityViolation(DataIntegrityViolationException exception) {
        return plainText(HttpStatus.BAD_REQUEST, extractMessage(exception));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleUnsupportedMethod(HttpRequestMethodNotSupportedException exception) {
        ResponseEntity.BodyBuilder builder =
                ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).contentType(MediaType.TEXT_PLAIN);
        if (exception.getSupportedHttpMethods() != null
                && !exception.getSupportedHttpMethods().isEmpty()) {
            builder = builder.allow(exception.getSupportedHttpMethods().toArray(HttpMethod[]::new));
        }
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = String.format("Request method '%s' is not supported", exception.getMethod());
        }
        return builder.body(message);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        return plainText(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<String> plainText(HttpStatus status, String message) {
        String body = (message == null || message.isBlank()) ? status.getReasonPhrase() : message;
        return ResponseEntity.status(status).contentType(MediaType.TEXT_PLAIN).body(body);
    }

    private String extractMessage(Throwable throwable) {
        if (throwable == null) {
            return "Bad request";
        }
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return Objects.requireNonNullElse(root.getMessage(), throwable.getMessage());
    }

    private boolean isPathVariable(MethodArgumentTypeMismatchException exception) {
        return exception.getParameter().hasParameterAnnotation(PathVariable.class);
    }
}
