package br.com.financehub.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Categoria {
    private long id;
    private String nome;
    private ArrayList<Lancamento> lancamentos;

    public Categoria(long id, String nome){
        this.id = id;
        this.nome = nome;
        this.lancamentos = new ArrayList<>();
    }

    public long getId() {
        return this.id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    public void adicionarLancamento(Lancamento lancamento){
        if (lancamento == null){
            throw new IllegalArgumentException("O lancamento nao pode ser nulo");
        }
        this.lancamentos.add(lancamento);
    }

    public ArrayList<Lancamento> getLancamentos() {
        return new ArrayList<>(this.lancamentos);
    }
    public boolean removerLancamento(Lancamento lancamento){
        return this.lancamentos.remove(lancamento);
    }
    public BigDecimal calcularSaldo(){
        BigDecimal saldo = BigDecimal.ZERO;
        for (Lancamento lancamento : this.lancamentos){
            saldo = saldo.add(lancamento.calcularImpacto());
        }
        return saldo;
    } 
}