package br.com.financehub.api.http;

import br.com.financehub.api.dto.JsonUtil;
import br.com.financehub.config.DatabaseConfig;
import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Despesa;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.domain.model.Receita;
import br.com.financehub.domain.model.Relatorio;
import br.com.financehub.domain.service.GerenciadorCategorias;
import br.com.financehub.domain.service.GerenciadorLancamentos;
import br.com.financehub.domain.service.ImportadorArquivoService;
import br.com.financehub.persistence.jdbc.JdbcCategoriaRepository;
import br.com.financehub.persistence.jdbc.JdbcLancamentoRepository;
import br.com.financehub.persistence.jdbc.PersistenciaArquivoRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servidor HTTP nativo (JDK HttpServer) do monólito FinanceHub.
 * Implementa os endpoints /api/v1/* e serve a interface gráfica web estática.
 */
public class FinanceHubHttpServer {

    private final int porta;
    private final GerenciadorCategorias gerenciadorCategorias;
    private final GerenciadorLancamentos gerenciadorLancamentos;
    private final ImportadorArquivoService importadorArquivoService;
    private final DatabaseConfig databaseConfig;
    private final JdbcCategoriaRepository jdbcCategoriaRepository;
    private final JdbcLancamentoRepository jdbcLancamentoRepository;
    private final PersistenciaArquivoRepository persistenciaArquivoRepository;
    private boolean mysqlAtivo;
    private HttpServer server;

    public FinanceHubHttpServer(int porta, Path arquivoPersistenciaLocal) {
        this.porta = porta;
        this.gerenciadorCategorias = new GerenciadorCategorias();
        this.gerenciadorLancamentos = new GerenciadorLancamentos();
        this.importadorArquivoService = new ImportadorArquivoService();
        this.databaseConfig = new DatabaseConfig();
        this.jdbcCategoriaRepository = new JdbcCategoriaRepository(this.databaseConfig);
        this.jdbcLancamentoRepository = new JdbcLancamentoRepository(this.databaseConfig);
        this.persistenciaArquivoRepository = new PersistenciaArquivoRepository(arquivoPersistenciaLocal);
        inicializarDadosPersistidos();
    }

