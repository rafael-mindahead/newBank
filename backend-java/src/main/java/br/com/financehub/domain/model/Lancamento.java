package br.com.financehub.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public abstract class Lancamento {
    
    private Long id;
    private String descricao;
    private BigDecimal valor;
    private LocalDate data;
    private Categoria categoria;
    private Long usuarioId;

    public Lancamento(Long id, String descricao, BigDecimal valor, LocalDate data){
        this(id, descricao, valor, data, null, 1L);
    }

    public Lancamento(Long id, String descricao, BigDecimal valor, LocalDate data, Categoria categoria, Long usuarioId){
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoria = categoria;
        this.usuarioId = usuarioId;
    }

    public abstract BigDecimal calcularImpacto();

    public abstract String getTipo();

    public String getResumo(){
        return this.descricao + " - R$ " + this.valor;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public long getValorCentavos() {
        if (this.valor == null) {
            return 0L;
        }
        return this.valor.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    public LocalDate getData() {
        return this.data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Categoria getCategoria() {
        return this.categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Long getUsuarioId() {
        return this.usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}
