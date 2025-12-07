package com.rendaxx.labs.repository.support;

import com.rendaxx.labs.exceptions.BadRequestException;
import com.rendaxx.labs.exceptions.InternalServerException;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.stereotype.Component;

@Component
public class RepositoryGuard {

    public <T> T execute(Supplier<? extends T> operation) {
        T result = executeNullable(operation);
        if (result == null) {
            throw new InternalServerException("Repository operation returned null");
        }
        return result;
    }

    public <T> @Nullable T executeNullable(Supplier<? extends T> operation) {
        try {
            return operation.get();
        } catch (ConstraintViolationException ex) {
            throw new BadRequestException(buildConstraintViolationMessage(ex), ex);
        } catch (PropertyReferenceException ex) {
            throw new BadRequestException(buildPropertyReferenceMessage(ex), ex);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException(extractMessage(ex), ex);
        } catch (DataAccessException ex) {
            throw new InternalServerException(ex);
        }
    }

    public void execute(Runnable operation) {
        executeNullable(() -> {
            operation.run();
            return null;
        });
    }

    private String buildConstraintViolationMessage(ConstraintViolationException exception) {
        if (exception.getConstraintViolations() == null
                || exception.getConstraintViolations().isEmpty()) {
            return "Validation failed";
        }
        String message = exception.getConstraintViolations().stream()
                .map(cv -> {
                    String path =
                            cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "";
                    if (!path.isBlank()) {
                        return path + ": " + cv.getMessage();
                    }
                    return cv.getMessage();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));
        return message.isBlank() ? "Validation failed" : message;
    }

    private String buildPropertyReferenceMessage(PropertyReferenceException exception) {
        String propertyName = exception.getPropertyName();
        String typeName = exception.getType().getType().getSimpleName();
        return String.format("Cannot sort by '%s' for resource '%s'", propertyName, typeName);
    }

    private String extractMessage(@Nullable Throwable throwable) {
        if (throwable == null) {
            return "Operation failed";
        }
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String message = root.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.getMessage();
        }
        if (message == null || message.isBlank()) {
            return "Operation failed";
        }
        return message;
    }
}
