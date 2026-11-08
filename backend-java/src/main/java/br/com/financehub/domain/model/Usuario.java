package br.com.financehub.domain.model;

import java.util.ArrayList;


public class Usuario {
    private long id;
    private String nome;
    private String email;

    private ArrayList<Lancamento> lancementos;

    public Usuario(long id, String nome, String email){
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.lancementos = new ArrayList<>();
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

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void adicionarLancamento(Lancamento lancamento){
        if (lancamento == null){
            throw new IllegalArgumentException("O lancamento nao pode ser nulo");
        }
        this.lancementos.add(lancamento);
    }

}