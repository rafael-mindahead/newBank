package br.com.financehub.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Usuario {
    private long id;
    private String nome;
    private String email;
    private BigDecimal metaDespesaMensal;
    private ArrayList<Lancamento> lancementos;

    public Usuario(long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.metaDespesaMensal = new BigDecimal("3500.00");
        this.lancementos = new ArrayList<>();
    }

    public BigDecimal getMetaDespesaMensal() {
        return this.metaDespesaMensal;
    }

    public void setMetaDespesaMensal(BigDecimal metaDespesaMensal) {
        if (metaDespesaMensal == null || metaDespesaMensal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A meta mensal deve ser maior que zero");
        }
        this.metaDespesaMensal = metaDespesaMensal;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<Lancamento> getLancementos() {
        return new ArrayList<>(this.lancementos);
    }

    public ArrayList<Lancamento> getLancamentos() {
        return new ArrayList<>(this.lancementos);
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome nao pode ser vazio");
        }
        this.nome = nome.trim();
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O email nao pode ser vazio");
        }
        this.email = email.trim();
    }

    public void adicionarLancamento(Lancamento lancamento) {
        if (lancamento == null) {
            throw new IllegalArgumentException("O lancamento nao pode ser nulo");
        }
        this.lancementos.add(lancamento);
    }

    public boolean removerLancamento(Lancamento lancamento) {
        return this.lancementos.remove(lancamento);
    }

    public void limparLancamentos() {
        this.lancementos.clear();
    }

    public BigDecimal calcularSaldoTotal() {
        BigDecimal saldo = BigDecimal.ZERO;
        for (Lancamento l : this.lancementos) {
            saldo = saldo.add(l.calcularImpacto());
        }
        return saldo;
    }
}
