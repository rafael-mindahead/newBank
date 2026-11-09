package br.com.financehub.domain.service;

import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Despesa;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.domain.model.Receita;
import br.com.financehub.domain.model.Relatorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por gerenciar a coleção de lançamentos financeiros,
 * aplicar regras de validação e gerar relatórios consolidados.
 */
public class GerenciadorLancamentos {

    private final ArrayList<Lancamento> lancamentos;
    private long proximoIdSequencial = 1L;

    public GerenciadorLancamentos() {
        this.lancamentos = new ArrayList<>();
    }

    public static void validarLancamento(Lancamento lancamento) throws FinanceHubException {
        if (lancamento == null) {
            throw new FinanceHubException("LANCAMENTO_NULO", "O lancamento nao pode ser nulo.");
        }
        if (lancamento.getDescricao() == null || lancamento.getDescricao().isBlank()) {
            throw new FinanceHubException("DESCRICAO_INVALIDA", "A descricao do lancamento e obrigatoria.");
        }
        if (lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new FinanceHubException("VALOR_INVALIDO", "O valor do lancamento deve ser maior que zero.");
        }
        if (lancamento.getData() == null) {
            throw new FinanceHubException("DATA_INVALIDA", "A data do lancamento e obrigatoria.");
        }
    }

    public Lancamento cadastrar(Lancamento lancamento) throws FinanceHubException {
        validarLancamento(lancamento);

        if (lancamento.getId() == null || lancamento.getId() <= 0) {
            lancamento.setId(gerarProximoId());
        } else {
            for (Lancamento existente : this.lancamentos) {
                if (existente.getId() != null && existente.getId().equals(lancamento.getId())) {
                    throw new FinanceHubException("LANCAMENTO_DUPLICADO", "Ja existe um lancamento com o ID " + lancamento.getId());
                }
            }
            if (lancamento.getId() >= this.proximoIdSequencial) {
                this.proximoIdSequencial = lancamento.getId() + 1;
            }
        }

        Categoria cat = lancamento.getCategoria();
        if (cat != null && !cat.getLancamentos().contains(lancamento)) {
            cat.adicionarLancamento(lancamento);
        }

        this.lancamentos.add(lancamento);
        return lancamento;
    }

    public Lancamento atualizar(
        long id,
        String novoTipo,
        String novaDescricao,
        BigDecimal novoValor,
        LocalDate novaData,
        Categoria novaCategoria
    ) throws FinanceHubException {
        Lancamento antigo = buscarPorId(id);

        Lancamento atualizado = "RECEITA".equalsIgnoreCase(novoTipo)
            ? new Receita(id, novaDescricao, novoValor, novaData, novaCategoria, antigo.getUsuarioId())
            : new Despesa(id, novaDescricao, novoValor, novaData, novaCategoria, antigo.getUsuarioId());

        validarLancamento(atualizado);

        if (antigo.getCategoria() != null) {
            antigo.getCategoria().removerLancamento(antigo);
        }
        if (novaCategoria != null) {
            novaCategoria.adicionarLancamento(atualizado);
        }

        int indice = this.lancamentos.indexOf(antigo);
        this.lancamentos.set(indice, atualizado);
        return atualizado;
    }

    private long gerarProximoId() {
        while (existeId(this.proximoIdSequencial)) {
            this.proximoIdSequencial++;
        }
        return this.proximoIdSequencial++;
    }

    private boolean existeId(long id) {
        for (Lancamento existente : this.lancamentos) {
            if (existente.getId() != null && existente.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public Lancamento buscarPorId(long id) throws FinanceHubException {
        for (Lancamento lancamento : this.lancamentos) {
            if (lancamento.getId() != null && lancamento.getId() == id) {
                return lancamento;
            }
        }
        throw new FinanceHubException("NAO_ENCONTRADO", "Lancamento nao encontrado para o ID " + id);
    }

    public boolean removerPorId(long id) {
        for (int i = 0; i < this.lancamentos.size(); i++) {
            Lancamento atual = this.lancamentos.get(i);
            if (atual.getId() != null && atual.getId() == id) {
                if (atual.getCategoria() != null) {
                    atual.getCategoria().removerLancamento(atual);
                }
                this.lancamentos.remove(i);
                return true;
            }
        }
        return false;
    }

    public ArrayList<Lancamento> listar() {
        return new ArrayList<>(this.lancamentos);
    }

    public ArrayList<Lancamento> listarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) throws FinanceHubException {
        if (dataInicio == null || dataFim == null) {
            throw new FinanceHubException("PERIODO_INVALIDO", "Datas de inicio e fim sao obrigatorias.");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new FinanceHubException("PERIODO_INVALIDO", "A data de inicio nao pode ser posterior a data de fim.");
        }

        ArrayList<Lancamento> filtrados = new ArrayList<>();
        for (Lancamento lancamento : this.lancamentos) {
            LocalDate data = lancamento.getData();
            if (!data.isBefore(dataInicio) && !data.isAfter(dataFim)) {
                filtrados.add(lancamento);
            }
        }
        return filtrados;
    }

    public Relatorio gerarRelatorio() {
        return calcularRelatorio(this.lancamentos);
    }

    public Relatorio gerarRelatorioPorPeriodo(LocalDate dataInicio, LocalDate dataFim) throws FinanceHubException {
        ArrayList<Lancamento> filtrados = listarPorPeriodo(dataInicio, dataFim);
        return calcularRelatorio(filtrados);
    }

    public static Relatorio calcularRelatorio(List<Lancamento> lista) {
        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        BigDecimal saldoTotal = BigDecimal.ZERO;
        int qtdReceitas = 0;
        int qtdDespesas = 0;

        if (lista != null) {
            for (Lancamento lancamento : lista) {
                // Chamadas polimórficas (sem uso de instanceof)
                BigDecimal impacto = lancamento.calcularImpacto();
                saldoTotal = saldoTotal.add(impacto);

                if (lancamento.isReceita()) {
                    totalReceitas = totalReceitas.add(lancamento.getValor());
                    qtdReceitas++;
                } else {
                    totalDespesas = totalDespesas.add(lancamento.getValor());
                    qtdDespesas++;
                }
            }
        }

        return new Relatorio(totalReceitas, totalDespesas, saldoTotal, qtdReceitas, qtdDespesas);
    }
}
