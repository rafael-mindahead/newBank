package br.com.financehub.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Despesa extends Lancamento {

    public Despesa(long id, String descricao, BigDecimal valor, LocalDate data) {
        super(id, descricao, valor, data);
    }

    public Despesa(Long id, String descricao, BigDecimal valor, LocalDate data, Categoria categoria, Long usuarioId) {
        super(id, descricao, valor, data, categoria, usuarioId);
    }

    @Override
    public BigDecimal calcularImpacto() {
        return this.getValor().negate();
    }

    @Override
    public String getTipo() {
        return "DESPESA";
    }

    @Override
    public boolean isReceita() {
        return false;
    }

    @Override
    public String getResumo() {
        return "Despesa: " + super.getResumo();
    }
}
