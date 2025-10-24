package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Supermarket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {
    Optional<Catalog> findBySupermarketAndCode(Supermarket supermarket, String code);

    List<Catalog> findAllBySupermarket_Id(Long supermarketId);

    Optional<Catalog> findCatalogoByIdAndSupermarket_CreatedByUser_Email(Long id, String email);
}
