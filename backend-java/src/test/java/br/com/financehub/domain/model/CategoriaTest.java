package br.com.financehub.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class CategoriaTest {

    @Test
    public void testarCriacaoCategoria() {
        Categoria categoria = new Categoria(1, "Alimentacao");

        assertEquals(1, categoria.getId());
        assertEquals("Alimentacao", categoria.getNome());
        assertTrue(categoria.getLancamentos().isEmpty());
    }

    @Test
    public void testarAdicionarLancamento() {
        Categoria categoria = new Categoria(1, "Alimentacao");

        Despesa despesa = new Despesa(
            1, "Mercado",
            new BigDecimal("300.00"),
            LocalDate.now()
        );

        categoria.adicionarLancamento(despesa);

        assertEquals(1, categoria.getLancamentos().size());
        assertSame(despesa, categoria.getLancamentos().get(0));
    }

    @Test
    public void testarRemoverLancamento() {
        Categoria categoria = new Categoria(1, "Alimentacao");

        Despesa despesa = new Despesa(
            1, "Mercado",
            new BigDecimal("300.00"),
            LocalDate.now()
        );

        categoria.adicionarLancamento(despesa);

        assertTrue(categoria.removerLancamento(despesa));
        assertTrue(categoria.getLancamentos().isEmpty());

        // Não podemos remover novamente o mesmo objeto.
        assertFalse(categoria.removerLancamento(despesa));
    }

    @Test
    public void testarEncapsulamentoLista() {
        Categoria categoria = new Categoria(1, "Alimentacao");

        Despesa despesa = new Despesa(
            1, "Mercado",
            new BigDecimal("300.00"),
            LocalDate.now()
        );

        categoria.adicionarLancamento(despesa);

        ArrayList<Lancamento> lista = categoria.getLancamentos();
        lista.clear();

        // Limpar a copia nao deve alterar a lista interna.
        assertEquals(1, categoria.getLancamentos().size());
    }

    @Test
    public void testarCalcularSaldo() {
        Categoria categoria = new Categoria(1, "Geral");

        Receita receita = new Receita(
            1, "Salario",
            new BigDecimal("2500.00"),
            LocalDate.now()
        );

        Despesa despesa = new Despesa(
            2, "Mercado",
            new BigDecimal("300.00"),
            LocalDate.now()
        );

        categoria.adicionarLancamento(receita);
        categoria.adicionarLancamento(despesa);

        assertEquals(
            new BigDecimal("2200.00"),
            categoria.calcularSaldo()
        );
    }

    @Test
    public void testarLancamentoNulo() {
        Categoria categoria = new Categoria(1, "Geral");

        assertThrows(
            IllegalArgumentException.class,
            () -> categoria.adicionarLancamento(null)
        );
    }
}