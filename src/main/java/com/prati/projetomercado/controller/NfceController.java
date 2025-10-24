package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.request.NfcePatchRequest;
import com.prati.projetomercado.dto.request.NfceRequest;
import com.prati.projetomercado.dto.response.NfceResponse;
import com.prati.projetomercado.dto.response.StatesResponse;
import com.prati.projetomercado.dto.response.SuccessResponse;
import com.prati.projetomercado.service.nfce.NfceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/nfces")
@RequiredArgsConstructor
@Tag(name = "NFC-e",
        description = "Endpoints para processamento de Notas Fiscais de Consumidor Eletrônicas")
public class NfceController {

    private final NfceService nfceService;

    @GetMapping("/")
    public ResponseEntity<SuccessResponse<List<NfceResponse>>> getAll(@RequestHeader("Authorization") String authorization) {
        List<NfceResponse> data = nfceService.getAll(authorization);

        if (data.isEmpty()) {
            return ResponseEntity.ok(new SuccessResponse<>("Nenhuma nota fiscal encontrada."));
        }

        return ResponseEntity.ok(new SuccessResponse<>("Notas fiscais encontradas com sucesso.", data));
    }

    @GetMapping("/{access-key}")
    public ResponseEntity<SuccessResponse<NfceResponse>> getOne(@RequestHeader("Authorization") String authorization,
                                                                @PathVariable("access-key") String accessKey) {
        NfceResponse data = nfceService.getOne(authorization, accessKey);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal encontrada com sucesso.", data));
    }

    @GetMapping("/states")
    public ResponseEntity<SuccessResponse<StatesResponse>> getStates(@RequestHeader("Authorization") String authorization) {
        StatesResponse data = nfceService.getStates(authorization);
        return ResponseEntity.ok(new SuccessResponse<>("Lista de estados implementados encontrada com sucesso.", data));
    }

    @PostMapping("/from-url")
    public ResponseEntity<SuccessResponse<NfceResponse>> registerLink(@RequestHeader("Authorization") String authorization,
                                                                      @RequestBody UrlRequest request) {
        NfceResponse data = nfceService.registerLink(authorization, request.getUrl());
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal pelo link cadastrada com sucesso.", data));
    }

    @PostMapping("/")
    public ResponseEntity<SuccessResponse<NfceResponse>> registerManual(@RequestHeader("Authorization") String authorization,
                                                                        @RequestBody NfceRequest manualData) {
        NfceResponse data = nfceService.registerManual(authorization, manualData);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal manual cadastrada com sucesso.", data));
    }

    @PutMapping("/{access-key}")
    public ResponseEntity<SuccessResponse<NfceResponse>> edit(@RequestHeader("Authorization") String authorization,
                                                              @PathVariable("access-key") String accessKey,
                                                              @RequestBody NfceRequest updatedNfce) {
        NfceResponse data = nfceService.edit(authorization, accessKey, updatedNfce);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal editada com sucesso.", data));
    }

    @PatchMapping("/{access-key}")
    public ResponseEntity<SuccessResponse<NfceResponse>> patch(@RequestHeader("Authorization") String authorization,
                                                               @PathVariable("access-key") String accessKey,
                                                               @RequestBody NfcePatchRequest patchNfce) {
        NfceResponse data = nfceService.patch(authorization, accessKey, patchNfce);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal editada com sucesso.", data));
    }

    @DeleteMapping("/{access-key}")
    public ResponseEntity<SuccessResponse<Void>> delete(@RequestHeader("Authorization") String authorization,
                                                        @PathVariable("access-key") String accessKey) {
        nfceService.delete(authorization, accessKey);
        return ResponseEntity.ok(new SuccessResponse<>("Nota fiscal deletada com sucesso."));
    }

    @Setter
    @Getter
    public static class UrlRequest {
        private String url;
    }
}
