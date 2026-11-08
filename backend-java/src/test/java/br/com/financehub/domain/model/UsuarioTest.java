package br.com.financehub.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class UsuarioTest {

    @Test
    public void testarCriacaoUsuario() {

        Usuario usuario = new Usuario(
            1, "Rafael", "rafael@email.com"
        );

        assertEquals(1, usuario.getId());
        assertEquals("Rafael", usuario.getNome());
        assertEquals("rafael@email.com", usuario.getEmail());
    }

    @Test
    public void testarAlteracaoDados() {

        Usuario usuario = new Usuario(
            1, "Rafael", "rafael@email.com"
        );

        usuario.setNome("Rafael Alves");
        usuario.setEmail("novo@email.com");

        assertEquals("Rafael Alves", usuario.getNome());
        assertEquals("novo@email.com", usuario.getEmail());
    }

    @Test
    public void testarAssociacaoLancamentos() {

        Usuario usuario = new Usuario(
            1, "Rafael", "rafael@email.com"
        );

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

        usuario.adicionarLancamento(receita);
        usuario.adicionarLancamento(despesa);

        assertEquals(2, usuario.getLancementos().size());

        assertTrue(
            usuario.getLancementos().get(0) instanceof Receita
        );

        assertTrue(
            usuario.getLancementos().get(1) instanceof Despesa
        );
    }

    @Test
    public void testarEncapsulamentoLista() {
        Usuario usuario = new Usuario(
            1, "Rafael", "rafael@email.com"
        );

        Receita receita = new Receita(
            1, "Salario",
            new BigDecimal("2500.00"),
            LocalDate.now()
        );

        usuario.adicionarLancamento(receita);

        ArrayList<Lancamento> lista = usuario.getLancementos();
        lista.clear();

        assertEquals(1, usuario.getLancementos().size());
    }


    @Test
    public void testarLancamentoNulo() {

        Usuario usuario = new Usuario(
            1, "Rafael", "rafael@email.com"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> usuario.adicionarLancamento(null)
        );
    }
}
