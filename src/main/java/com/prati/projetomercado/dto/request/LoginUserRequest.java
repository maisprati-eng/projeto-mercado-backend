package com.prati.projetomercado.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginUserRequest(

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Pattern(
            regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$",
            message = "E-mail deve conter um domínio com TLD válido (ex.: exemplo@dominio.com)."
        )
        String email,

        @NotBlank(message = "Senha é obrigatória.")
        String password
) {}
