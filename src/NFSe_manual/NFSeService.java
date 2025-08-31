package com.nfse.service;

import com.nfse.model.ModelNFse;
import com.nfse.repository.NFSeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NFSeService {
    private final NFSeRepository repository;

    public NFSeService(NFSeRepository nfseRepository){
        this.repository = nfseRepository;
    }

    public NFSe criar(NFSe nfse){
        return repository.save(nfse);
    }
    public List<NFSe> listarTodos(){
        return repository.findAll();

    }

    public Optional<NFSe> findById(Long id){
        return repository.findById(id);

    }
    public NFSe atualizar(Long id, NFSe nfseAtualizada){
        return repository.findById(id) .map(nfse ->{
            nfse.setNumeroNf(nfseAtualizada.getNumeroNf());
            nfse.setDataEmissao(nfseAtualizada.getDataEmissao());
            nfse.setCodigoVerificacao(nfseAtualizada.getCodigoVerificacao());
            nfse.setPrestador(nfseAtualizada.getPrestador());
            nfse.setTomador(nfseAtualizada.getTomador());
            nfse.setValor(nfseAtualizada.getValor());
            nfse.setUpdate(nfseAtualizada.getUpdateAt());

            return repository.save(nfse);
        }).orElseThrow(() -> new RuntimeException("NFSe não encontrada"));
    }
    public void deletar(Long id) {
        repository.deleteById(id);
    }
}
