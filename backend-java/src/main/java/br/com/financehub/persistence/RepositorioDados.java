package br.com.financehub.persistence;

import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Lancamento;

import java.util.List;

/**
 * Interface que define o contrato de persistência do sistema.
 * Permite salvar e recuperar categorias e lançamentos independentemente
 * do meio de armazenamento (arquivo local ou banco de dados).
 */
public interface RepositorioDados {

    void salvarObjetos(List<Categoria> categorias, List<Lancamento> lancamentos) throws FinanceHubException;

    DadosCarregados recuperarObjetos() throws FinanceHubException;

    class DadosCarregados {
        private final List<Categoria> categorias;
        private final List<Lancamento> lancamentos;

        public DadosCarregados(List<Categoria> categorias, List<Lancamento> lancamentos) {
            this.categorias = categorias;
            this.lancamentos = lancamentos;
        }

        public List<Categoria> getCategorias() {
            return this.categorias;
        }

        public List<Lancamento> getLancamentos() {
            return this.lancamentos;
        }
    }
}
