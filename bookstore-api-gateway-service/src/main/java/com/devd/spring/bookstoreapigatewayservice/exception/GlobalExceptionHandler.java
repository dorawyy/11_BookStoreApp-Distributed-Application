package com.devd.spring.bookstoreapigatewayservice.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.UUID;

/**
 * @author: Devaraj Reddy, Date : 2019-04-12 12:00
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RunTimeExceptionPlaceHolder.class)
    public ResponseEntity<ErrorResponse> handleCustomException(RunTimeExceptionPlaceHolder ex) {

        ErrorResponse errorResponse = populateErrorResponse(ex.getHttpStatus()+"", ex.getMessage()); // call // call
        log.error("Something went wrong, Exception : " + ex.getMessage());
        ex.printStackTrace(); 
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getHttpStatus())); // call

    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(InvalidFormatException ex) {

        ErrorResponse errorResponse = populateErrorResponse("400", ex.getMessage()); // call
        log.error("Something went wrong, Exception : " + ex.getMessage());
        ex.printStackTrace();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleCustomException(Exception ex) {

        ErrorResponse errorResponse = populateErrorResponse("500",  // call
                ex.getMessage());
        log.error("Something went wrong, Exception : " + ex.getMessage());
        ex.printStackTrace();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    public ErrorResponse populateErrorResponse(String code, String message) {
        ErrorResponse errorResponse = new ErrorResponse(); // call
        errorResponse.setUuid(UUID.randomUUID()); // call

        Error error = new Error(); // call
        error.setCode(code); // call
        error.setMessage(message); // call

        errorResponse.setErrors(Collections.singletonList(error)); // call

        return errorResponse;
    }
}
