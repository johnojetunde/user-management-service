package com.iddera.usermanagement.api.app.config;

import com.iddera.usermanagement.api.app.model.ResponseModel;
import com.iddera.usermanagement.api.domain.exception.UserManagementException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletionException;

import static com.iddera.usermanagement.api.app.util.ErrorFormatter.format;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Slf4j
public class RestControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {FileUploadException.class, NoSuchElementException.class})
    public ResponseEntity<ResponseModel> handleUploadException(Exception ex) {
        log.error("Unhandled exception encountered: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new ResponseModel(null, "", singletonList(ex.getMessage()), INTERNAL_SERVER_ERROR.value()), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {CompletionException.class})
    public ResponseEntity<ResponseModel> handleCompletionException(CompletionException ex) {
        if (ex.getCause() instanceof UserManagementException) {
            return handleDomainException((UserManagementException) ex.getCause());
        }
        return handleException(ex);
    }

    @ExceptionHandler(value = {UserManagementException.class})
    public ResponseEntity<ResponseModel> handleDomainException(UserManagementException ex) {
        log.error("Unhandled exception encountered: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                new ResponseModel(null, "", singletonList(ex.getMessage()), ex.getCode()),
                valueOf(ex.getCode()));
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<ResponseModel> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Unhandled exception encountered: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new ResponseModel(null, "", format(ex.getConstraintViolations()), BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<ResponseModel> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("Unhandled exception encountered: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new ResponseModel(null, "", singletonList(ex.getMessage()), FORBIDDEN.value()), FORBIDDEN);
    }

    @ExceptionHandler(value = {MultipartException.class})
    public ResponseEntity<ResponseModel> handleBadRequestException(Exception ex, WebRequest request) {
        log.error("Unhandled exception encountered: {}", ex.getMessage(), ex);
        String msg = String.format("Exception message [%s] %n Metadata [%s]",
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(new ResponseModel(null, "", singletonList(msg), BAD_REQUEST.value()), BAD_REQUEST);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ResponseModel> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Unhandled exception encountered: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new ResponseModel(null, "", singletonList(ex.getMessage()), BAD_REQUEST.value()), BAD_REQUEST);
    }

    @ExceptionHandler(value = {Throwable.class})
    public ResponseEntity<ResponseModel> handleException(Throwable ex) {
        log.error("Unhandled exception encountered: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new ResponseModel(null, "", singletonList(ex.getMessage()), INTERNAL_SERVER_ERROR.value()), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseModel> handleConverterErrors(MethodArgumentTypeMismatchException ex) {
        if (ex.getCause() instanceof IllegalArgumentException) {
            return handleIllegalArgumentException((IllegalArgumentException) ex.getCause());
        }
        return handleException(ex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        return new ResponseEntity<>(new ResponseModel(null, "", format(bindingResult), BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        return new ResponseEntity<>(
                new ResponseModel(null, "", singletonList(ex.getMessage()), BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }
}

