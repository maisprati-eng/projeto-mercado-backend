package com.NFSe.controller

import com.NfSe.modal.NFSe;
import com.NFSe.service.NFSe_service;
import org.springframework.http.ResponseEsntity;
import org.springframework.web.bind.annotation.*;

import java.utils.List;

@RestController //Define essa classe como a que responde as requisições em http
@RequestMapping("/nfse")

public class NFSe_Controller {
    private final NFSeService service;

    public NFSe_Controller(NFSeService service){
this.service = service;

}

@PostMapping 

public ResponseEntity<NFSe> createNFSe(@RequestBody NFSe nfse){
    NFSe created = service.createNFSe(nfse);
    return ResponseEntity.ok(created);
}

@GetMapping

public ResponseEntity<List<NFSe>> getNFSe(@PathVariable long id){
    NFSe nfse = service.getNFSe(id);
    return ResponseEntity.ok(Collections.singletonList(nfse));

}

@GetMapping("/id/{id}")

public ResponseEntity<NFSe> buscarPorId(@PathVariable Long id){
 return service.buscarPorId(id)
 .map(ResponseEntity::ok)
 .orElse(ResponseEntity.notFound() build());

}

@PutMapping("/id/{id}")

public ResponseEntity<NFSe> atualizarNFSe(@PathVariable long id, @RequestBody NFSe nfseAtualizada){
    try{
        return ResponseEntity.ok (service.atualizarNFSe(id, nfseAtualizada));
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}

@DeleteMapping("/id/{id}")

public ResponseEntity<Void> deletarNFSe(@PathVariable long id){
    try{
        service.deletarNFSe(id);
        return ResponseEntity.noContent().build();
    } catch (Exception e){
        return ResponseEntity.badRequest().build();
    }
}







}
