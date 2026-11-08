package br.com.financehub.api.dto;

import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.domain.model.Relatorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitário de serialização e leitura JSON UTF-8 sem dependências externas,
 * seguindo o contrato definido em docs/arquitetura.md (dinheiro em centavos inteiros,
 * datas ISO-8601 YYYY-MM-DD e objetos de erro com code e message).
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
    }

    public static String erroJson(String code, String message) {
        return String.format(
            "{\"code\":\"%s\",\"message\":\"%s\"}",
            escaparJson(code),
            escaparJson(message)
        );
    }

    public static String categoriaParaJson(Categoria c) {
        return String.format(
            "{\"id\":%d,\"nome\":\"%s\"}",
            c.getId(),
            escaparJson(c.getNome())
        );
    }

    public static String listaCategoriasParaJson(List<Categoria> categorias) {
        StringBuilder sb = new StringBuilder("[");
        if (categorias != null) {
            for (int i = 0; i < categorias.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(categoriaParaJson(categorias.get(i)));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String lancamentoParaJson(Lancamento l) {
        long id = l.getId() != null ? l.getId() : 0L;
        long catId = l.getCategoria() != null ? l.getCategoria().getId() : 0L;
        String catNome = l.getCategoria() != null ? l.getCategoria().getNome() : "Geral";
        long usuarioId = l.getUsuarioId() != null ? l.getUsuarioId() : 1L;
        BigDecimal valorFormatado = l.getValor().setScale(2, RoundingMode.HALF_UP);
        long impactoCentavos = l.calcularImpacto().movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

        return String.format(
            "{\"id\":%d,\"tipo\":\"%s\",\"descricao\":\"%s\",\"valorCentavos\":%d,\"impactoCentavos\":%d,\"valor\":%s,\"data\":\"%s\",\"resumo\":\"%s\",\"categoriaId\":%d,\"categoriaNome\":\"%s\",\"usuarioId\":%d}",
            id,
            escaparJson(l.getTipo()),
            escaparJson(l.getDescricao()),
            l.getValorCentavos(),
            impactoCentavos,
            valorFormatado.toPlainString(),
            escaparJson(l.getData().toString()),
            escaparJson(l.getResumo()),
            catId,
            escaparJson(catNome),
            usuarioId
        );
    }

    public static String listaLancamentosParaJson(List<Lancamento> lancamentos) {
        StringBuilder sb = new StringBuilder("[");
        if (lancamentos != null) {
            for (int i = 0; i < lancamentos.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(lancamentoParaJson(lancamentos.get(i)));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String relatorioParaJson(Relatorio r) {
        return String.format(
            "{\"totalReceitasCentavos\":%d,\"totalDespesasCentavos\":%d,\"saldoTotalCentavos\":%d,\"totalReceitas\":%s,\"totalDespesas\":%s,\"saldoTotal\":%s,\"quantidadeReceitas\":%d,\"quantidadeDespesas\":%d,\"quantidadeLancamentos\":%d,\"resumoTexto\":\"%s\"}",
            r.getTotalReceitasCentavos(),
            r.getTotalDespesasCentavos(),
            r.getSaldoTotalCentavos(),
            r.getTotalReceitas().setScale(2, RoundingMode.HALF_UP).toPlainString(),
            r.getTotalDespesas().setScale(2, RoundingMode.HALF_UP).toPlainString(),
            r.getSaldoTotal().setScale(2, RoundingMode.HALF_UP).toPlainString(),
            r.getQuantidadeReceitas(),
            r.getQuantidadeDespesas(),
            r.getQuantidadeLancamentos(),
            escaparJson(r.gerarResumoTexto())
        );
    }

    public static String extrairCampoString(String json, String campo) {
        if (json == null || campo == null) {
            return null;
        }
        Pattern padraoString = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher m = padraoString.matcher(json);
        if (m.find()) {
            return m.group(1)
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        }
        Pattern padraoLiteral = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*([^,}\\s]+)");
        Matcher mLit = padraoLiteral.matcher(json);
        if (mLit.find()) {
            String val = mLit.group(1).trim();
            return "null".equalsIgnoreCase(val) ? null : val;
        }
        return null;
    }
}

