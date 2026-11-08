package br.com.financehub.persistence.jdbc;

import br.com.financehub.config.DatabaseConfig;
import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Repositório JDBC para persistência de objetos Categoria no MySQL.
 * Atende ao requisito 14 do PjBL (Salvar e recuperar objetos persistentes).
 */
public class JdbcCategoriaRepository {

    private final DatabaseConfig databaseConfig;

    public JdbcCategoriaRepository(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Categoria salvar(Categoria categoria) throws FinanceHubException {
        if (categoria == null || categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new FinanceHubException("CATEGORIA_INVALIDA", "Nome da categoria e obrigatorio.");
        }

        String sql = "INSERT INTO categorias (nome) VALUES (?)";
        try (
            Connection conn = this.databaseConfig.abrirConexao();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setString(1, categoria.getNome().trim());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Categoria(keys.getLong(1), categoria.getNome().trim());
                }
            }
            return categoria;
        } catch (SQLException e) {
            throw new FinanceHubException("ERRO_PERSISTENCIA_CATEGORIA", "Erro ao salvar categoria no MySQL: " + e.getMessage(), e);
        }
    }

    public ArrayList<Categoria> listarTodas() throws FinanceHubException {
        String sql = "SELECT id, nome FROM categorias ORDER BY id ASC";
        ArrayList<Categoria> lista = new ArrayList<>();

        try (
            Connection conn = this.databaseConfig.abrirConexao();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getLong("id"), rs.getString("nome")));
            }
            return lista;
        } catch (SQLException e) {
            throw new FinanceHubException("ERRO_CONSULTA_CATEGORIA", "Erro ao listar categorias do MySQL: " + e.getMessage(), e);
        }
    }

    public Categoria buscarPorId(long id) throws FinanceHubException {
        String sql = "SELECT id, nome FROM categorias WHERE id = ?";
        try (
            Connection conn = this.databaseConfig.abrirConexao();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(rs.getLong("id"), rs.getString("nome"));
                }
            }
            throw new FinanceHubException("CATEGORIA_NAO_ENCONTRADA", "Categoria nao encontrada para ID " + id);
        } catch (SQLException e) {
            throw new FinanceHubException("ERRO_CONSULTA_CATEGORIA", "Erro ao buscar categoria no MySQL: " + e.getMessage(), e);
        }
    }

    public boolean removerPorId(long id) throws FinanceHubException {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (
            Connection conn = this.databaseConfig.abrirConexao();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new FinanceHubException("ERRO_REMOCAO_CATEGORIA", "Erro ao remover categoria no MySQL: " + e.getMessage(), e);
        }
    }
}

