package com.nfse.repository;

import com.nfse.model.ModelNfse;
import .org.springframework.data.jpa.repository.JpaRepository;
import .org.springframework.sterotype.Repository;

@Repository
public interface NFSeRepository extends JpaRepository<ModelNfse, Long>{

    //Deste modo o JPA fornece dados prontos para o Crud como 

    //save(), findById(), findAll(), deleteById()

}