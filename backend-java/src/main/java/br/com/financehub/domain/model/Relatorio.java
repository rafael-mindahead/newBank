package br.com.financehub.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Representa o resumo e relatório consolidado sobre uma coleção de lançamentos.
 */
public class Relatorio {

    private final BigDecimal totalReceitas;
    private final BigDecimal totalDespesas;
    private final BigDecimal saldoTotal;
    private final int quantidadeReceitas;
    private final int quantidadeDespesas;
    private final int quantidadeLancamentos;

    public Relatorio(
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldoTotal,
        int quantidadeReceitas,
        int quantidadeDespesas
    ) {
        this.totalReceitas = totalReceitas != null ? totalReceitas : BigDecimal.ZERO;
        this.totalDespesas = totalDespesas != null ? totalDespesas : BigDecimal.ZERO;
        this.saldoTotal = saldoTotal != null ? saldoTotal : BigDecimal.ZERO;
        this.quantidadeReceitas = quantidadeReceitas;
        this.quantidadeDespesas = quantidadeDespesas;
        this.quantidadeLancamentos = quantidadeReceitas + quantidadeDespesas;
    }

    public BigDecimal getTotalReceitas() {
        return this.totalReceitas;
    }

    public BigDecimal getTotalDespesas() {
        return this.totalDespesas;
    }

    public BigDecimal getSaldoTotal() {
        return this.saldoTotal;
    }

    public long getTotalReceitasCentavos() {
        return this.totalReceitas.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public long getTotalDespesasCentavos() {
        return this.totalDespesas.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public long getSaldoTotalCentavos() {
        return this.saldoTotal.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public int getQuantidadeReceitas() {
        return this.quantidadeReceitas;
    }

    public int getQuantidadeDespesas() {
        return this.quantidadeDespesas;
    }

    public int getQuantidadeLancamentos() {
        return this.quantidadeLancamentos;
    }

    public String gerarResumoTexto() {
        return String.format(
            "Relatorio Financeiro [Lancamentos: %d | Receitas: R$ %s (%d) | Despesas: R$ %s (%d) | Saldo: R$ %s]",
            this.quantidadeLancamentos,
            this.totalReceitas.setScale(2, RoundingMode.HALF_UP),
            this.quantidadeReceitas,
            this.totalDespesas.setScale(2, RoundingMode.HALF_UP),
            this.quantidadeDespesas,
            this.saldoTotal.setScale(2, RoundingMode.HALF_UP)
        );
    }
}

