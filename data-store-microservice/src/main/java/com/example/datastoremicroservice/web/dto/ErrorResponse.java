package com.example.datastoremicroservice.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private Map<String, String> errors;

}
