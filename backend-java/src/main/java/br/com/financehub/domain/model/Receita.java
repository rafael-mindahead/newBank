package br.com.financehub.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Receita extends Lancamento {
    public Receita(long id, String descricao, BigDecimal valor, LocalDate data){
        super(id, descricao, valor, data);
    }

    public Receita(Long id, String descricao, BigDecimal valor, LocalDate data, Categoria categoria, Long usuarioId){
        super(id, descricao, valor, data, categoria, usuarioId);
    }

    @Override
    public BigDecimal calcularImpacto(){
        return this.getValor();
    }

    @Override
    public String getTipo() {
        return "RECEITA";
    }

    @Override
    public String getResumo(){
        return "Receita: " + super.getResumo();
    }
}