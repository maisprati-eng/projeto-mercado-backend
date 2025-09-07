package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "purchase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AuthUser user;

    @ManyToOne
    @JoinColumn(name = "supermarket_id")
    private Supermarket supermarket;

    @Column(name = "access_key", unique = true)
    private String accessKey;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_price")
    private Double totalPrice;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Item> items;
}