    private synchronized void inicializarDadosPersistidos() {
        this.mysqlAtivo = this.databaseConfig.testarConexao();
        if (this.mysqlAtivo) {
            try {
                for (Categoria c : this.jdbcCategoriaRepository.listarTodas()) {
                    try {
                        this.gerenciadorCategorias.cadastrar(c);
                    } catch (Exception ignored) {
                    }
                }
                for (Lancamento l : this.jdbcLancamentoRepository.listarTodos()) {
                    try {
                        this.gerenciadorLancamentos.cadastrar(l);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                this.mysqlAtivo = false;
            }
        }

        if (!this.mysqlAtivo) {
            try {
                PersistenciaArquivoRepository.DadosPersistidos dados = this.persistenciaArquivoRepository.recuperarObjetos();
                for (Categoria c : dados.getCategorias()) {
                    try {
                        this.gerenciadorCategorias.cadastrar(c);
                    } catch (Exception ignored) {
                    }
                }
                for (Lancamento l : dados.getLancamentos()) {
                    try {
                        this.gerenciadorLancamentos.cadastrar(l);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Caso não haja dados iniciais, semear categorias e lançamentos de demonstração
        if (this.gerenciadorCategorias.listar().isEmpty()) {
            this.gerenciadorCategorias.cadastrar(new Categoria(1L, "Trabalho"));
            this.gerenciadorCategorias.cadastrar(new Categoria(2L, "Alimentacao"));
            this.gerenciadorCategorias.cadastrar(new Categoria(3L, "Moradia"));
            this.gerenciadorCategorias.cadastrar(new Categoria(4L, "Transporte"));
            this.gerenciadorCategorias.cadastrar(new Categoria(5L, "Lazer"));
        }

        if (this.gerenciadorLancamentos.listar().isEmpty()) {
            try {
                Categoria trabalho = this.gerenciadorCategorias.buscarPorId(1L);
                Categoria alimentacao = this.gerenciadorCategorias.buscarPorId(2L);
                Categoria moradia = this.gerenciadorCategorias.buscarPorId(3L);

                this.gerenciadorLancamentos.cadastrar(
                    new Receita(1L, "Salario Mensal", new BigDecimal("4500.00"), LocalDate.now().withDayOfMonth(1), trabalho, 1L)
                );
                this.gerenciadorLancamentos.cadastrar(
                    new Despesa(2L, "Aluguel", new BigDecimal("1600.00"), LocalDate.now().withDayOfMonth(5), moradia, 1L)
                );
                this.gerenciadorLancamentos.cadastrar(
                    new Despesa(3L, "Supermercado", new BigDecimal("620.50"), LocalDate.now().withDayOfMonth(7), alimentacao, 1L)
                );
                sincronizarPersistenciaLocal();
            } catch (Exception ignored) {
            }
        }
    }

    private synchronized void sincronizarPersistenciaLocal() {
        try {
            this.persistenciaArquivoRepository.salvarObjetos(
                this.gerenciadorCategorias.listar(),
                this.gerenciadorLancamentos.listar()
            );
        } catch (Exception ignored) {
        }
    }

    public void iniciar() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(this.porta), 0);
        this.server.createContext("/api/v1/health", this::tratarHealth);
        this.server.createContext("/api/v1/categorias", this::tratarCategorias);
        this.server.createContext("/api/v1/lancamentos", this::tratarLancamentos);
        this.server.createContext("/api/v1/relatorios", this::tratarRelatorios);
        this.server.createContext("/api/v1/importacao", this::tratarImportacao);
        this.server.createContext("/api/v1/exportacao", this::tratarExportacao);
        this.server.setExecutor(null);
        this.server.start();
    }

    public void parar() {
        if (this.server != null) {
            this.server.stop(0);
        }
    }

    public int getPorta() {
        return this.server != null ? this.server.getAddress().getPort() : this.porta;
    }

    private void tratarHealth(HttpExchange exchange) throws IOException {
        if (tratarCorsPreflight(exchange)) {
            return;
        }
        String modo = this.mysqlAtivo ? "MYSQL_JDBC" : "ARQUIVO_LOCAL";
        String json = String.format("{\"status\":\"UP\",\"persistencia\":\"%s\"}", modo);
        enviarJson(exchange, 200, json);
    }

    private synchronized void tratarCategorias(HttpExchange exchange) throws IOException {
        if (tratarCorsPreflight(exchange)) {
            return;
        }
        String metodo = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(metodo)) {
                enviarJson(exchange, 200, JsonUtil.listaCategoriasParaJson(this.gerenciadorCategorias.listar()));
                return;
            }

            if ("POST".equals(metodo)) {
                String body = lerBody(exchange);
                String nome = JsonUtil.extrairCampoString(body, "nome");
                if (nome == null || nome.isBlank()) {
                    enviarJson(exchange, 400, JsonUtil.erroJson("CATEGORIA_INVALIDA", "O campo 'nome' e obrigatorio."));
                    return;
                }
                long novoId = gerarProximoIdCategoria();
                Categoria nova = new Categoria(novoId, nome.trim());
                try {
                    this.gerenciadorCategorias.cadastrar(nova);
                } catch (IllegalArgumentException e) {
                    enviarJson(exchange, 400, JsonUtil.erroJson("CATEGORIA_DUPLICADA", e.getMessage()));
                    return;
                }
                if (this.mysqlAtivo) {
                    try {
                        this.jdbcCategoriaRepository.salvar(nova);
                    } catch (Exception ignored) {
                    }
                }
                sincronizarPersistenciaLocal();
                enviarJson(exchange, 201, JsonUtil.categoriaParaJson(nova));
                return;
            }

            if ("DELETE".equals(metodo)) {
                Long id = extrairIdFinalDaRota(path, "/api/v1/categorias/");
                if (id == null) {
                    enviarJson(exchange, 400, JsonUtil.erroJson("ID_INVALIDO", "Informe o ID da categoria na URL."));
                    return;
                }
                boolean removido = this.gerenciadorCategorias.removerPorId(id);
                if (!removido) {
                    enviarJson(exchange, 404, JsonUtil.erroJson("NAO_ENCONTRADO", "Categoria nao encontrada."));
                    return;
                }
                if (this.mysqlAtivo) {
                    try {
                        this.jdbcCategoriaRepository.removerPorId(id);
                    } catch (Exception ignored) {
                    }
                }
                sincronizarPersistenciaLocal();
                enviarJson(exchange, 200, "{\"removido\":true}");
                return;
            }

            enviarJson(exchange, 405, JsonUtil.erroJson("METODO_NAO_PERMITIDO", "Metodo HTTP nao suportado."));
        } catch (Exception e) {
            enviarJson(exchange, 500, JsonUtil.erroJson("ERRO_INTERNO", e.getMessage()));
        }
    }

    private synchronized void tratarLancamentos(HttpExchange exchange) throws IOException {
        if (tratarCorsPreflight(exchange)) {
            return;
        }
        String metodo = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(metodo)) {
                Map<String, String> params = extrairQueryParams(exchange.getRequestURI());
                List<Lancamento> lista;
                if (params.containsKey("inicio") && params.containsKey("fim")
                    && !params.get("inicio").isBlank() && !params.get("fim").isBlank()) {
                    LocalDate inicio = LocalDate.parse(params.get("inicio"));
                    LocalDate fim = LocalDate.parse(params.get("fim"));
                    lista = this.gerenciadorLancamentos.listarPorPeriodo(inicio, fim);
                } else {
                    lista = this.gerenciadorLancamentos.listar();
                }
                enviarJson(exchange, 200, JsonUtil.listaLancamentosParaJson(lista));
                return;
            }

            if ("POST".equals(metodo)) {
                String body = lerBody(exchange);
                String tipo = JsonUtil.extrairCampoString(body, "tipo");
                String descricao = JsonUtil.extrairCampoString(body, "descricao");
                String valorCentavosStr = JsonUtil.extrairCampoString(body, "valorCentavos");
                String valorStr = JsonUtil.extrairCampoString(body, "valor");
                String dataStr = JsonUtil.extrairCampoString(body, "data");
                String categoriaIdStr = JsonUtil.extrairCampoString(body, "categoriaId");

                if (tipo == null || tipo.isBlank()) {
                    throw new FinanceHubException("TIPO_OBRIGATORIO", "O campo 'tipo' (RECEITA ou DESPESA) e obrigatorio.");
                }

                BigDecimal valor;
                if (valorCentavosStr != null && !valorCentavosStr.isBlank()) {
                    long centavos = Long.parseLong(valorCentavosStr.trim());
                    valor = BigDecimal.valueOf(centavos).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
                } else if (valorStr != null && !valorStr.isBlank()) {
                    valor = new BigDecimal(valorStr.trim().replace(",", ".")).setScale(2, RoundingMode.HALF_UP);
                } else {
                    throw new FinanceHubException("VALOR_OBRIGATORIO", "Informe 'valorCentavos' ou 'valor'.");
                }

                LocalDate data = (dataStr != null && !dataStr.isBlank())
                    ? LocalDate.parse(dataStr.trim())
                    : LocalDate.now();

                Categoria categoria = null;
                if (categoriaIdStr != null && !categoriaIdStr.isBlank()) {
                    long catId = Long.parseLong(categoriaIdStr.trim());
                    try {
                        categoria = this.gerenciadorCategorias.buscarPorId(catId);
                    } catch (Exception ignored) {
                    }
                }
                if (categoria == null && !this.gerenciadorCategorias.listar().isEmpty()) {
                    categoria = this.gerenciadorCategorias.listar().get(0);
                }

                Lancamento novo;
                if ("RECEITA".equalsIgnoreCase(tipo.trim())) {
                    novo = new Receita(null, descricao, valor, data, categoria, 1L);
                } else if ("DESPESA".equalsIgnoreCase(tipo.trim())) {
                    novo = new Despesa(null, descricao, valor, data, categoria, 1L);
                } else {
                    throw new FinanceHubException("TIPO_INVALIDO", "Tipo deve ser RECEITA ou DESPESA.");
                }

                this.gerenciadorLancamentos.cadastrar(novo);
                if (categoria != null) {
                    categoria.adicionarLancamento(novo);
                }

                if (this.mysqlAtivo) {
                    try {
                        this.jdbcLancamentoRepository.salvar(novo);
                    } catch (Exception ignored) {
                    }
                }
                sincronizarPersistenciaLocal();
                enviarJson(exchange, 201, JsonUtil.lancamentoParaJson(novo));
                return;
            }

            if ("DELETE".equals(metodo)) {
                Long id = extrairIdFinalDaRota(path, "/api/v1/lancamentos/");
                if (id == null) {
                    enviarJson(exchange, 400, JsonUtil.erroJson("ID_INVALIDO", "Informe o ID do lancamento na URL."));
                    return;
                }
                boolean removido = this.gerenciadorLancamentos.removerPorId(id);
                if (!removido) {
                    enviarJson(exchange, 404, JsonUtil.erroJson("NAO_ENCONTRADO", "Lancamento nao encontrado."));
                    return;
                }
                if (this.mysqlAtivo) {
                    try {
                        this.jdbcLancamentoRepository.removerPorId(id);
                    } catch (Exception ignored) {
                    }
                }
                sincronizarPersistenciaLocal();
                enviarJson(exchange, 200, "{\"removido\":true}");
                return;
            }

            enviarJson(exchange, 405, JsonUtil.erroJson("METODO_NAO_PERMITIDO", "Metodo HTTP nao suportado."));
        } catch (FinanceHubException e) {
            enviarJson(exchange, 400, JsonUtil.erroJson(e.getCodigo(), e.getMessage()));
        } catch (Exception e) {
            enviarJson(exchange, 400, JsonUtil.erroJson("REQUISICAO_INVALIDA", e.getMessage()));
        }
    }

