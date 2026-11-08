package br.com.financehub.domain.exception;

/**
 * Exceção verificada (checked exception) de domínio do FinanceHub.
 * Atende ao requisito 11 do PjBL (Classe que estende Exception).
 */
public class FinanceHubException extends Exception {

    private final String codigo;

    public FinanceHubException(String mensagem) {
        super(mensagem);
        this.codigo = "ERRO_DOMINIO";
    }

    public FinanceHubException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = (codigo == null || codigo.isBlank()) ? "ERRO_DOMINIO" : codigo;
    }

    public FinanceHubException(String codigo, String mensagem, Throwable causa) {
        super(mensagem, causa);
        this.codigo = (codigo == null || codigo.isBlank()) ? "ERRO_DOMINIO" : codigo;
    }

    public String getCodigo() {
        return this.codigo;
    }
}

