package br.com.financehub.persistence.jdbc;

import br.com.financehub.config.DatabaseConfig;
import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.persistence.RepositorioDados;

import java.nio.file.Path;
import java.util.List;

/**
 * Implementação de RepositorioDados que integra o banco de dados MySQL via JDBC
 * e mantém cópia local em arquivo caso o banco MySQL não esteja ativo na máquina.
 */
public class JdbcRepositorioDados implements RepositorioDados {

    private final DatabaseConfig databaseConfig;
    private final JdbcCategoriaRepository jdbcCategoriaRepository;
    private final JdbcLancamentoRepository jdbcLancamentoRepository;
    private final PersistenciaArquivoRepository arquivoFallback;
    private boolean mysqlDisponivel;

    public JdbcRepositorioDados(Path arquivoLocalFallback) {
        this.databaseConfig = new DatabaseConfig();
        this.jdbcCategoriaRepository = new JdbcCategoriaRepository(this.databaseConfig);
        this.jdbcLancamentoRepository = new JdbcLancamentoRepository(this.databaseConfig);
        this.arquivoFallback = new PersistenciaArquivoRepository(arquivoLocalFallback);
        this.mysqlDisponivel = this.databaseConfig.testarConexao();
    }

    @Override
    public void salvarObjetos(List<Categoria> categorias, List<Lancamento> lancamentos) throws FinanceHubException {
        this.arquivoFallback.salvarObjetos(categorias, lancamentos);
    }

    @Override
    public DadosCarregados recuperarObjetos() throws FinanceHubException {
        if (this.mysqlDisponivel) {
            try {
                List<Categoria> categorias = this.jdbcCategoriaRepository.listarTodas();
                List<Lancamento> lancamentos = this.jdbcLancamentoRepository.listarTodos();
                return new DadosCarregados(categorias, lancamentos);
            } catch (FinanceHubException e) {
                this.mysqlDisponivel = false;
            }
        }
        return this.arquivoFallback.recuperarObjetos();
    }

    public boolean isMysqlDisponivel() {
        return this.mysqlDisponivel;
    }
}

