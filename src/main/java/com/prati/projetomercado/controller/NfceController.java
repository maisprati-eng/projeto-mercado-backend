package com.prati.projetomercado.controller;

import com.prati.projetomercado.service.NfceService;
import com.prati.projetomercado.utils.ScraperUtils.NfceData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@RestController
@RequestMapping("/api/nfce")
@RequiredArgsConstructor
public class NfceController {

    private final NfceService nfceService;

    @Setter
    @Getter
    public static class UrlRequest {
        private String url;
        private Long userId;
    }

    @PostMapping("/scrape")
    public ResponseEntity<?> scrapeNfce(@RequestBody UrlRequest request) {
        try {
            NfceData data = nfceService.processNfce(request.getUrl(), request.getUserId());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/purchase/{id}")
    public ResponseEntity<?> deletePurchase(@PathVariable Long id) {
        return nfceService.deletePurchase(id)
                .map(p -> ResponseEntity.ok(
                        Map.of("message", "NFCe deletada com sucesso!", "purchase", p)))
                .orElse(ResponseEntity.status(404)
                        .body(Map.of("erro", "NFCe não encontrada")));
    }
}
