package com.mist.glassabbey.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errMsg = ex.getBindingResult().getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation Failed");

        ErrorDto errDto = new ErrorDto(errMsg);
        return new ResponseEntity<>(errDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorDto> handleUnauthorizedExceptions(UnauthorizedException ex) {
        ErrorDto errDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorDto> handleForbiddenExceptions(ForbiddenException ex) {
        ErrorDto errDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errDto, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEntityNotFoundException(
            EntityNotFoundException ex) {
        ErrorDto errDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BidRejectedException.class)
    public ResponseEntity<ErrorDto> handleBidRejectedException(BidRejectedException ex) {
        ErrorDto errDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NwcException.class)
    public ResponseEntity<ErrorDto> handleNwcException(NwcException ex) {
        ErrorDto errDto = new ErrorDto(ex.getMessage());
        return new ResponseEntity<>(errDto, HttpStatus.CONFLICT);
    }
}
