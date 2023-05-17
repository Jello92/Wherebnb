package com.example.wherebnb.exception;

import com.example.wherebnb.dto.BasicResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity signValidException (MethodArgumentNotValidException exception){
        BindingResult bindingResult = exception.getBindingResult();

        StringBuilder builder = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("[");
            builder.append(fieldError.getField());
            builder.append("]");
            builder.append(fieldError.getDefaultMessage());
            builder.append(" ");
        }
        return new ResponseEntity(BasicResponseDto.addBadRequest(builder.toString()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ErrorResponseDto> handleErrorException (ErrorException e){
        ErrorResponseDto errorResponseDto = new ErrorResponseDto( e.getExceptionEnum().getStatus(), e.getMessage()) ;
        return ResponseEntity.status(e.getExceptionEnum().getStatus()).body(errorResponseDto);
    }
}
