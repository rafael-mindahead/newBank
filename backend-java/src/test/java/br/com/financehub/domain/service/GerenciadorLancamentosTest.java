package br.com.financehub.domain.service;

import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Despesa;
import br.com.financehub.domain.model.Receita;
import br.com.financehub.domain.model.Relatorio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class GerenciadorLancamentosTest {

    @Test
    public void testarCadastroERelatorioConsolidado() throws FinanceHubException {
        GerenciadorLancamentos gerenciador = new GerenciadorLancamentos();
        Categoria trabalho = new Categoria(1, "Trabalho");
        Categoria alimentacao = new Categoria(2, "Alimentacao");

        gerenciador.cadastrar(new Receita(1L, "Salario", new BigDecimal("3500.00"), LocalDate.of(2026, 11, 1), trabalho, 1L));
        gerenciador.cadastrar(new Despesa(2L, "Mercado", new BigDecimal("450.00"), LocalDate.of(2026, 11, 5), alimentacao, 1L));

        Relatorio relatorio = gerenciador.gerarRelatorio();

        assertEquals(2, relatorio.getQuantidadeLancamentos());
        assertEquals(1, relatorio.getQuantidadeReceitas());
        assertEquals(1, relatorio.getQuantidadeDespesas());
        assertEquals(new BigDecimal("3500.00"), relatorio.getTotalReceitas());
        assertEquals(new BigDecimal("450.00"), relatorio.getTotalDespesas());
        assertEquals(new BigDecimal("3050.00"), relatorio.getSaldoTotal());
        assertEquals(305000L, relatorio.getSaldoTotalCentavos());
    }

    @Test
    public void testarExcecaoPersonalizadaQuandoLancamentoInvalido() {
        GerenciadorLancamentos gerenciador = new GerenciadorLancamentos();

        FinanceHubException ex = assertThrows(
            FinanceHubException.class,
            () -> gerenciador.cadastrar(new Receita(1L, "", new BigDecimal("100.00"), LocalDate.now()))
        );
        assertEquals("DESCRICAO_INVALIDA", ex.getCodigo());

        FinanceHubException exValor = assertThrows(
            FinanceHubException.class,
            () -> gerenciador.cadastrar(new Despesa(2L, "Aluguel", new BigDecimal("-10.00"), LocalDate.now()))
        );
        assertEquals("VALOR_INVALIDO", exValor.getCodigo());
    }

    @Test
    public void testarFiltroPorPeriodo() throws FinanceHubException {
        GerenciadorLancamentos gerenciador = new GerenciadorLancamentos();
        gerenciador.cadastrar(new Receita(1L, "Outubro", new BigDecimal("1000.00"), LocalDate.of(2026, 10, 15)));
        gerenciador.cadastrar(new Receita(2L, "Novembro", new BigDecimal("2000.00"), LocalDate.of(2026, 11, 10)));

        Relatorio relNov = gerenciador.gerarRelatorioPorPeriodo(
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2026, 11, 30)
        );

        assertEquals(1, relNov.getQuantidadeLancamentos());
        assertEquals(new BigDecimal("2000.00"), relNov.getSaldoTotal());
    }
}

