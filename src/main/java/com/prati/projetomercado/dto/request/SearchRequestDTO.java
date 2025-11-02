package com.prati.projetomercado.dto.request;


public record SearchRequestDTO(
        String fullName,
        String ageRange, // ex: "40-49"
        String street,
        String cityState,
        String phone,
        String email,
        String relativeFirstName,
        String relativeLastName
) {
    public SearchRequestDTO {
        if (fullName != null && fullName.isBlank()) fullName = null;
        if (ageRange != null && ageRange.isBlank()) ageRange = null;
        if (street != null && street.isBlank()) street = null;
        // ... (fazer o mesmo para todos os campos) ...
        if (email != null && email.isBlank()) email = null;
    }
}