    private synchronized void tratarRelatorios(HttpExchange exchange) throws IOException {
        if (tratarCorsPreflight(exchange)) {
            return;
        }
        try {
            Map<String, String> params = extrairQueryParams(exchange.getRequestURI());
            Relatorio relatorio;
            if (params.containsKey("inicio") && params.containsKey("fim")
                && !params.get("inicio").isBlank() && !params.get("fim").isBlank()) {
                LocalDate inicio = LocalDate.parse(params.get("inicio"));
                LocalDate fim = LocalDate.parse(params.get("fim"));
                relatorio = this.gerenciadorLancamentos.gerarRelatorioPorPeriodo(inicio, fim);
            } else {
                relatorio = this.gerenciadorLancamentos.gerarRelatorio();
            }
            enviarJson(exchange, 200, JsonUtil.relatorioParaJson(relatorio));
        } catch (FinanceHubException e) {
            enviarJson(exchange, 400, JsonUtil.erroJson(e.getCodigo(), e.getMessage()));
        } catch (Exception e) {
            enviarJson(exchange, 400, JsonUtil.erroJson("PARAMETRO_INVALIDO", e.getMessage()));
        }
    }

    private synchronized void tratarImportacao(HttpExchange exchange) throws IOException {
        if (tratarCorsPreflight(exchange)) {
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            enviarJson(exchange, 405, JsonUtil.erroJson("METODO_NAO_PERMITIDO", "Use POST para importar CSV/TXT."));
            return;
        }
        try {
            String body = lerBody(exchange);
            String conteudoCsv = body;
            if (body != null && body.trim().startsWith("{")) {
                String extraido = JsonUtil.extrairCampoString(body, "csv");
                if (extraido == null) {
                    extraido = JsonUtil.extrairCampoString(body, "conteudo");
                }
                if (extraido != null) {
                    conteudoCsv = extraido;
                }
            }

            ArrayList<Lancamento> importados = this.importadorArquivoService.importarDeConteudo(
                conteudoCsv,
                this.gerenciadorLancamentos,
                this.gerenciadorCategorias
            );

            if (this.mysqlAtivo) {
                for (Lancamento l : importados) {
                    try {
                        this.jdbcLancamentoRepository.salvar(l);
                    } catch (Exception ignored) {
                    }
                }
            }
            sincronizarPersistenciaLocal();

            String resposta = String.format(
                "{\"quantidadeImportada\":%d,\"lancamentos\":%s}",
                importados.size(),
                JsonUtil.listaLancamentosParaJson(importados)
            );
            enviarJson(exchange, 201, resposta);
        } catch (FinanceHubException e) {
            enviarJson(exchange, 400, JsonUtil.erroJson(e.getCodigo(), e.getMessage()));
        } catch (Exception e) {
            enviarJson(exchange, 400, JsonUtil.erroJson("ERRO_IMPORTACAO", e.getMessage()));
        }
    }

