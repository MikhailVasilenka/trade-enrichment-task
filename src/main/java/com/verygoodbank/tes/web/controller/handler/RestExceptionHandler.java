package com.verygoodbank.tes.web.controller.handler;

import com.verygoodbank.tes.exception.InternalServerError;
import com.verygoodbank.tes.web.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({InternalServerError.class})
    public ResponseEntity<ErrorResponse> handleReadTradeFileException(Exception e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(), List.of(e.getMessage())), HttpStatus.BAD_REQUEST);
    }
}
