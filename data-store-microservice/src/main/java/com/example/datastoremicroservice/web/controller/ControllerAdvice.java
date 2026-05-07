package com.example.datastoremicroservice.web.controller;

import com.example.datastoremicroservice.model.exception.SensorNotFoundException;
import com.example.datastoremicroservice.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(SensorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse sensorNotFound(SensorNotFoundException e) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                Map.of("error", "Sensor not found")
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse server(Exception e) {
        e.printStackTrace();
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                Map.of("error", "Something went wrong")
        );
    }

}