    private synchronized void tratarExportacao(HttpExchange exchange) throws IOException {
        if (tratarCorsPreflight(exchange)) {
            return;
        }
        StringBuilder sb = new StringBuilder("TIPO;DESCRICAO;VALOR;DATA;CATEGORIA\n");
        for (Lancamento l : this.gerenciadorLancamentos.listar()) {
            String catNome = l.getCategoria() != null ? l.getCategoria().getNome() : "Geral";
            sb.append(l.getTipo()).append(";")
              .append(l.getDescricao()).append(";")
              .append(l.getValor().setScale(2, RoundingMode.HALF_UP).toPlainString()).append(";")
              .append(l.getData().toString()).append(";")
              .append(catNome).append("\n");
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        adicionarHeadersCors(exchange);
        exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"financehub-lancamentos.csv\"");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private long gerarProximoIdCategoria() {
        long max = 0L;
        for (Categoria c : this.gerenciadorCategorias.listar()) {
            if (c.getId() > max) {
                max = c.getId();
            }
        }
        return max + 1L;
    }

    private Long extrairIdFinalDaRota(String path, String prefixo) {
        if (path == null || !path.startsWith(prefixo)) {
            return null;
        }
        String resto = path.substring(prefixo.length()).trim();
        if (resto.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(resto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, String> extrairQueryParams(URI uri) {
        Map<String, String> mapa = new HashMap<>();
        String query = uri.getQuery();
        if (query == null || query.isBlank()) {
            return mapa;
        }
        for (String par : query.split("&")) {
            String[] kv = par.split("=", 2);
            if (kv.length == 2) {
                mapa.put(kv[0].trim(), kv[1].trim());
            }
        }
        return mapa;
    }

    private String lerBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private boolean tratarCorsPreflight(HttpExchange exchange) throws IOException {
        adicionarHeadersCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private void adicionarHeadersCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Accept");
    }

    private void enviarJson(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        adicionarHeadersCors(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

