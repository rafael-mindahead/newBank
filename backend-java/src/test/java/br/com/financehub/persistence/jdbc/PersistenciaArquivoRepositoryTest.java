package br.com.financehub.persistence.jdbc;

import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Despesa;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.domain.model.Receita;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenciaArquivoRepositoryTest {

    @Test
    public void testarSalvarERecuperarObjetosPersistentes(@TempDir Path tempDir) throws FinanceHubException {
        Path arquivo = tempDir.resolve("financehub-dados.db");
        PersistenciaArquivoRepository repo = new PersistenciaArquivoRepository(arquivo);

        Categoria catTrabalho = new Categoria(1L, "Trabalho");
        Categoria catMoradia = new Categoria(2L, "Moradia");

        Lancamento r1 = new Receita(10L, "Salario Novembro", new BigDecimal("5000.00"), LocalDate.of(2026, 11, 1), catTrabalho, 1L);
        Lancamento d1 = new Despesa(11L, "Aluguel", new BigDecimal("1500.00"), LocalDate.of(2026, 11, 5), catMoradia, 1L);

        repo.salvarObjetos(List.of(catTrabalho, catMoradia), List.of(r1, d1));

        PersistenciaArquivoRepository.DadosPersistidos recuperados = repo.recuperarObjetos();

        assertEquals(2, recuperados.getCategorias().size());
        assertEquals(2, recuperados.getLancamentos().size());

        Lancamento primeiro = recuperados.getLancamentos().get(0);
        assertEquals(10L, primeiro.getId());
        assertEquals("RECEITA", primeiro.getTipo());
        assertEquals("Salario Novembro", primeiro.getDescricao());
        assertEquals(new BigDecimal("5000.00"), primeiro.getValor());
        assertEquals("Trabalho", primeiro.getCategoria().getNome());
    }
}

