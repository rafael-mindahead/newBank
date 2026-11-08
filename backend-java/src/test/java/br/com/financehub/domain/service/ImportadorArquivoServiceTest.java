package br.com.financehub.domain.service;

import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.domain.model.Relatorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ImportadorArquivoServiceTest {

    @Test
    public void testarImportacaoDeConteudoCsv() throws FinanceHubException {
        ImportadorArquivoService importador = new ImportadorArquivoService();
        GerenciadorLancamentos lancamentos = new GerenciadorLancamentos();
        GerenciadorCategorias categorias = new GerenciadorCategorias();

        String csv = """
            TIPO;DESCRICAO;VALOR;DATA;CATEGORIA
            RECEITA;Salario;3000.00;2026-11-01;Trabalho
            DESPESA;Supermercado;450.50;2026-11-02;Alimentacao
            """;

        ArrayList<Lancamento> lista = importador.importarDeConteudo(csv, lancamentos, categorias);

        assertEquals(2, lista.size());
        assertEquals(2, categorias.listar().size());

        Relatorio relatorio = lancamentos.gerarRelatorio();
        assertEquals(new BigDecimal("2549.50"), relatorio.getSaldoTotal());
    }

    @Test
    public void testarExportacaoELeituraDeArquivo(@TempDir Path tempDir) throws FinanceHubException {
        ImportadorArquivoService importador = new ImportadorArquivoService();
        GerenciadorLancamentos origem = new GerenciadorLancamentos();
        GerenciadorCategorias categoriasOrigem = new GerenciadorCategorias();

        String conteudoInicial = "RECEITA;Freela;800.00;2026-11-05;Extra\nDESPESA;Internet;120.00;2026-11-06;Casa";
        importador.importarDeConteudo(conteudoInicial, origem, categoriasOrigem);

        Path arquivoCsv = tempDir.resolve("lancamentos-teste.csv");
        importador.exportarParaArquivo(arquivoCsv, origem.listar());

        GerenciadorLancamentos destino = new GerenciadorLancamentos();
        GerenciadorCategorias categoriasDestino = new GerenciadorCategorias();
        ArrayList<Lancamento> reimportados = importador.importarDeArquivo(arquivoCsv, destino, categoriasDestino);

        assertEquals(2, reimportados.size());
        assertEquals(new BigDecimal("680.00"), destino.gerarRelatorio().getSaldoTotal());
    }

    @Test
    public void testarErroEmLinhaMalformada() {
        ImportadorArquivoService importador = new ImportadorArquivoService();
        GerenciadorLancamentos lancamentos = new GerenciadorLancamentos();
        GerenciadorCategorias categorias = new GerenciadorCategorias();

        assertThrows(
            FinanceHubException.class,
            () -> importador.importarDeConteudo("LINHA_INVALIDA_SEM_CAMPOS", lancamentos, categorias)
        );
    }
}

