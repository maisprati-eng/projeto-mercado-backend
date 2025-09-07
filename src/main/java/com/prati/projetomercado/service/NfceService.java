package com.prati.projetomercado.service;

import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Catalog;
import com.prati.projetomercado.entity.Item;
import com.prati.projetomercado.entity.Purchase;
import com.prati.projetomercado.entity.Supermarket;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.CatalogRepository;
import com.prati.projetomercado.repository.ItemRepository;
import com.prati.projetomercado.repository.PurchaseRepository;
import com.prati.projetomercado.repository.SupermarketRepository;
import com.prati.projetomercado.utils.ScraperUtils;
import com.prati.projetomercado.utils.ScraperUtils.NfceData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NfceService {

    private final ScraperUtils scraper;
    private final AuthUserRepository userRepo;
    private final SupermarketRepository supermarketRepo;
    private final PurchaseRepository purchaseRepo;

    @Transactional
    public Optional<Purchase> deletePurchase(Long id) {
        Optional<Purchase> purchase = purchaseRepo.findById(id);
        purchase.ifPresent(purchaseRepo::delete);
        return purchase;
    }

    private final CatalogRepository catalogRepo;
    private final ItemRepository itemRepo;

    private Supermarket createSupermarket(NfceData data, AuthUser user) {
        Supermarket newMarket = Supermarket.builder()
                .name(data.getStore())
                .cnpj(data.getCnpj())
                .street(data.getAddress().getStreet())
                .number(data.getAddress().getNumber())
                .complement(data.getAddress().getComplement())
                .neighborhood(data.getAddress().getNeighborhood())
                .city(data.getAddress().getCity())
                .state(data.getAddress().getState())
                .createdByUser(user)
                .build();

        return supermarketRepo.save(newMarket);
    }

    private Catalog createCatalog(ScraperUtils.Product p, Supermarket market) {
        Catalog newCatalog = Catalog.builder()
                .supermarket(market)
                .code(p.getCode())
                .name(p.getName())
                .unit(p.getUnit())
                .build();

        return catalogRepo.save(newCatalog);
    }

    // if an error occurs, the transaction is rolled back and nothing is sent to DB
    @Transactional(rollbackFor = Exception.class)
    public NfceData processNfce(String url, Long userId) throws IOException {
        // scrapes nfc-e
        NfceData data = scraper.getData(url);

        // fetches user
        AuthUser user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // fetches supermarket or creates a new one
        Supermarket market = supermarketRepo.findByCnpj(data.getCnpj())
                .orElseGet(() -> createSupermarket(data, user));

        // creates/inserts purchase
        Purchase purchase = Purchase.builder()
                .user(user)
                .supermarket(market)
                .accessKey(data.getAccessKey())
                .date(data.getDate())
                .totalPrice(data.getTotalPrice())
                .build();

        purchase = purchaseRepo.save(purchase);

        List<Item> itemsToSave = new ArrayList<>();

        for (ScraperUtils.Product p : data.getProducts()) {
            // fetches catalog or creates a new one
            Catalog catalog = catalogRepo.findBySupermarketAndCode(market, p.getCode())
                    .orElseGet(() -> createCatalog(p, market));

            // creates item and adds to items array
            Item item = Item.builder()
                    .purchase(purchase)
                    .catalog(catalog)
                    .quantity(p.getQuantity())
                    .unitPrice(p.getPrice())
                    .build();

            itemsToSave.add(item);
        }
        // inserts all items in a single query
        itemRepo.saveAll(itemsToSave);

        return data;
    }
}