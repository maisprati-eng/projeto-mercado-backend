package com.prati.projetomercado.controller; import com.prati.projetomercado.dto.handlers.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.responses.ApiResponse; import io.swagger.v3.oas.annotations.tags.Tag; import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/version")
@RequiredArgsConstructor
@Tag(name = "Versão", description = "Endpoint para verificar a versão da aplicação")
public class VersionController {

    private final BuildProperties buildProperties;

    @Operation(summary = "Retorna a versão atual da aplicação",
            description = "Verifica a versão definida no pom.xml do projeto.")
    @ApiResponse(responseCode = "200", description = "Versão retornada com sucesso")
    @GetMapping
    public ResponseEntity<ResponseHandler<Map<String, String>>> getVersion() {
        Map<String, String> versionInfo = new HashMap<>();
        versionInfo.put("version", buildProperties.getVersion());
        versionInfo.put("buildTime", buildProperties.getTime().toString());

        return ResponseEntity.ok(ResponseHandler.success("Informações de versão", versionInfo));
    }
}