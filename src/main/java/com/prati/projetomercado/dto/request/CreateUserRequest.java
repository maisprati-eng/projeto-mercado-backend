package com.prati.projetomercado.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Username é obrigatório.")
        @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres.")
        String username,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Pattern(
            regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$",
            message = "E-mail deve conter um domínio com TLD válido (ex.: exemplo@dominio.com)."
        )
        String email,

        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
        String password,

        @NotBlank(message = "Confirmação de senha é obrigatória.")
        String confirmPassword
) {}
