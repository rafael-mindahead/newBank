package br.com.financehub.app;

import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Despesa;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.domain.model.Receita;
import br.com.financehub.domain.model.Relatorio;
import br.com.financehub.domain.model.Usuario;
import br.com.financehub.domain.service.GerenciadorCategorias;
import br.com.financehub.domain.service.GerenciadorLancamentos;
import br.com.financehub.domain.service.ImportadorArquivoService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DemonstracaoPOO {

    public static void main(String[] args) {
        try {
            Usuario usuario = new Usuario(1L, "Rafael", "rafael@financehub.com");
            GerenciadorCategorias gerenciadorCategorias = new GerenciadorCategorias();
            GerenciadorLancamentos gerenciadorLancamentos = new GerenciadorLancamentos();

            Categoria trabalho = new Categoria(1L, "Trabalho");
            Categoria alimentacao = new Categoria(2L, "Alimentacao");
            gerenciadorCategorias.cadastrar(trabalho);
            gerenciadorCategorias.cadastrar(alimentacao);

            Lancamento lancamento = new Receita(
                1L,
                "Salario",
                new BigDecimal("2500.00"),
                LocalDate.now(),
                trabalho,
                usuario.getId()
            );

            System.out.println(lancamento.getResumo());
            System.out.println(lancamento.calcularImpacto());
            gerenciadorLancamentos.cadastrar(lancamento);
            usuario.adicionarLancamento(lancamento);

            lancamento = new Despesa(
                2L,
                "Mercado",
                new BigDecimal("300.00"),
                LocalDate.now(),
                alimentacao,
                usuario.getId()
            );

            System.out.println(lancamento.getResumo());
            System.out.println(lancamento.calcularImpacto());
            gerenciadorLancamentos.cadastrar(lancamento);
            usuario.adicionarLancamento(lancamento);

            // Demonstração de importação via texto CSV
            ImportadorArquivoService importador = new ImportadorArquivoService();
            importador.importarDeConteudo(
                "RECEITA;Freelance POO;800.00;2026-11-08;Trabalho",
                gerenciadorLancamentos,
                gerenciadorCategorias
            );

            Relatorio relatorio = gerenciadorLancamentos.gerarRelatorio();
            System.out.println(relatorio.gerarResumoTexto());
        } catch (FinanceHubException e) {
            System.err.println("Erro de dominio [" + e.getCodigo() + "]: " + e.getMessage());
        }
    }
}
