package br.com.financehub.domain.service;

import br.com.financehub.domain.model.Categoria;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class GerenciadorCategorias {

    private ArrayList<Categoria> categorias;

    public GerenciadorCategorias(){
        this.categorias = new ArrayList<>();
    }
    public void cadastrar(Categoria categoria){
        if (categoria == null || categoria.getNome() == null || categoria.getNome().isBlank()){
            throw new IllegalArgumentException("Categoria invalida");
        }
        for (Categoria existente : this.categorias){
            boolean mesmoId = existente.getId() == categoria.getId();
            boolean mesmoNome = existente.getNome().equalsIgnoreCase(categoria.getNome());
            if (mesmoId || mesmoNome){
                throw new IllegalArgumentException("Categoria duplicada");
            }
        }
        categorias.add(categoria);
    }
    public Categoria buscarPorId(long id){
        for (Categoria categoria : categorias){
            if (categoria.getId() == id){
                return categoria;
            }
        }
        throw new NoSuchElementException("Categoria nao encontrada");
    }
    public boolean removerPorId(long id){
        for (int i = 0; i < categorias.size(); i++){
            if (categorias.get(i).getId() == id){
                categorias.remove(i);
                return true;
            }
        }
        return false;
    }
    public ArrayList<Categoria> listar(){
        return new ArrayList<>(categorias);
    }
}