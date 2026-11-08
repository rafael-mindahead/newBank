package br.com.financehub.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Configuração de conexão JDBC com o MySQL e parâmetros do servidor HTTP.
 */
public class DatabaseConfig {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseConfig() {
        String host = lerEnv("MYSQL_HOST", "localhost");
        String port = lerEnv("MYSQL_PORT", "3306");
        String db = lerEnv("MYSQL_DATABASE", "financehub");
        this.jdbcUrl = lerEnv(
            "JDBC_URL",
            "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"
        );
        this.username = lerEnv("MYSQL_USER", "financehub");
        this.password = lerEnv("MYSQL_PASSWORD", "financehub_pass");
    }

    public DatabaseConfig(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public Connection abrirConexao() throws SQLException {
        DriverManager.setLoginTimeout(2);
        return DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
    }

    public boolean testarConexao() {
        try (Connection conn = abrirConexao()) {
            return conn != null && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public String getUsername() {
        return this.username;
    }

    private static String lerEnv(String chave, String padrao) {
        String valor = System.getenv(chave);
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(chave);
        }
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}

