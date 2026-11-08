package br.com.financehub.persistence.jdbc;

import br.com.financehub.config.DatabaseConfig;
import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Despesa;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.domain.model.Receita;
import br.com.financehub.domain.service.GerenciadorLancamentos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Repositório JDBC para salvar e recuperar objetos Lancamento (Receita e Despesa) no MySQL.
 * Atende ao requisito 14 do PjBL (Salvar e recuperar objetos persistentes).
 */
public class JdbcLancamentoRepository {

    private final DatabaseConfig databaseConfig;

    public JdbcLancamentoRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Lancamento salvar(Lancamento lancamento) throws FinanceHubException {
        GerenciadorLancamentos.validarLancamento(lancamento);

        String sql = """
            INSERT INTO lancamentos (tipo, descricao, valor, valor_centavos, data_lancamento, categoria_id, usuario_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (
            Connection conn = this.databaseConfig.abrirConexao();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, lancamento.getTipo());
            stmt.setString(2, lancamento.getDescricao().trim());
            stmt.setBigDecimal(3, lancamento.getValor());
            stmt.setLong(4, lancamento.getValorCentavos());
            stmt.setDate(5, Date.valueOf(lancamento.getData()));

            if (lancamento.getCategoria() != null && lancamento.getCategoria().getId() > 0) {
                stmt.setLong(6, lancamento.getCategoria().getId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }

            long usuarioId = lancamento.getUsuarioId() != null ? lancamento.getUsuarioId() : 1L;
            stmt.setLong(7, usuarioId);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    lancamento.setId(keys.getLong(1));
                }
            }
            return lancamento;
        } catch (SQLException e) {
            throw new FinanceHubException("ERRO_PERSISTENCIA_LANCAMENTO", "Erro ao salvar lancamento no MySQL: " + e.getMessage(), e);
        }
    }

    public ArrayList<Lancamento> listarTodos() throws FinanceHubException {
        String sql = """
            SELECT l.id, l.tipo, l.descricao, l.valor, l.data_lancamento, l.usuario_id,
                   c.id AS cat_id, c.nome AS cat_nome
            FROM lancamentos l
            LEFT JOIN categorias c ON l.categoria_id = c.id
            ORDER BY l.data_lancamento DESC, l.id DESC
            """;

        ArrayList<Lancamento> lista = new ArrayList<>();
        try (
            Connection conn = this.databaseConfig.abrirConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(mapearLinha(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new FinanceHubException("ERRO_CONSULTA_LANCAMENTO", "Erro ao listar lancamentos do MySQL: " + e.getMessage(), e);
        }
    }

    public boolean removerPorId(long id) throws FinanceHubException {
        String sql = "DELETE FROM lancamentos WHERE id = ?";
        try (
            Connection conn = this.databaseConfig.abrirConexao();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new FinanceHubException("ERRO_REMOCAO_LANCAMENTO", "Erro ao remover lancamento no MySQL: " + e.getMessage(), e);
        }
    }

    private Lancamento mapearLinha(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String tipo = rs.getString("tipo");
        String descricao = rs.getString("descricao");
        BigDecimal valor = rs.getBigDecimal("valor");
        LocalDate data = rs.getDate("data_lancamento").toLocalDate();
        Long usuarioId = rs.getLong("usuario_id");

        Categoria categoria = null;
        long catId = rs.getLong("cat_id");
        if (!rs.wasNull()) {
            categoria = new Categoria(catId, rs.getString("cat_nome"));
        }

        if ("RECEITA".equalsIgnoreCase(tipo)) {
            return new Receita(id, descricao, valor, data, categoria, usuarioId);
        } else {
            return new Despesa(id, descricao, valor, data, categoria, usuarioId);
        }
    }
}

