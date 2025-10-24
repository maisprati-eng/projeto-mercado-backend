package com.prati.projetomercado.advice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração de "caixa-preta" para garantir que o ExceptionAdvice
 * (e overrides da ResponseEntityExceptionHandler) padronize o JSON de erro.
 * <p>
 * Cenários cobertos:
 * - 400 com validação no /auth/register (senha curta)
 * - 404 de rota inexistente (precisa do opt-in no application.yml)
 * - 405 Method Not Allowed com lista de métodos suportados
 * - 400 JSON malformado
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ExceptionAdviceTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("POST /auth/register com senha curta -> 400 JSON padronizado")
    void registerSenhaCurta() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"a@a.com","password":"123"}                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/auth/register"));
    }

    @Test
    @DisplayName("Rota inexistente -> 404 JSON 'Rota não encontrada'")
    void rotaInexistente404() throws Exception {
        mockMvc.perform(get("/__nao_existe__"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Rota não encontrada"))
                .andExpect(jsonPath("$.path").value("/__nao_existe__"));
    }

    @Test
    @DisplayName("DELETE /auth/register (método errado) -> 405 JSON com lista de métodos")
    void methodNotAllowed405() throws Exception {
        mockMvc.perform(delete("/auth/register"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.message", startsWith("Método não suportado.")));
    }

    @Test
    @DisplayName("JSON malformado -> 400 JSON 'JSON malformado ou tipo incompatível.'")
    void jsonMalformado400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("JSON malformado ou tipo incompatível."));
    }
}
