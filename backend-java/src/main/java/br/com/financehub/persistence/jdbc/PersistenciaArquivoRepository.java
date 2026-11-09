package br.com.financehub.persistence.jdbc;

import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Despesa;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.domain.model.Receita;
import br.com.financehub.persistence.RepositorioDados;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação da interface RepositorioDados para salvar e recuperar objetos
 * em arquivo local (data/financehub-dados.db).
 */
public class PersistenciaArquivoRepository implements RepositorioDados {

    private final Path arquivoDados;

    public PersistenciaArquivoRepository(Path arquivoDados) {
        this.arquivoDados = arquivoDados;
    }

    @Override
    public void salvarObjetos(List<Categoria> categorias, List<Lancamento> lancamentos) throws FinanceHubException {
        if (this.arquivoDados == null) {
            throw new FinanceHubException("ARQUIVO_INVALIDO", "Caminho de persistencia nao informado.");
        }
        try {
            Path parent = this.arquivoDados.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(this.arquivoDados, StandardCharsets.UTF_8)) {
                writer.write("# FINANCEHUB_PERSISTENCIA_V1");
                writer.newLine();
                if (categorias != null) {
                    for (Categoria c : categorias) {
                        writer.write(String.format("CATEGORIA|%d|%s", c.getId(), escapar(c.getNome())));
                        writer.newLine();
                    }
                }
                if (lancamentos != null) {
                    for (Lancamento l : lancamentos) {
                        long catId = l.getCategoria() != null ? l.getCategoria().getId() : 0L;
                        String catNome = l.getCategoria() != null ? l.getCategoria().getNome() : "Geral";
                        long id = l.getId() != null ? l.getId() : 0L;
                        long usuarioId = l.getUsuarioId() != null ? l.getUsuarioId() : 1L;
                        writer.write(String.format(
                            "LANCAMENTO|%d|%s|%s|%s|%s|%d|%s|%d",
                            id,
                            l.getTipo(),
                            escapar(l.getDescricao()),
                            l.getValor().toPlainString(),
                            l.getData().toString(),
                            catId,
                            escapar(catNome),
                            usuarioId
                        ));
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            throw new FinanceHubException("ERRO_SALVAR_OBJETOS", "Erro ao salvar objetos persistentes: " + e.getMessage(), e);
        }
    }

    @Override
    public DadosPersistidos recuperarObjetos() throws FinanceHubException {
        ArrayList<Categoria> categorias = new ArrayList<>();
        ArrayList<Lancamento> lancamentos = new ArrayList<>();

        if (this.arquivoDados == null || !Files.exists(this.arquivoDados)) {
            return new DadosPersistidos(categorias, lancamentos);
        }

        try (BufferedReader reader = Files.newBufferedReader(this.arquivoDados, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String limpa = linha.trim();
                if (limpa.isEmpty() || limpa.startsWith("#")) {
                    continue;
                }
                String[] partes = limpa.split("\\|");
                if ("CATEGORIA".equals(partes[0]) && partes.length >= 3) {
                    long id = Long.parseLong(partes[1]);
                    String nome = desescapar(partes[2]);
                    categorias.add(new Categoria(id, nome));
                } else if ("LANCAMENTO".equals(partes[0]) && partes.length >= 9) {
                    Long id = Long.parseLong(partes[1]);
                    String tipo = partes[2];
                    String descricao = desescapar(partes[3]);
                    BigDecimal valor = new BigDecimal(partes[4]);
                    LocalDate data = LocalDate.parse(partes[5]);
                    long catId = Long.parseLong(partes[6]);
                    String catNome = desescapar(partes[7]);
                    Long usuarioId = Long.parseLong(partes[8]);

                    Categoria cat = buscarCategoriaNaLista(categorias, catId, catNome);
                    Lancamento lancamento = "RECEITA".equalsIgnoreCase(tipo)
                        ? new Receita(id, descricao, valor, data, cat, usuarioId)
                        : new Despesa(id, descricao, valor, data, cat, usuarioId);
                    cat.adicionarLancamento(lancamento);
                    lancamentos.add(lancamento);
                }
            }
        } catch (Exception e) {
            throw new FinanceHubException("ERRO_RECUPERAR_OBJETOS", "Erro ao recuperar objetos persistentes: " + e.getMessage(), e);
        }

        return new DadosPersistidos(categorias, lancamentos);
    }

    private Categoria buscarCategoriaNaLista(List<Categoria> categorias, long id, String nome) {
        for (Categoria c : categorias) {
            if (c.getId() == id || c.getNome().equalsIgnoreCase(nome)) {
                return c;
            }
        }
        Categoria nova = new Categoria(id > 0 ? id : categorias.size() + 1L, nome);
        categorias.add(nova);
        return nova;
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("|", "/");
    }

    private String desescapar(String texto) {
        return texto == null ? "" : texto;
    }

    public static class DadosPersistidos extends RepositorioDados.DadosCarregados {
        private final ArrayList<Categoria> listaCategorias;
        private final ArrayList<Lancamento> listaLancamentos;

        public DadosPersistidos(ArrayList<Categoria> categorias, ArrayList<Lancamento> lancamentos) {
            super(categorias, lancamentos);
            this.listaCategorias = categorias;
            this.listaLancamentos = lancamentos;
        }

        @Override
        public ArrayList<Categoria> getCategorias() {
            return this.listaCategorias;
        }

        @Override
        public ArrayList<Lancamento> getLancamentos() {
            return this.listaLancamentos;
        }
    }
}
