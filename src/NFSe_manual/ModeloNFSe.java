package com.NFSe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity // Indicação de que será feito uma tabela no banco de dados
@Table(name = "nfse")
public class ModelNFse {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;


@Column(nullable = false, unique = true)
private String numeroNf;

private LocalDateTime dataEmisso;

private String codigoVerificacao;

private Double valor;

private String prestador;

private String tomador;

private LocalDateTime createdAt = LocalDateTime.now();
private LocalDateTime updatedAt= LocalDateTime.now();


public Long getId(){  return id;}
public void setId(Long id){this.id = id;}

public String getNumeroNf(){ return numeroNf;}
public void setNumeroNf(String numeroNf){this.numeroNf = numeroNf;}

public LocalDateTime getDataEmissao(){return dataEmissao;}
public void setDataEmissao(LocalDateTime dataEmissao){this.dataEmissao = dataEmissao;}

public String getCodigoVerificacao(){return codigoVerificacao;}
public void setCodigoVerificacao(String codigoVerificacao){this.codigoVerificacao = codigoVerificacao;}

public String getPrestador(){return prestador;}
public void setPrestador(String prestador){this.prestador = prestador;}

public String getTomador(){return tomador;}
public void setTomador(String tomador){this.tomador = tomador;}

public Double getValor(){return valor;}
public void setValor(Double valor){this.valor = valor;}

public LocalDateTime getCreatedAt(){return createdAt;}
public LocalDateTime getUpdatedAt(){return updatedAt;}

}
