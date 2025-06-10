package com.germogli.backend.authentication.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PasswordResetResponseDTO {
    private String message;
    private String token;
}
