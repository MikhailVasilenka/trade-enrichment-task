package com.verygoodbank.tes.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private final String response = "Error";
    private LocalDateTime timestamp;
    private List<String> messages;
}
