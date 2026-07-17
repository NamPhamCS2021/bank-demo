//package com.example.demoSQL.exception;
//
//import com.example.demoSQL.dto.ApiResponse;
//import com.example.demoSQL.enums.ReturnMessage;
//import jakarta.persistence.EntityNotFoundException;
//import jakarta.validation.ConstraintViolationException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.context.request.WebRequest;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@ControllerAdvice
//@Slf4j
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(EntityNotFoundException.class)
//    public ResponseEntity<ApiResponse<Object>> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
//        log.error("Entity not found: {}", ex.getMessage());
//        ApiResponse<Object> response = new ApiResponse<>(
//            null,
//            ReturnMessage.NOT_FOUND.getCode(),
//            ex.getMessage()
//        );
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
//        log.error("Validation error: {}", ex.getMessage());
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors().forEach(error ->
//            errors.put(error.getField(), error.getDefaultMessage())
//        );
//
//        ApiResponse<Object> response = new ApiResponse<>(
//            errors,
//            ReturnMessage.BAD_REQUEST.getCode(),
//            "Validation failed"
//        );
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }
//
//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
//        log.error("Constraint violation: {}", ex.getMessage());
//        ApiResponse<Object> response = new ApiResponse<>(
//            null,
//            ReturnMessage.BAD_REQUEST.getCode(),
//            "Data constraint violation: " + ex.getMessage()
//        );
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }
//
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
//        log.error("Data integrity violation: {}", ex.getMessage());
//        String message = "Data integrity violation. This might be due to duplicate values or foreign key constraints.";
//        if (ex.getMessage().contains("Duplicate entry")) {
//            message = "Duplicate entry detected. Please check your data.";
//        }
//
//        ApiResponse<Object> response = new ApiResponse<>(
//            null,
//            ReturnMessage.CONFLICT.getCode(),
//            message
//        );
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
//        log.error("Access denied: {}", ex.getMessage());
//        ApiResponse<Object> response = new ApiResponse<>(
//            null,
//            ReturnMessage.FORBIDDEN.getCode(),
//            "Access denied: " + ex.getMessage()
//        );
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
//        log.error("Illegal argument: {}", ex.getMessage());
//        ApiResponse<Object> response = new ApiResponse<>(
//            null,
//            ReturnMessage.BAD_REQUEST.getCode(),
//            ex.getMessage()
//        );
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, WebRequest request) {
//        log.error("Unexpected error occurred: ", ex);
//        ApiResponse<Object> response = new ApiResponse<>(
//            null,
//            ReturnMessage.INTERNAL_SERVER_ERROR.getCode(),
//            "An unexpected error occurred. Please try again later."
//        );
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
//}