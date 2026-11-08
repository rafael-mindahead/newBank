package br.com.financehub.domain.service;

import br.com.financehub.domain.model.Categoria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GerenciadorCategoriasTest {

    @Test
    public void testarCadastroEBusca() {
        GerenciadorCategorias gerenciador =
            new GerenciadorCategorias();

        Categoria categoria =
            new Categoria(1, "Alimentacao");

        gerenciador.cadastrar(categoria);

        assertSame(
            categoria,
            gerenciador.buscarPorId(1)
        );
    }

    @Test
    public void testarCategoriaInexistente() {
        GerenciadorCategorias gerenciador =
            new GerenciadorCategorias();

        assertThrows(
            java.util.NoSuchElementException.class,
            () -> gerenciador.buscarPorId(999)
        );
    }

    @Test
    public void testarCategoriaDuplicada() {
        GerenciadorCategorias gerenciador =
            new GerenciadorCategorias();

        gerenciador.cadastrar(
            new Categoria(1, "Alimentacao")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> gerenciador.cadastrar(
                new Categoria(1, "Transporte")
            )
        );
    }

    @Test
    public void testarRemocao() {
        GerenciadorCategorias gerenciador =
            new GerenciadorCategorias();

        gerenciador.cadastrar(
            new Categoria(1, "Alimentacao")
        );

        assertTrue(gerenciador.removerPorId(1));
        assertFalse(gerenciador.removerPorId(1));
        assertTrue(gerenciador.listar().isEmpty());
    }
}
