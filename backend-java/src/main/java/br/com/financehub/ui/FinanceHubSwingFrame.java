package br.com.financehub.ui;

import br.com.financehub.domain.exception.FinanceHubException;
import br.com.financehub.domain.model.Categoria;
import br.com.financehub.domain.model.Despesa;
import br.com.financehub.domain.model.Lancamento;
import br.com.financehub.domain.model.Receita;
import br.com.financehub.domain.model.Relatorio;
import br.com.financehub.domain.model.Usuario;
import br.com.financehub.domain.service.CotacaoMoedaService;
import br.com.financehub.domain.service.GerenciadorCategorias;
import br.com.financehub.domain.service.GerenciadorLancamentos;
import br.com.financehub.domain.service.ImportadorArquivoService;
import br.com.financehub.persistence.RepositorioDados;
import br.com.financehub.persistence.jdbc.JdbcRepositorioDados;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Aplicativo desktop FinanceHub estruturado em múltiplas telas independentes
 * (Início, Adicionar Lançamento, Consultar Período, Categorias, Arquivos & API)
 * navegadas por um menu lateral fixo.
 */
public class FinanceHubSwingFrame extends JFrame {

    // Identificadores das telas do aplicativo
    private static final String TELA_INICIO = "TELA_INICIO";
    private static final String TELA_ADICIONAR = "TELA_ADICIONAR";
    private static final String TELA_PERIODO = "TELA_PERIODO";
    private static final String TELA_CATEGORIAS = "TELA_CATEGORIAS";
    private static final String TELA_ARQUIVOS_API = "TELA_ARQUIVOS_API";

    // Paleta de cores: Preto, Cinza Escuro e Ciano
    private static final Color BG_PRETO = new Color(8, 11, 16);
    private static final Color BG_SIDEBAR = new Color(12, 16, 23);
    private static final Color BG_CAMPO = new Color(14, 19, 28);
    private static final Color CARD_CINZA = new Color(18, 24, 34);
    private static final Color CARD_CINZA_CLARO = new Color(26, 35, 49);
    private static final Color BORDA_CINZA = new Color(42, 54, 72);
    private static final Color TEXTO_CLARO = new Color(238, 244, 250);
    private static final Color TEXTO_CINZA = new Color(144, 160, 182);
    private static final Color CIANO_VIVO = new Color(34, 211, 238);
    private static final Color CIANO_ESCURO = new Color(6, 182, 212);
    private static final Color VERDE_RECEITA = new Color(45, 212, 191);
    private static final Color CORAL_DESPESA = new Color(248, 113, 113);
    private static final Color AMBAR_ALERTA = new Color(251, 191, 36);

    private static final Font FONTE_TITULO_PAGINA = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONTE_SECAO = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONTE_VALOR_CARD = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONTE_ROTULO = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONTE_TEXTO = new Font("Segoe UI", Font.PLAIN, 13);

    // Objetos de domínio, serviços e contrato de persistência
    private final Usuario usuarioAtual;
    private final GerenciadorCategorias gerenciadorCategorias;
    private final GerenciadorLancamentos gerenciadorLancamentos;
    private final ImportadorArquivoService importadorArquivoService;
    private final CotacaoMoedaService cotacaoMoedaService;
    private final RepositorioDados repositorioDados;

    private final NumberFormat moedaBRL = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private final NumberFormat moedaUSD = NumberFormat.getCurrencyInstance(Locale.US);

    // Navegação principal por telas (CardLayout)
    private CardLayout gerenciadorTelas;
    private JPanel painelTelas;
    private JButton btnMenuInicio;
    private JButton btnMenuAdicionar;
    private JButton btnMenuPeriodo;
    private JButton btnMenuCategorias;
    private JButton btnMenuArquivos;
    private JLabel lblUsuarioSidebar;
    private JLabel lblEmailSidebar;

    // ==================== COMPONENTES DA TELA 1: INÍCIO ====================
    private JLabel lblCardReceitas;
    private JLabel lblSubReceitas;
    private JProgressBar barraReceitas;

    private JLabel lblCardDespesas;
    private JLabel lblSubDespesas;
    private JProgressBar barraDespesas;

    private JLabel lblCardSaldo;
    private JLabel lblSubPoupanca;
    private JProgressBar barraPoupanca;

    private JLabel lblCardMeta;
    private JLabel lblSubMeta;
    private JProgressBar barraMeta;

    private JLabel lblCardCambio;
    private JLabel lblSubCambio;

    private JPanel painelBarrasCategoriasInicio;
    private JLabel lblInsightMaiorReceita;
    private JLabel lblInsightMaiorDespesa;
    private JLabel lblInsightTicketMedio;
    private JLabel lblInsightStatusOrcamento;
    private DefaultTableModel modeloTabelaRecentes;

    // ==================== COMPONENTES DA TELA 2: ADICIONAR / EDITAR ====================
    private Long idEmEdicao = null;
    private JLabel lblTituloFormulario;
    private JComboBox<String> cmbTipo;
    private JTextField txtDescricao;
    private JTextField txtValor;
    private JTextField txtData;
    private final DefaultComboBoxModel<Categoria> modeloComboCategorias = new DefaultComboBoxModel<>();
    private JComboBox<Categoria> cmbCategoria;

    // ==================== COMPONENTES DA TELA 3: EXTRATO E PERÍODO ====================
    private JTextField txtBuscaDescricao;
    private JComboBox<String> cmbFiltroTipo;
    private JTextField txtFiltroInicio;
    private JTextField txtFiltroFim;
    private JLabel lblPeriodoReceitas;
    private JLabel lblPeriodoDespesas;
    private JLabel lblPeriodoSaldo;
    private JLabel lblResumoPeriodoTexto;
    private DefaultTableModel modeloTabelaPeriodo;
    private JTable tabelaPeriodo;

    // ==================== COMPONENTES DA TELA 4: CATEGORIAS ====================
    private JTextField txtNovaCategoria;
    private DefaultTableModel modeloTabelaCategorias;
    private JTable tabelaCategorias;
    private final DefaultListModel<String> modeloListaCategoriasSimples = new DefaultListModel<>();

    // ==================== COMPONENTES DA TELA 5: ARQUIVOS CSV & API ====================
    private JTextArea txtAreaCsv;
    private JLabel lblStatusApiDetalhe;

    public FinanceHubSwingFrame(Path arquivoPersistenciaLocal) {
        super("FinanceHub — Gerenciador Financeiro Pessoal");
        configurarCoresDialogo();

        this.usuarioAtual = new Usuario(1L, "Rafael", "rafael@financehub.com");
        this.gerenciadorCategorias = new GerenciadorCategorias();
        this.gerenciadorLancamentos = new GerenciadorLancamentos();
        this.importadorArquivoService = new ImportadorArquivoService();
        this.cotacaoMoedaService = new CotacaoMoedaService();
        this.repositorioDados = new JdbcRepositorioDados(arquivoLocalOuPadrao(arquivoPersistenciaLocal));

        carregarDadosIniciais();
        construirEstruturaApp();
        atualizarTodasAsTelas(null, null);
        navegarParaTela(TELA_INICIO, this.btnMenuInicio);
    }

    private Path arquivoLocalOuPadrao(Path caminho) {
        return caminho != null ? caminho : Path.of("data", "financehub-dados.db");
    }

    private void configurarCoresDialogo() {
        UIManager.put("OptionPane.background", CARD_CINZA);
        UIManager.put("Panel.background", CARD_CINZA);
        UIManager.put("OptionPane.messageForeground", TEXTO_CLARO);
        UIManager.put("ComboBox.selectionBackground", CIANO_ESCURO);
        UIManager.put("ComboBox.selectionForeground", BG_PRETO);
    }

    private void carregarDadosIniciais() {
        try {
            RepositorioDados.DadosCarregados dados = this.repositorioDados.recuperarObjetos();
            for (Categoria c : dados.getCategorias()) {
                this.gerenciadorCategorias.cadastrar(c);
            }
            for (Lancamento l : dados.getLancamentos()) {
                this.gerenciadorLancamentos.cadastrar(l);
                this.usuarioAtual.adicionarLancamento(l);
            }
        } catch (Exception e) {
            System.err.println("Aviso ao carregar dados iniciais: " + e.getMessage());
        }

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
                Categoria moradia = this.gerenciadorCategorias.buscarPorId(3L);
                Categoria alimentacao = this.gerenciadorCategorias.buscarPorId(2L);

                registrarNovoLancamentoInterno(new Receita(1L, "Salario Mensal", new BigDecimal("4500.00"), LocalDate.now().withDayOfMonth(1), trabalho, this.usuarioAtual.getId()));
                registrarNovoLancamentoInterno(new Despesa(2L, "Aluguel", new BigDecimal("1600.00"), LocalDate.now().withDayOfMonth(5), moradia, this.usuarioAtual.getId()));
                registrarNovoLancamentoInterno(new Despesa(3L, "Supermercado", new BigDecimal("620.50"), LocalDate.now().withDayOfMonth(7), alimentacao, this.usuarioAtual.getId()));
                salvarAlteracoes();
            } catch (FinanceHubException e) {
                mostrarErro("Erro Inicial", e.getMessage());
            }
        }
    }

    private void registrarNovoLancamentoInterno(Lancamento lancamento) throws FinanceHubException {
        this.gerenciadorLancamentos.cadastrar(lancamento);
        this.usuarioAtual.adicionarLancamento(lancamento);
    }

    private void salvarAlteracoes() {
        try {
            this.repositorioDados.salvarObjetos(
                this.gerenciadorCategorias.listar(),
                this.gerenciadorLancamentos.listar()
            );
        } catch (FinanceHubException e) {
            mostrarErro("Erro de Persistência", e.getMessage());
        }
    }

    // ==================== ESTRUTURA GERAL DO APP (SIDEBAR + TELAS) ====================

    private void construirEstruturaApp() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 840);
        setMinimumSize(new Dimension(1150, 720));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout(0, 0));
        raiz.setBackground(BG_PRETO);
        setContentPane(raiz);

        raiz.add(criarMenuLateralFixo(), BorderLayout.WEST);

        this.gerenciadorTelas = new CardLayout();
        this.painelTelas = new JPanel(this.gerenciadorTelas);
        this.painelTelas.setBackground(BG_PRETO);
        this.painelTelas.setBorder(new EmptyBorder(20, 24, 20, 24));

        this.painelTelas.add(criarTelaInicio(), TELA_INICIO);
        this.painelTelas.add(criarTelaAdicionarLancamento(), TELA_ADICIONAR);
        this.painelTelas.add(criarTelaExtratoPeriodo(), TELA_PERIODO);
        this.painelTelas.add(criarTelaCategorias(), TELA_CATEGORIAS);
        this.painelTelas.add(criarTelaArquivosEApi(), TELA_ARQUIVOS_API);

        raiz.add(this.painelTelas, BorderLayout.CENTER);
    }

    private JPanel criarMenuLateralFixo() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 20));
        sidebar.setPreferredSize(new Dimension(245, 800));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDA_CINZA),
            new EmptyBorder(20, 16, 20, 16)
        ));

        // Topo do menu: Logo + Nome do App
        JPanel blocoMarca = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        blocoMarca.setOpaque(false);

        JLabel logo = new JLabel("FH", SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(44, 44));
        logo.setOpaque(true);
        logo.setBackground(CIANO_VIVO);
        logo.setForeground(BG_PRETO);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JPanel textosMarca = new JPanel();
        textosMarca.setLayout(new BoxLayout(textosMarca, BoxLayout.Y_AXIS));
        textosMarca.setOpaque(false);

        JLabel nomeApp = new JLabel("FinanceHub");
        nomeApp.setForeground(TEXTO_CLARO);
        nomeApp.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel subApp = new JLabel("Gestão Pessoal POO");
        subApp.setForeground(CIANO_VIVO);
        subApp.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        textosMarca.add(nomeApp);
        textosMarca.add(subApp);

        blocoMarca.add(logo);
        blocoMarca.add(textosMarca);

        // Botões de navegação das 5 telas
        JPanel menuBotoes = new JPanel();
        menuBotoes.setLayout(new BoxLayout(menuBotoes, BoxLayout.Y_AXIS));
        menuBotoes.setOpaque(false);

        JLabel lblMenuTitulo = new JLabel("MENU PRINCIPAL");
        lblMenuTitulo.setForeground(TEXTO_CINZA);
        lblMenuTitulo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMenuTitulo.setBorder(new EmptyBorder(10, 6, 8, 0));
        lblMenuTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuBotoes.add(lblMenuTitulo);

        this.btnMenuInicio = criarBotaoMenuLateral("Início (Visão Geral)");
        this.btnMenuAdicionar = criarBotaoMenuLateral("Adicionar Lançamento");
        this.btnMenuPeriodo = criarBotaoMenuLateral("Extrato por Período");
        this.btnMenuCategorias = criarBotaoMenuLateral("Categorias e Saldos");
        this.btnMenuArquivos = criarBotaoMenuLateral("Arquivos CSV e Câmbio API");

        this.btnMenuInicio.addActionListener(e -> navegarParaTela(TELA_INICIO, this.btnMenuInicio));
        this.btnMenuAdicionar.addActionListener(e -> {
            limparFormularioLancamento();
            navegarParaTela(TELA_ADICIONAR, this.btnMenuAdicionar);
        });
        this.btnMenuPeriodo.addActionListener(e -> navegarParaTela(TELA_PERIODO, this.btnMenuPeriodo));
        this.btnMenuCategorias.addActionListener(e -> navegarParaTela(TELA_CATEGORIAS, this.btnMenuCategorias));
        this.btnMenuArquivos.addActionListener(e -> navegarParaTela(TELA_ARQUIVOS_API, this.btnMenuArquivos));

        menuBotoes.add(this.btnMenuInicio);
        menuBotoes.add(Box.createVerticalStrut(8));
        menuBotoes.add(this.btnMenuAdicionar);
        menuBotoes.add(Box.createVerticalStrut(8));
        menuBotoes.add(this.btnMenuPeriodo);
        menuBotoes.add(Box.createVerticalStrut(8));
        menuBotoes.add(this.btnMenuCategorias);
        menuBotoes.add(Box.createVerticalStrut(8));
        menuBotoes.add(this.btnMenuArquivos);

        // Rodapé do menu lateral: Perfil do Usuário e Meta
        JPanel cardUsuarioSidebar = new JPanel();
        cardUsuarioSidebar.setLayout(new BoxLayout(cardUsuarioSidebar, BoxLayout.Y_AXIS));
        cardUsuarioSidebar.setBackground(CARD_CINZA);
        cardUsuarioSidebar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTitUser = new JLabel("TITULAR DA CONTA");
        lblTitUser.setForeground(TEXTO_CINZA);
        lblTitUser.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.lblUsuarioSidebar = new JLabel(this.usuarioAtual.getNome());
        this.lblUsuarioSidebar.setForeground(TEXTO_CLARO);
        this.lblUsuarioSidebar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        this.lblUsuarioSidebar.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.lblEmailSidebar = new JLabel(this.usuarioAtual.getEmail());
        this.lblEmailSidebar.setForeground(CIANO_VIVO);
        this.lblEmailSidebar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        this.lblEmailSidebar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEditarPerfil = criarBotaoSecundario("Editar Perfil / Meta");
        btnEditarPerfil.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnEditarPerfil.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnEditarPerfil.addActionListener(e -> acaoEditarUsuarioEMeta());

        cardUsuarioSidebar.add(lblTitUser);
        cardUsuarioSidebar.add(Box.createVerticalStrut(4));
        cardUsuarioSidebar.add(this.lblUsuarioSidebar);
        cardUsuarioSidebar.add(this.lblEmailSidebar);
        cardUsuarioSidebar.add(Box.createVerticalStrut(10));
        cardUsuarioSidebar.add(btnEditarPerfil);

        sidebar.add(blocoMarca, BorderLayout.NORTH);
        sidebar.add(menuBotoes, BorderLayout.CENTER);
        sidebar.add(cardUsuarioSidebar, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton criarBotaoMenuLateral(String texto) {
        JButton btn = new JButton(texto);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        aplicarEstiloBotaoMenu(btn, false);
        return btn;
    }

    private void aplicarEstiloBotaoMenu(JButton btn, boolean ativo) {
        if (ativo) {
            btn.setBackground(CIANO_VIVO);
            btn.setForeground(BG_PRETO);
            btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        } else {
            btn.setBackground(CARD_CINZA);
            btn.setForeground(TEXTO_CLARO);
            btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDA_CINZA, 1, true),
                new EmptyBorder(9, 13, 9, 13)
            ));
        }
    }

    private void navegarParaTela(String identificadorTela, JButton botaoAtivo) {
        this.gerenciadorTelas.show(this.painelTelas, identificadorTela);
        aplicarEstiloBotaoMenu(this.btnMenuInicio, botaoAtivo == this.btnMenuInicio);
        aplicarEstiloBotaoMenu(this.btnMenuAdicionar, botaoAtivo == this.btnMenuAdicionar);
        aplicarEstiloBotaoMenu(this.btnMenuPeriodo, botaoAtivo == this.btnMenuPeriodo);
        aplicarEstiloBotaoMenu(this.btnMenuCategorias, botaoAtivo == this.btnMenuCategorias);
        aplicarEstiloBotaoMenu(this.btnMenuArquivos, botaoAtivo == this.btnMenuArquivos);
    }

    // ==================== TELA 1: INÍCIO (VISÃO GERAL / DASHBOARD) ====================

    private JPanel criarTelaInicio() {
        JPanel tela = new JPanel(new BorderLayout(0, 16));
        tela.setOpaque(false);

        // Cabeçalho da tela Início
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);

        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.setOpaque(false);

        JLabel titulo = new JLabel("Visão Geral do Orçamento");
        titulo.setForeground(TEXTO_CLARO);
        titulo.setFont(FONTE_TITULO_PAGINA);

        JLabel subtitulo = new JLabel("Resumo consolidado das suas receitas, despesas, metas e indicadores polimórficos");
        subtitulo.setForeground(TEXTO_CINZA);
        subtitulo.setFont(FONTE_TEXTO);

        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(2));
        titulos.add(subtitulo);

        JPanel acoesRapidasTopo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acoesRapidasTopo.setOpaque(false);

        JButton btnIrAdicionar = criarBotaoCiano("+ Novo Lançamento");
        btnIrAdicionar.addActionListener(e -> {
            limparFormularioLancamento();
            navegarParaTela(TELA_ADICIONAR, this.btnMenuAdicionar);
        });

        JButton btnIrPeriodo = criarBotaoSecundario("Filtrar por Período");
        btnIrPeriodo.addActionListener(e -> navegarParaTela(TELA_PERIODO, this.btnMenuPeriodo));

        acoesRapidasTopo.add(btnIrPeriodo);
        acoesRapidasTopo.add(btnIrAdicionar);

        cabecalho.add(titulos, BorderLayout.WEST);
        cabecalho.add(acoesRapidasTopo, BorderLayout.EAST);

        // Grade de 5 Cards de Resumo
        JPanel gradeCards = new JPanel(new GridLayout(1, 5, 12, 0));
        gradeCards.setOpaque(false);

        this.lblCardReceitas = new JLabel("R$ 0,00");
        this.lblSubReceitas = new JLabel("0 entradas");
        this.barraReceitas = criarBarraProgresso(VERDE_RECEITA);
        gradeCards.add(criarCardDashboard("RECEITAS TOTAIS", this.lblCardReceitas, this.lblSubReceitas, this.barraReceitas, VERDE_RECEITA));

        this.lblCardDespesas = new JLabel("R$ 0,00");
        this.lblSubDespesas = new JLabel("0 saídas");
        this.barraDespesas = criarBarraProgresso(CORAL_DESPESA);
        gradeCards.add(criarCardDashboard("DESPESAS TOTAIS", this.lblCardDespesas, this.lblSubDespesas, this.barraDespesas, CORAL_DESPESA));

        this.lblCardSaldo = new JLabel("R$ 0,00");
        this.lblSubPoupanca = new JLabel("Poupança: 0%");
        this.barraPoupanca = criarBarraProgresso(CIANO_VIVO);
        gradeCards.add(criarCardDashboard("SALDO LÍQUIDO (R$)", this.lblCardSaldo, this.lblSubPoupanca, this.barraPoupanca, CIANO_VIVO));

        this.lblCardMeta = new JLabel("R$ 3.500,00");
        this.lblSubMeta = new JLabel("0% utilizado");
        this.barraMeta = criarBarraProgresso(CIANO_VIVO);
        gradeCards.add(criarCardDashboard("META DE GASTOS", this.lblCardMeta, this.lblSubMeta, this.barraMeta, AMBAR_ALERTA));

        this.lblCardCambio = new JLabel("US$ 0.00");
        this.lblSubCambio = new JLabel("€ 0.00 (API)");
        JProgressBar barraCambio = criarBarraProgresso(CIANO_ESCURO);
        barraCambio.setValue(100);
        gradeCards.add(criarCardDashboard("SALDO EM USD / EUR", this.lblCardCambio, this.lblSubCambio, barraCambio, CIANO_VIVO));

        JPanel blocoNorte = new JPanel(new BorderLayout(0, 16));
        blocoNorte.setOpaque(false);
        blocoNorte.add(cabecalho, BorderLayout.NORTH);
        blocoNorte.add(gradeCards, BorderLayout.CENTER);

        // Miolo da tela Início: Esquerda (Gráfico por Categoria + Insights) | Direita (Últimos Lançamentos)
        JPanel mioloInicio = new JPanel(new GridLayout(1, 2, 16, 0));
        mioloInicio.setOpaque(false);

        JPanel colunaAnalise = new JPanel(new BorderLayout(0, 14));
        colunaAnalise.setOpaque(false);

        // Card de Barras por Categoria
        JPanel cardBarras = criarCardPadrao("Distribuição de Saldo por Categoria");
        this.painelBarrasCategoriasInicio = new JPanel();
        this.painelBarrasCategoriasInicio.setLayout(new BoxLayout(this.painelBarrasCategoriasInicio, BoxLayout.Y_AXIS));
        this.painelBarrasCategoriasInicio.setBackground(CARD_CINZA);

        JScrollPane scrollBarras = new JScrollPane(this.painelBarrasCategoriasInicio);
        scrollBarras.setBorder(null);
        scrollBarras.getViewport().setBackground(CARD_CINZA);
        cardBarras.add(scrollBarras, BorderLayout.CENTER);

        // Card de 4 Indicadores
        JPanel cardInsights = criarCardPadrao("Indicadores e Diagnóstico Financeiro");
        JPanel gradeInsights = new JPanel(new GridLayout(2, 2, 10, 10));
        gradeInsights.setOpaque(false);

        this.lblInsightMaiorReceita = new JLabel("-");
        this.lblInsightMaiorDespesa = new JLabel("-");
        this.lblInsightTicketMedio = new JLabel("-");
        this.lblInsightStatusOrcamento = new JLabel("-");

        gradeInsights.add(criarMiniCardInsight("MAIOR RECEITA", this.lblInsightMaiorReceita, VERDE_RECEITA));
        gradeInsights.add(criarMiniCardInsight("MAIOR DESPESA", this.lblInsightMaiorDespesa, CORAL_DESPESA));
        gradeInsights.add(criarMiniCardInsight("TICKET MÉDIO", this.lblInsightTicketMedio, CIANO_VIVO));
        gradeInsights.add(criarMiniCardInsight("DIAGNÓSTICO DO MÊS", this.lblInsightStatusOrcamento, CIANO_VIVO));
        cardInsights.add(gradeInsights, BorderLayout.CENTER);

        colunaAnalise.add(cardBarras, BorderLayout.CENTER);
        colunaAnalise.add(cardInsights, BorderLayout.SOUTH);

        // Card de Movimentações Recentes
        JPanel cardRecentes = criarCardPadrao("Últimas Movimentações Registradas");
        String[] colRecentes = {"Data", "Tipo", "Resumo (Polimorfismo)", "Categoria", "Impacto"};
        this.modeloTabelaRecentes = new DefaultTableModel(colRecentes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabRecentes = criarTabelaEstilizada(this.modeloTabelaRecentes, 1, 4);
        JScrollPane scrollRecentes = new JScrollPane(tabRecentes);
        scrollRecentes.getViewport().setBackground(BG_CAMPO);
        scrollRecentes.setBorder(new LineBorder(BORDA_CINZA, 1));
        cardRecentes.add(scrollRecentes, BorderLayout.CENTER);

        mioloInicio.add(colunaAnalise);
        mioloInicio.add(cardRecentes);

        tela.add(blocoNorte, BorderLayout.NORTH);
        tela.add(mioloInicio, BorderLayout.CENTER);
        return tela;
    }

    // ==================== TELA 2: ADICIONAR / EDITAR LANÇAMENTO ====================

    private JPanel criarTelaAdicionarLancamento() {
        JPanel tela = new JPanel(new BorderLayout(0, 18));
        tela.setOpaque(false);

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);

        this.lblTituloFormulario = new JLabel("Adicionar Novo Lançamento Financeiro");
        this.lblTituloFormulario.setForeground(TEXTO_CLARO);
        this.lblTituloFormulario.setFont(FONTE_TITULO_PAGINA);
        cabecalho.add(this.lblTituloFormulario, BorderLayout.WEST);

        // Card central amplo para o formulário
        JPanel wrapperCentro = new JPanel(new GridLayout(1, 2, 20, 0));
        wrapperCentro.setOpaque(false);

        JPanel cardForm = new JPanel();
        cardForm.setLayout(new BoxLayout(cardForm, BoxLayout.Y_AXIS));
        cardForm.setBackground(CARD_CINZA);
        cardForm.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(24, 28, 24, 28)
        ));

        JLabel subForm = new JLabel("Dados da Movimentação (Receita ou Despesa)");
        subForm.setForeground(CIANO_VIVO);
        subForm.setFont(FONTE_SECAO);
        cardForm.add(criarBlocoTituloComBarraCiano(subForm));
        cardForm.add(Box.createVerticalStrut(18));

        this.cmbTipo = new JComboBox<>(new String[]{"RECEITA", "DESPESA"});
        estilizarCombo(this.cmbTipo);

        this.txtDescricao = criarCampoTexto("");
        this.txtValor = criarCampoTexto("250.00");
        this.txtData = criarCampoTexto(LocalDate.now().toString());

        this.cmbCategoria = new JComboBox<>(this.modeloComboCategorias);
        estilizarCombo(this.cmbCategoria);
        this.cmbCategoria.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Categoria c) {
                    lbl.setText(c.getId() + " - " + c.getNome());
                }
                lbl.setBackground(isSelected ? CIANO_ESCURO : BG_CAMPO);
                lbl.setForeground(isSelected ? BG_PRETO : TEXTO_CLARO);
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                return lbl;
            }
        });

        cardForm.add(criarRotulo("1. TIPO DE LANÇAMENTO (HERANÇA POO: RECEITA OU DESPESA)"));
        cardForm.add(this.cmbTipo);
        cardForm.add(Box.createVerticalStrut(14));

        cardForm.add(criarRotulo("2. DESCRIÇÃO DETALHADA"));
        cardForm.add(this.txtDescricao);
        cardForm.add(Box.createVerticalStrut(14));

        cardForm.add(criarRotulo("3. VALOR EM REAIS (R$)"));
        cardForm.add(this.txtValor);
        cardForm.add(Box.createVerticalStrut(14));

        cardForm.add(criarRotulo("4. DATA DO LANÇAMENTO (AAAA-MM-DD)"));
        cardForm.add(this.txtData);
        cardForm.add(Box.createVerticalStrut(8));

        JPanel botoesDataRapida = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botoesDataRapida.setOpaque(false);
        botoesDataRapida.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnHoje = criarBotaoSecundario("Hoje");
        btnHoje.addActionListener(e -> this.txtData.setText(LocalDate.now().toString()));

        JButton btnOntem = criarBotaoSecundario("Ontem");
        btnOntem.addActionListener(e -> this.txtData.setText(LocalDate.now().minusDays(1).toString()));

        JButton btnInicioMes = criarBotaoSecundario("Dia 1 do Mês");
        btnInicioMes.addActionListener(e -> this.txtData.setText(LocalDate.now().withDayOfMonth(1).toString()));

        botoesDataRapida.add(btnHoje);
        botoesDataRapida.add(btnOntem);
        botoesDataRapida.add(btnInicioMes);
        cardForm.add(botoesDataRapida);
        cardForm.add(Box.createVerticalStrut(14));

        cardForm.add(criarRotulo("5. CATEGORIA ASSOCIADA"));
        cardForm.add(this.cmbCategoria);
        cardForm.add(Box.createVerticalStrut(24));

        JButton btnSalvar = criarBotaoCiano("Salvar Lançamento e Ir para o Extrato");
        btnSalvar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSalvar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnSalvar.addActionListener(e -> acaoSalvarLancamento());
        cardForm.add(btnSalvar);

        cardForm.add(Box.createVerticalStrut(10));

        JButton btnLimpar = criarBotaoSecundario("Limpar Campos / Cancelar Edição");
        btnLimpar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLimpar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLimpar.addActionListener(e -> limparFormularioLancamento());
        cardForm.add(btnLimpar);

        // Card explicativo à direita mostrando o funcionamento POO em tempo real
        JPanel cardExplicacao = criarCardPadrao("Como o Sistema Processa este Lançamento (POO)");
        JPanel textoExp = new JPanel();
        textoExp.setLayout(new BoxLayout(textoExp, BoxLayout.Y_AXIS));
        textoExp.setOpaque(false);
        textoExp.setBorder(new EmptyBorder(10, 4, 10, 4));

        textoExp.add(criarItemExplicacao(
            "Herança e Especialização",
            "Ao selecionar RECEITA ou DESPESA, o sistema instancia a subclasse correspondente que herda da classe abstrata Lancamento."
        ));
        textoExp.add(Box.createVerticalStrut(14));
        textoExp.add(criarItemExplicacao(
            "Polimorfismo (calcularImpacto e getResumo)",
            "• Receita retorna impacto positivo (+valor) e sobrescreve getResumo().\n• Despesa retorna impacto negativo (-valor) e sobrescreve getResumo()."
        ));
        textoExp.add(Box.createVerticalStrut(14));
        textoExp.add(criarItemExplicacao(
            "Associação entre Objetos",
            "O lançamento é automaticamente associado ao objeto Categoria selecionado e à coleção do Usuario ativo."
        ));
        textoExp.add(Box.createVerticalStrut(14));
        textoExp.add(criarItemExplicacao(
            "Persistência via Interface (RepositorioDados)",
            "Ao clicar em Salvar, o contrato RepositorioDados grava imediatamente os objetos atualizados em disco."
        ));

        cardExplicacao.add(textoExp, BorderLayout.CENTER);

        wrapperCentro.add(cardForm);
        wrapperCentro.add(cardExplicacao);

        tela.add(cabecalho, BorderLayout.NORTH);
        tela.add(wrapperCentro, BorderLayout.CENTER);
        return tela;
    }

    private JPanel criarItemExplicacao(String titulo, String descricao) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CAMPO);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblT = new JLabel(titulo);
        lblT.setForeground(CIANO_VIVO);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblD = new JLabel("<html><body style='width: 380px'>" + descricao.replace("\n", "<br>") + "</body></html>");
        lblD.setForeground(TEXTO_CINZA);
        lblD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblD.setBorder(new EmptyBorder(4, 0, 0, 0));

        p.add(lblT);
        p.add(lblD);
        return p;
    }

    // ==================== TELA 3: EXTRATO E VERIFICAÇÃO POR PERÍODO ====================

    private JPanel criarTelaExtratoPeriodo() {
        JPanel tela = new JPanel(new BorderLayout(0, 14));
        tela.setOpaque(false);

        // Cabeçalho + 3 Cards de Resumo do Período Filtrado
        JPanel topoPeriodo = new JPanel(new BorderLayout(0, 12));
        topoPeriodo.setOpaque(false);

        JLabel titulo = new JLabel("Consulta de Extrato e Verificação por Período");
        titulo.setForeground(TEXTO_CLARO);
        titulo.setFont(FONTE_TITULO_PAGINA);

        JPanel cardsPeriodo = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsPeriodo.setOpaque(false);

        this.lblPeriodoReceitas = new JLabel("R$ 0,00");
        this.lblPeriodoDespesas = new JLabel("R$ 0,00");
        this.lblPeriodoSaldo = new JLabel("R$ 0,00");

        cardsPeriodo.add(criarCardDashboard("RECEITAS NO PERÍODO FILTRADO", this.lblPeriodoReceitas, new JLabel("Soma das entradas exibidas"), null, VERDE_RECEITA));
        cardsPeriodo.add(criarCardDashboard("DESPESAS NO PERÍODO FILTRADO", this.lblPeriodoDespesas, new JLabel("Soma das saídas exibidas"), null, CORAL_DESPESA));
        cardsPeriodo.add(criarCardDashboard("SALDO DO PERÍODO FILTRADO", this.lblPeriodoSaldo, new JLabel("Resultado polimórfico do filtro"), null, CIANO_VIVO));

        topoPeriodo.add(titulo, BorderLayout.NORTH);
        topoPeriodo.add(cardsPeriodo, BorderLayout.CENTER);

        // Painel de Filtros e Tabela Completa
        JPanel cardTabela = new JPanel(new BorderLayout(0, 12));
        cardTabela.setBackground(CARD_CINZA);
        cardTabela.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel barraSuperiorFiltros = new JPanel(new BorderLayout(8, 8));
        barraSuperiorFiltros.setOpaque(false);

        JPanel linhaAtalhos = new JPanel(new BorderLayout());
        linhaAtalhos.setOpaque(false);

        this.lblResumoPeriodoTexto = new JLabel("Exibindo todos os lançamentos");
        this.lblResumoPeriodoTexto.setForeground(CIANO_VIVO);
        this.lblResumoPeriodoTexto.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel botoesRapidos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botoesRapidos.setOpaque(false);

        JButton btnMesAtual = criarBotaoSecundario("Mês Atual");
        btnMesAtual.addActionListener(e -> {
            LocalDate hoje = LocalDate.now();
            this.txtFiltroInicio.setText(hoje.withDayOfMonth(1).toString());
            this.txtFiltroFim.setText(hoje.withDayOfMonth(hoje.lengthOfMonth()).toString());
            acaoAplicarFiltros();
        });

        JButton btnUltimos30 = criarBotaoSecundario("Últimos 30 Dias");
        btnUltimos30.addActionListener(e -> {
            LocalDate hoje = LocalDate.now();
            this.txtFiltroInicio.setText(hoje.minusDays(30).toString());
            this.txtFiltroFim.setText(hoje.toString());
            acaoAplicarFiltros();
        });

        JButton btnAnoAtual = criarBotaoSecundario("Ano Atual");
        btnAnoAtual.addActionListener(e -> {
            LocalDate hoje = LocalDate.now();
            this.txtFiltroInicio.setText(LocalDate.of(hoje.getYear(), 1, 1).toString());
            this.txtFiltroFim.setText(LocalDate.of(hoje.getYear(), 12, 31).toString());
            acaoAplicarFiltros();
        });

        botoesRapidos.add(btnMesAtual);
        botoesRapidos.add(btnUltimos30);
        botoesRapidos.add(btnAnoAtual);

        linhaAtalhos.add(this.lblResumoPeriodoTexto, BorderLayout.WEST);
        linhaAtalhos.add(botoesRapidos, BorderLayout.EAST);

        JPanel controlesFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        controlesFiltro.setOpaque(false);

        this.txtBuscaDescricao = criarCampoTexto("");
        this.txtBuscaDescricao.setPreferredSize(new Dimension(170, 32));

        this.cmbFiltroTipo = new JComboBox<>(new String[]{"TODOS", "RECEITA", "DESPESA"});
        estilizarCombo(this.cmbFiltroTipo);
        this.cmbFiltroTipo.setPreferredSize(new Dimension(120, 32));

        this.txtFiltroInicio = criarCampoTexto("");
        this.txtFiltroInicio.setPreferredSize(new Dimension(110, 32));

        this.txtFiltroFim = criarCampoTexto("");
        this.txtFiltroFim.setPreferredSize(new Dimension(110, 32));

        JButton btnFiltrar = criarBotaoCiano("Aplicar Filtro");
        btnFiltrar.addActionListener(e -> acaoAplicarFiltros());

        JButton btnLimpar = criarBotaoSecundario("Mostrar Todos");
        btnLimpar.addActionListener(e -> {
            this.txtBuscaDescricao.setText("");
            this.cmbFiltroTipo.setSelectedIndex(0);
            this.txtFiltroInicio.setText("");
            this.txtFiltroFim.setText("");
            atualizarTodasAsTelas(null, null);
        });

        controlesFiltro.add(criarRotuloInline("Descrição:"));
        controlesFiltro.add(this.txtBuscaDescricao);
        controlesFiltro.add(criarRotuloInline("Tipo:"));
        controlesFiltro.add(this.cmbFiltroTipo);
        controlesFiltro.add(criarRotuloInline("Data Início:"));
        controlesFiltro.add(this.txtFiltroInicio);
        controlesFiltro.add(criarRotuloInline("Data Fim:"));
        controlesFiltro.add(this.txtFiltroFim);
        controlesFiltro.add(btnFiltrar);
        controlesFiltro.add(btnLimpar);

        barraSuperiorFiltros.add(linhaAtalhos, BorderLayout.NORTH);
        barraSuperiorFiltros.add(controlesFiltro, BorderLayout.SOUTH);

        String[] colunas = {"ID", "Data", "Tipo", "Resumo (Sobrescrita getResumo)", "Categoria", "Impacto (calcularImpacto)"};
        this.modeloTabelaPeriodo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tabelaPeriodo = criarTabelaEstilizada(this.modeloTabelaPeriodo, 2, 5);
        this.tabelaPeriodo.getColumnModel().getColumn(0).setPreferredWidth(45);
        this.tabelaPeriodo.getColumnModel().getColumn(1).setPreferredWidth(95);
        this.tabelaPeriodo.getColumnModel().getColumn(2).setPreferredWidth(90);
        this.tabelaPeriodo.getColumnModel().getColumn(3).setPreferredWidth(300);
        this.tabelaPeriodo.getColumnModel().getColumn(4).setPreferredWidth(130);
        this.tabelaPeriodo.getColumnModel().getColumn(5).setPreferredWidth(140);

        this.tabelaPeriodo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabelaPeriodo.getSelectedRow() >= 0) {
                    acaoCarregarLancamentoParaEdicao();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(this.tabelaPeriodo);
        scroll.getViewport().setBackground(BG_CAMPO);
        scroll.setBorder(new LineBorder(BORDA_CINZA, 1));

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rodape.setOpaque(false);

        JButton btnDuplicar = criarBotaoSecundario("Duplicar Selecionado");
        btnDuplicar.addActionListener(e -> acaoDuplicarLancamentoSelecionado());

        JButton btnEditar = criarBotaoCiano("Editar Lançamento Selecionado");
        btnEditar.addActionListener(e -> acaoCarregarLancamentoParaEdicao());

        JButton btnExcluir = criarBotaoPerigo("Excluir Lançamento Selecionado");
        btnExcluir.addActionListener(e -> acaoExcluirLancamentoSelecionado());

        rodape.add(btnDuplicar);
        rodape.add(btnEditar);
        rodape.add(btnExcluir);

        cardTabela.add(barraSuperiorFiltros, BorderLayout.NORTH);
        cardTabela.add(scroll, BorderLayout.CENTER);
        cardTabela.add(rodape, BorderLayout.SOUTH);

        tela.add(topoPeriodo, BorderLayout.NORTH);
        tela.add(cardTabela, BorderLayout.CENTER);
        return tela;
    }

    // ==================== TELA 4: CATEGORIAS E SALDOS ====================

    private JPanel criarTelaCategorias() {
        JPanel tela = new JPanel(new BorderLayout(0, 16));
        tela.setOpaque(false);

        JLabel titulo = new JLabel("Gerenciamento de Categorias e Saldos Polimórficos");
        titulo.setForeground(TEXTO_CLARO);
        titulo.setFont(FONTE_TITULO_PAGINA);
        tela.add(titulo, BorderLayout.NORTH);

        JPanel miolo = new JPanel(new BorderLayout(18, 0));
        miolo.setOpaque(false);

        // Card esquerdo: Criar nova categoria
        JPanel cardCriar = new JPanel();
        cardCriar.setPreferredSize(new Dimension(360, 400));
        cardCriar.setLayout(new BoxLayout(cardCriar, BoxLayout.Y_AXIS));
        cardCriar.setBackground(CARD_CINZA);
        cardCriar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(20, 22, 20, 22)
        ));

        JLabel subCriar = new JLabel("Nova Categoria");
        subCriar.setForeground(CIANO_VIVO);
        subCriar.setFont(FONTE_SECAO);
        cardCriar.add(criarBlocoTituloComBarraCiano(subCriar));
        cardCriar.add(Box.createVerticalStrut(16));

        cardCriar.add(criarRotulo("NOME DA CATEGORIA"));
        this.txtNovaCategoria = criarCampoTexto("");
        cardCriar.add(this.txtNovaCategoria);
        cardCriar.add(Box.createVerticalStrut(14));

        JButton btnAdicionar = criarBotaoCiano("Cadastrar Categoria");
        btnAdicionar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAdicionar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnAdicionar.addActionListener(e -> acaoAdicionarCategoria());
        cardCriar.add(btnAdicionar);
        cardCriar.add(Box.createVerticalStrut(22));

        cardCriar.add(criarItemExplicacao(
            "Proteção de Integridade",
            "Uma categoria só pode ser removida se não possuir lançamentos associados a ela, preservando a consistência dos objetos."
        ));

        // Card direito: Tabela de categorias com saldo individual
        JPanel cardLista = criarCardPadrao("Categorias Cadastradas e Saldo Individual (Categoria.calcularSaldo)");
        String[] colCat = {"ID", "Nome da Categoria", "Qtd. Lançamentos", "Saldo Polimórfico (R$)"};
        this.modeloTabelaCategorias = new DefaultTableModel(colCat, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.tabelaCategorias = criarTabelaEstilizada(this.modeloTabelaCategorias, -1, 3);
        JScrollPane scrollCat = new JScrollPane(this.tabelaCategorias);
        scrollCat.getViewport().setBackground(BG_CAMPO);
        scrollCat.setBorder(new LineBorder(BORDA_CINZA, 1));

        JPanel rodapeCat = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        rodapeCat.setOpaque(false);
        JButton btnRemoverCat = criarBotaoPerigo("Remover Categoria Selecionada");
        btnRemoverCat.addActionListener(e -> acaoRemoverCategoria());
        rodapeCat.add(btnRemoverCat);

        cardLista.add(scrollCat, BorderLayout.CENTER);
        cardLista.add(rodapeCat, BorderLayout.SOUTH);

        miolo.add(cardCriar, BorderLayout.WEST);
        miolo.add(cardLista, BorderLayout.CENTER);

        tela.add(miolo, BorderLayout.CENTER);
        return tela;
    }

    // ==================== TELA 5: ARQUIVOS CSV/TXT E CÂMBIO API ====================

    private JPanel criarTelaArquivosEApi() {
        JPanel tela = new JPanel(new BorderLayout(0, 16));
        tela.setOpaque(false);

        JLabel titulo = new JLabel("Importação/Exportação de Arquivos CSV e Integração com API");
        titulo.setForeground(TEXTO_CLARO);
        titulo.setFont(FONTE_TITULO_PAGINA);
        tela.add(titulo, BorderLayout.NORTH);

        JPanel grade = new JPanel(new GridLayout(1, 2, 18, 0));
        grade.setOpaque(false);

        // Card Esquerdo: Importar e Exportar CSV/TXT
        JPanel cardCsv = new JPanel();
        cardCsv.setLayout(new BoxLayout(cardCsv, BoxLayout.Y_AXIS));
        cardCsv.setBackground(CARD_CINZA);
        cardCsv.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(20, 22, 20, 22)
        ));

        JLabel subCsv = new JLabel("Leitura e Escrita de Arquivos CSV / TXT");
        subCsv.setForeground(CIANO_VIVO);
        subCsv.setFont(FONTE_SECAO);
        cardCsv.add(criarBlocoTituloComBarraCiano(subCsv));
        cardCsv.add(Box.createVerticalStrut(14));

        JPanel botoesArquivoDisco = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botoesArquivoDisco.setOpaque(false);
        botoesArquivoDisco.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAbrirArquivo = criarBotaoCiano("Selecionar Arquivo CSV/TXT do Computador");
        btnAbrirArquivo.addActionListener(e -> acaoImportarArquivoDisco());

        JButton btnExportarArquivo = criarBotaoSecundario("Exportar Histórico para CSV");
        btnExportarArquivo.addActionListener(e -> acaoExportarArquivoDisco());

        botoesArquivoDisco.add(btnAbrirArquivo);
        botoesArquivoDisco.add(btnExportarArquivo);
        cardCsv.add(botoesArquivoDisco);
        cardCsv.add(Box.createVerticalStrut(18));

        cardCsv.add(criarRotulo("OU COLE LINHAS NO FORMATO: TIPO;DESCRICAO;VALOR;DATA;CATEGORIA"));
        this.txtAreaCsv = new JTextArea(10, 30);
        this.txtAreaCsv.setBackground(BG_CAMPO);
        this.txtAreaCsv.setForeground(TEXTO_CLARO);
        this.txtAreaCsv.setCaretColor(CIANO_VIVO);
        this.txtAreaCsv.setFont(new Font("Consolas", Font.PLAIN, 13));
        this.txtAreaCsv.setText(
            "RECEITA;Freelance Java;950.00;" + LocalDate.now() + ";Trabalho\n" +
            "DESPESA;Livros POO;140.00;" + LocalDate.now() + ";Trabalho"
        );

        JScrollPane scrollCsv = new JScrollPane(this.txtAreaCsv);
        scrollCsv.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollCsv.setBorder(new LineBorder(BORDA_CINZA, 1));
        cardCsv.add(scrollCsv);
        cardCsv.add(Box.createVerticalStrut(12));

        JButton btnImportarTexto = criarBotaoCiano("Importar Linhas Coladas Acima");
        btnImportarTexto.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnImportarTexto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnImportarTexto.addActionListener(e -> acaoImportarTextoCsv());
        cardCsv.add(btnImportarTexto);

        // Card Direito: Câmbio via API Externa
        JPanel cardApi = new JPanel();
        cardApi.setLayout(new BoxLayout(cardApi, BoxLayout.Y_AXIS));
        cardApi.setBackground(CARD_CINZA);
        cardApi.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(20, 22, 20, 22)
        ));

        JLabel subApi = new JLabel("Integração com Serviço Externo via API (AwesomeAPI)");
        subApi.setForeground(CIANO_VIVO);
        subApi.setFont(FONTE_SECAO);
        cardApi.add(criarBlocoTituloComBarraCiano(subApi));
        cardApi.add(Box.createVerticalStrut(16));

        this.lblStatusApiDetalhe = new JLabel(
            "Cotação atual: US$ 1 = R$ " + this.cotacaoMoedaService.getUltimaCotacaoDolar() +
            "  |  € 1 = R$ " + this.cotacaoMoedaService.getUltimaCotacaoEuro()
        );
        this.lblStatusApiDetalhe.setForeground(TEXTO_CLARO);
        this.lblStatusApiDetalhe.setFont(new Font("Segoe UI", Font.BOLD, 15));
        this.lblStatusApiDetalhe.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardApi.add(this.lblStatusApiDetalhe);
        cardApi.add(Box.createVerticalStrut(12));

        JButton btnConsultarApi = criarBotaoCiano("Consultar Cotação em Tempo Real (AwesomeAPI)");
        btnConsultarApi.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnConsultarApi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnConsultarApi.addActionListener(e -> acaoConsultarApiCotacao());
        cardApi.add(btnConsultarApi);
        cardApi.add(Box.createVerticalStrut(20));

        cardApi.add(criarItemExplicacao(
            "Endpoint Consumido (HttpClient)",
            "GET https://economia.awesomeapi.com.br/json/last/USD-BRL,EUR-BRL\nA classe CotacaoMoedaService realiza a requisição HTTP e converte automaticamente o saldo do usuário."
        ));

        grade.add(cardCsv);
        grade.add(cardApi);

        tela.add(grade, BorderLayout.CENTER);
        return tela;
    }

    // ==================== AÇÕES E EVENTOS ====================

    private void acaoSalvarLancamento() {
        try {
            String tipo = String.valueOf(this.cmbTipo.getSelectedItem());
            String descricao = this.txtDescricao.getText().trim();
            String valorTexto = this.txtValor.getText().trim().replace(",", ".");
            String dataTexto = this.txtData.getText().trim();
            Categoria categoria = (Categoria) this.cmbCategoria.getSelectedItem();

            if (valorTexto.isEmpty()) {
                throw new FinanceHubException("VALOR_VAZIO", "Informe o valor em reais.");
            }

            BigDecimal valor = new BigDecimal(valorTexto).setScale(2, RoundingMode.HALF_UP);
            LocalDate data = LocalDate.parse(dataTexto);

            if (this.idEmEdicao == null) {
                Lancamento novo = "RECEITA".equalsIgnoreCase(tipo)
                    ? new Receita(null, descricao, valor, data, categoria, this.usuarioAtual.getId())
                    : new Despesa(null, descricao, valor, data, categoria, this.usuarioAtual.getId());
                registrarNovoLancamentoInterno(novo);
            } else {
                this.gerenciadorLancamentos.atualizar(this.idEmEdicao, tipo, descricao, valor, data, categoria);
                sincronizarLancamentosNoUsuario();
            }

            salvarAlteracoes();
            limparFormularioLancamento();
            atualizarTodasAsTelas(null, null);
            navegarParaTela(TELA_PERIODO, this.btnMenuPeriodo);
        } catch (FinanceHubException e) {
            mostrarErro("Validação (" + e.getCodigo() + ")", e.getMessage());
        } catch (Exception e) {
            mostrarErro("Dados Inválidos", "Verifique o valor numérico e a data (AAAA-MM-DD).\nDetalhe: " + e.getMessage());
        }
    }

    private void acaoCarregarLancamentoParaEdicao() {
        int linha = this.tabelaPeriodo.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Seleção Necessária", "Selecione uma linha na tabela para editar.");
            return;
        }
        try {
            long id = Long.parseLong(String.valueOf(this.modeloTabelaPeriodo.getValueAt(linha, 0)));
            Lancamento l = this.gerenciadorLancamentos.buscarPorId(id);

            this.idEmEdicao = l.getId();
            this.lblTituloFormulario.setText("Editando Lançamento #" + l.getId());
            this.cmbTipo.setSelectedItem(l.getTipo());
            this.txtDescricao.setText(l.getDescricao());
            this.txtValor.setText(l.getValor().toPlainString());
            this.txtData.setText(l.getData().toString());
            if (l.getCategoria() != null) {
                this.cmbCategoria.setSelectedItem(l.getCategoria());
            }
            navegarParaTela(TELA_ADICIONAR, this.btnMenuAdicionar);
        } catch (FinanceHubException e) {
            mostrarErro("Erro ao Carregar", e.getMessage());
        }
    }

    private void acaoDuplicarLancamentoSelecionado() {
        int linha = this.tabelaPeriodo.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Seleção Necessária", "Selecione um lançamento na tabela para duplicar.");
            return;
        }
        try {
            long id = Long.parseLong(String.valueOf(this.modeloTabelaPeriodo.getValueAt(linha, 0)));
            Lancamento original = this.gerenciadorLancamentos.buscarPorId(id);

            Lancamento copia = original.isReceita()
                ? new Receita(null, original.getDescricao() + " (Cópia)", original.getValor(), LocalDate.now(), original.getCategoria(), this.usuarioAtual.getId())
                : new Despesa(null, original.getDescricao() + " (Cópia)", original.getValor(), LocalDate.now(), original.getCategoria(), this.usuarioAtual.getId());

            registrarNovoLancamentoInterno(copia);
            salvarAlteracoes();
            atualizarTodasAsTelas(null, null);
        } catch (FinanceHubException e) {
            mostrarErro("Erro ao Duplicar", e.getMessage());
        }
    }

    private void limparFormularioLancamento() {
        this.idEmEdicao = null;
        if (this.lblTituloFormulario != null) {
            this.lblTituloFormulario.setText("Adicionar Novo Lançamento Financeiro");
        }
        if (this.txtDescricao != null) {
            this.txtDescricao.setText("");
            this.txtValor.setText("");
            this.txtData.setText(LocalDate.now().toString());
        }
    }

    private void acaoAdicionarCategoria() {
        try {
            String nome = this.txtNovaCategoria.getText().trim();
            long novoId = gerarProximoIdCategoria();
            Categoria nova = new Categoria(novoId, nome);
            this.gerenciadorCategorias.cadastrar(nova);
            salvarAlteracoes();
            this.txtNovaCategoria.setText("");
            atualizarTodasAsTelas(null, null);
        } catch (Exception e) {
            mostrarErro("Erro na Categoria", e.getMessage());
        }
    }

    private void acaoRemoverCategoria() {
        int linha = this.tabelaCategorias.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Seleção Necessária", "Selecione uma categoria na tabela para remover.");
            return;
        }
        Categoria selecionada = this.gerenciadorCategorias.listar().get(linha);
        if (!selecionada.getLancamentos().isEmpty()) {
            mostrarErro(
                "Categoria em Uso",
                "A categoria '" + selecionada.getNome() + "' possui " + selecionada.getLancamentos().size() +
                " lançamento(s) vinculado(s) e não pode ser removida."
            );
            return;
        }
        this.gerenciadorCategorias.removerPorId(selecionada.getId());
        salvarAlteracoes();
        atualizarTodasAsTelas(null, null);
    }

    private void acaoExcluirLancamentoSelecionado() {
        int linha = this.tabelaPeriodo.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Seleção Necessária", "Selecione um lançamento na tabela para excluir.");
            return;
        }
        long id = Long.parseLong(String.valueOf(this.modeloTabelaPeriodo.getValueAt(linha, 0)));
        if (this.gerenciadorLancamentos.removerPorId(id)) {
            sincronizarLancamentosNoUsuario();
            salvarAlteracoes();
            atualizarTodasAsTelas(null, null);
        }
    }

    private void acaoImportarTextoCsv() {
        try {
            String conteudo = this.txtAreaCsv.getText().trim();
            ArrayList<Lancamento> importados = this.importadorArquivoService.importarDeConteudo(
                conteudo,
                this.gerenciadorLancamentos,
                this.gerenciadorCategorias
            );
            sincronizarLancamentosNoUsuario();
            salvarAlteracoes();
            atualizarTodasAsTelas(null, null);
            JOptionPane.showMessageDialog(this, importados.size() + " lançamento(s) importado(s) com sucesso!");
            navegarParaTela(TELA_PERIODO, this.btnMenuPeriodo);
        } catch (FinanceHubException e) {
            mostrarErro("Erro na Importação (" + e.getCodigo() + ")", e.getMessage());
        }
    }

    private void acaoImportarArquivoDisco() {
        JFileChooser chooser = new JFileChooser(Path.of(".").toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos CSV ou TXT", "csv", "txt"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path arquivo = chooser.getSelectedFile().toPath();
                ArrayList<Lancamento> importados = this.importadorArquivoService.importarDeArquivo(
                    arquivo,
                    this.gerenciadorLancamentos,
                    this.gerenciadorCategorias
                );
                sincronizarLancamentosNoUsuario();
                salvarAlteracoes();
                atualizarTodasAsTelas(null, null);
                JOptionPane.showMessageDialog(this, importados.size() + " lançamento(s) importado(s) de " + arquivo.getFileName());
                navegarParaTela(TELA_PERIODO, this.btnMenuPeriodo);
            } catch (FinanceHubException e) {
                mostrarErro("Erro na Leitura do Arquivo", e.getMessage());
            }
        }
    }

    private void acaoExportarArquivoDisco() {
        JFileChooser chooser = new JFileChooser(Path.of(".").toFile());
        chooser.setSelectedFile(Path.of("financehub-lancamentos.csv").toFile());
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path destino = chooser.getSelectedFile().toPath();
                this.importadorArquivoService.exportarParaArquivo(destino, this.gerenciadorLancamentos.listar());
                JOptionPane.showMessageDialog(this, "Exportado com sucesso para:\n" + destino.toAbsolutePath());
            } catch (FinanceHubException e) {
                mostrarErro("Erro na Exportação", e.getMessage());
            }
        }
    }

    private void acaoConsultarApiCotacao() {
        BigDecimal usd = this.cotacaoMoedaService.buscarCotacaoDolarAtual();
        BigDecimal eur = this.cotacaoMoedaService.getUltimaCotacaoEuro();
        String origem = this.cotacaoMoedaService.isObtidaDaApiOnline() ? "AwesomeAPI Online" : "Valor Padrão Local";
        this.lblStatusApiDetalhe.setText("Cotação atual (" + origem + "): US$ 1 = R$ " + usd + "  |  € 1 = R$ " + eur);
        atualizarTodasAsTelas(null, null);
        JOptionPane.showMessageDialog(this, "Cotações atualizadas!\nDólar: R$ " + usd + "\nEuro: R$ " + eur);
    }

    private void acaoEditarUsuarioEMeta() {
        String novoNome = JOptionPane.showInputDialog(this, "Nome do Titular:", this.usuarioAtual.getNome());
        if (novoNome != null && !novoNome.isBlank()) {
            this.usuarioAtual.setNome(novoNome);
        }
        String novoEmail = JOptionPane.showInputDialog(this, "E-mail do Titular:", this.usuarioAtual.getEmail());
        if (novoEmail != null && !novoEmail.isBlank()) {
            this.usuarioAtual.setEmail(novoEmail);
        }
        String novaMeta = JOptionPane.showInputDialog(
            this,
            "Meta / Limite de Despesas Mensal (R$):",
            this.usuarioAtual.getMetaDespesaMensal().toPlainString()
        );
        if (novaMeta != null && !novaMeta.isBlank()) {
            try {
                this.usuarioAtual.setMetaDespesaMensal(new BigDecimal(novaMeta.trim().replace(",", ".")).setScale(2, RoundingMode.HALF_UP));
            } catch (Exception e) {
                mostrarErro("Meta Inválida", "Informe um valor positivo.");
            }
        }
        this.lblUsuarioSidebar.setText(this.usuarioAtual.getNome());
        this.lblEmailSidebar.setText(this.usuarioAtual.getEmail());
        atualizarTodasAsTelas(null, null);
    }

    private void acaoAplicarFiltros() {
        try {
            String ini = this.txtFiltroInicio.getText().trim();
            String fim = this.txtFiltroFim.getText().trim();
            LocalDate dataInicio = ini.isEmpty() ? null : LocalDate.parse(ini);
            LocalDate dataFim = fim.isEmpty() ? null : LocalDate.parse(fim);
            atualizarTodasAsTelas(dataInicio, dataFim);
        } catch (Exception e) {
            mostrarErro("Filtro Inválido", "Use o formato AAAA-MM-DD para as datas.");
        }
    }

    // ==================== ATUALIZAÇÃO SINCRONIZADA DE TODAS AS TELAS ====================

    private void atualizarTodasAsTelas(LocalDate inicio, LocalDate fim) {
        try {
            // 1. Atualizar Combo e Tabela de Categorias (Tela 2 e Tela 4)
            Categoria selecionadaAntes = (Categoria) this.cmbCategoria.getSelectedItem();
            this.modeloComboCategorias.removeAllElements();
            this.modeloTabelaCategorias.setRowCount(0);

            for (Categoria c : this.gerenciadorCategorias.listar()) {
                this.modeloComboCategorias.addElement(c);
                this.modeloTabelaCategorias.addRow(new Object[]{
                    c.getId(),
                    c.getNome(),
                    c.getLancamentos().size() + " lançamento(s)",
                    this.moedaBRL.format(c.calcularSaldo())
                });
            }
            if (selecionadaAntes != null) {
                this.cmbCategoria.setSelectedItem(selecionadaAntes);
            }

            // 2. Relatório Global para a Tela 1 (Início)
            List<Lancamento> todos = this.gerenciadorLancamentos.listar();
            Relatorio relGlobal = this.gerenciadorLancamentos.gerarRelatorio();

            this.lblCardReceitas.setText(this.moedaBRL.format(relGlobal.getTotalReceitas()));
            this.lblSubReceitas.setText(relGlobal.getQuantidadeReceitas() + " entrada(s)");

            this.lblCardDespesas.setText(this.moedaBRL.format(relGlobal.getTotalDespesas()));
            this.lblSubDespesas.setText(relGlobal.getQuantidadeDespesas() + " saída(s)");

            this.lblCardSaldo.setText(this.moedaBRL.format(relGlobal.getSaldoTotal()));

            int poupanca = 0;
            if (relGlobal.getTotalReceitas().compareTo(BigDecimal.ZERO) > 0 && relGlobal.getSaldoTotal().compareTo(BigDecimal.ZERO) > 0) {
                poupanca = relGlobal.getSaldoTotal().multiply(new BigDecimal("100")).divide(relGlobal.getTotalReceitas(), 0, RoundingMode.HALF_UP).intValue();
            }
            this.lblSubPoupanca.setText("Poupança: " + Math.min(100, Math.max(0, poupanca)) + "% das receitas");
            this.barraPoupanca.setValue(Math.min(100, Math.max(0, poupanca)));

            BigDecimal meta = this.usuarioAtual.getMetaDespesaMensal();
            this.lblCardMeta.setText(this.moedaBRL.format(meta));
            int percMeta = meta.compareTo(BigDecimal.ZERO) > 0
                ? relGlobal.getTotalDespesas().multiply(new BigDecimal("100")).divide(meta, 0, RoundingMode.HALF_UP).intValue()
                : 0;
            this.lblSubMeta.setText(percMeta + "% da meta utilizada");
            this.barraMeta.setValue(Math.min(100, percMeta));
            this.barraMeta.setForeground(percMeta > 100 ? CORAL_DESPESA : (percMeta >= 80 ? AMBAR_ALERTA : VERDE_RECEITA));

            BigDecimal usd = this.cotacaoMoedaService.converterReaisParaDolar(relGlobal.getSaldoTotal());
            BigDecimal eur = this.cotacaoMoedaService.converterReaisParaEuro(relGlobal.getSaldoTotal());
            this.lblCardCambio.setText(this.moedaUSD.format(usd));
            this.lblSubCambio.setText(String.format("Euro: € %.2f", eur));

            BigDecimal somaMov = relGlobal.getTotalReceitas().add(relGlobal.getTotalDespesas());
            if (somaMov.compareTo(BigDecimal.ZERO) > 0) {
                int percRec = relGlobal.getTotalReceitas().multiply(new BigDecimal("100")).divide(somaMov, 0, RoundingMode.HALF_UP).intValue();
                this.barraReceitas.setValue(percRec);
                this.barraDespesas.setValue(100 - percRec);
            } else {
                this.barraReceitas.setValue(0);
                this.barraDespesas.setValue(0);
            }

            // Popular Últimos Lançamentos e Insights na Tela 1 (Início)
            this.modeloTabelaRecentes.setRowCount(0);
            int limiteRecentes = Math.min(8, todos.size());
            for (int i = todos.size() - 1; i >= todos.size() - limiteRecentes; i--) {
                Lancamento l = todos.get(i);
                String catNome = l.getCategoria() != null ? l.getCategoria().getNome() : "Geral";
                this.modeloTabelaRecentes.addRow(new Object[]{
                    l.getData().toString(),
                    l.getTipo(),
                    l.getResumo(),
                    catNome,
                    this.moedaBRL.format(l.calcularImpacto())
                });
            }
            atualizarGraficoEInsightsInicio(todos, relGlobal);

            // 3. Atualizar Tela 3 (Extrato e Período Filtrado)
            List<Lancamento> basePeriodo = (inicio != null && fim != null)
                ? this.gerenciadorLancamentos.listarPorPeriodo(inicio, fim)
                : todos;

            String termo = this.txtBuscaDescricao != null ? this.txtBuscaDescricao.getText().trim().toLowerCase() : "";
            String tipoFiltro = this.cmbFiltroTipo != null ? String.valueOf(this.cmbFiltroTipo.getSelectedItem()) : "TODOS";

            List<Lancamento> filtrados = new ArrayList<>();
            for (Lancamento l : basePeriodo) {
                boolean okTexto = termo.isEmpty() || l.getDescricao().toLowerCase().contains(termo);
                boolean okTipo = "TODOS".equalsIgnoreCase(tipoFiltro) || l.getTipo().equalsIgnoreCase(tipoFiltro);
                if (okTexto && okTipo) {
                    filtrados.add(l);
                }
            }

            Relatorio relPeriodo = GerenciadorLancamentos.calcularRelatorio(filtrados);
            this.lblPeriodoReceitas.setText(this.moedaBRL.format(relPeriodo.getTotalReceitas()));
            this.lblPeriodoDespesas.setText(this.moedaBRL.format(relPeriodo.getTotalDespesas()));
            this.lblPeriodoSaldo.setText(this.moedaBRL.format(relPeriodo.getSaldoTotal()));
            this.lblResumoPeriodoTexto.setText(relPeriodo.gerarResumoTexto());

            this.modeloTabelaPeriodo.setRowCount(0);
            for (Lancamento l : filtrados) {
                String catNome = l.getCategoria() != null ? l.getCategoria().getNome() : "Geral";
                this.modeloTabelaPeriodo.addRow(new Object[]{
                    l.getId(),
                    l.getData().toString(),
                    l.getTipo(),
                    l.getResumo(),
                    catNome,
                    this.moedaBRL.format(l.calcularImpacto())
                });
            }
        } catch (FinanceHubException e) {
            mostrarErro("Erro no Filtro de Período", e.getMessage());
        }
    }

    private void atualizarGraficoEInsightsInicio(List<Lancamento> lista, Relatorio relatorio) {
        this.painelBarrasCategoriasInicio.removeAll();
        BigDecimal maiorModulo = BigDecimal.ONE;
        for (Categoria c : this.gerenciadorCategorias.listar()) {
            BigDecimal abs = c.calcularSaldo().abs();
            if (abs.compareTo(maiorModulo) > 0) {
                maiorModulo = abs;
            }
        }

        for (Categoria c : this.gerenciadorCategorias.listar()) {
            BigDecimal saldoCat = c.calcularSaldo();
            int perc = saldoCat.abs().multiply(new BigDecimal("100")).divide(maiorModulo, 0, RoundingMode.HALF_UP).intValue();

            JPanel linha = new JPanel(new BorderLayout(10, 0));
            linha.setOpaque(false);
            linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            linha.setBorder(new EmptyBorder(3, 0, 4, 4));

            JLabel lblNome = new JLabel(c.getNome() + " (" + c.getLancamentos().size() + ")");
            lblNome.setPreferredSize(new Dimension(130, 22));
            lblNome.setForeground(TEXTO_CLARO);
            lblNome.setFont(new Font("Segoe UI", Font.BOLD, 12));

            JProgressBar barra = new JProgressBar(0, 100);
            barra.setValue(perc);
            barra.setBackground(BG_CAMPO);
            barra.setForeground(saldoCat.compareTo(BigDecimal.ZERO) >= 0 ? VERDE_RECEITA : CORAL_DESPESA);
            barra.setBorderPainted(false);

            JLabel lblSaldo = new JLabel(this.moedaBRL.format(saldoCat), SwingConstants.RIGHT);
            lblSaldo.setPreferredSize(new Dimension(115, 22));
            lblSaldo.setForeground(saldoCat.compareTo(BigDecimal.ZERO) >= 0 ? VERDE_RECEITA : CORAL_DESPESA);
            lblSaldo.setFont(new Font("Segoe UI", Font.BOLD, 12));

            linha.add(lblNome, BorderLayout.WEST);
            linha.add(barra, BorderLayout.CENTER);
            linha.add(lblSaldo, BorderLayout.EAST);
            this.painelBarrasCategoriasInicio.add(linha);
        }
        this.painelBarrasCategoriasInicio.revalidate();
        this.painelBarrasCategoriasInicio.repaint();

        Lancamento maiorRec = null;
        Lancamento maiorDesp = null;
        BigDecimal soma = BigDecimal.ZERO;

        for (Lancamento l : lista) {
            soma = soma.add(l.getValor());
            if (l.isReceita()) {
                if (maiorRec == null || l.getValor().compareTo(maiorRec.getValor()) > 0) {
                    maiorRec = l;
                }
            } else {
                if (maiorDesp == null || l.getValor().compareTo(maiorDesp.getValor()) > 0) {
                    maiorDesp = l;
                }
            }
        }

        this.lblInsightMaiorReceita.setText(maiorRec != null ? maiorRec.getDescricao() + " (" + this.moedaBRL.format(maiorRec.getValor()) + ")" : "Nenhuma");
        this.lblInsightMaiorDespesa.setText(maiorDesp != null ? maiorDesp.getDescricao() + " (" + this.moedaBRL.format(maiorDesp.getValor()) + ")" : "Nenhuma");
        this.lblInsightTicketMedio.setText(!lista.isEmpty() ? this.moedaBRL.format(soma.divide(BigDecimal.valueOf(lista.size()), 2, RoundingMode.HALF_UP)) : "R$ 0,00");

        if (relatorio.getSaldoTotal().compareTo(BigDecimal.ZERO) < 0) {
            this.lblInsightStatusOrcamento.setText("Déficit — Gastos maiores que ganhos");
            this.lblInsightStatusOrcamento.setForeground(CORAL_DESPESA);
        } else if (relatorio.getTotalDespesas().compareTo(this.usuarioAtual.getMetaDespesaMensal()) > 0) {
            this.lblInsightStatusOrcamento.setText("Alerta — Acima da meta mensal");
            this.lblInsightStatusOrcamento.setForeground(AMBAR_ALERTA);
        } else {
            this.lblInsightStatusOrcamento.setText("Saudável — Dentro da meta");
            this.lblInsightStatusOrcamento.setForeground(VERDE_RECEITA);
        }
    }

    private void sincronizarLancamentosNoUsuario() {
        this.usuarioAtual.limparLancamentos();
        for (Lancamento l : this.gerenciadorLancamentos.listar()) {
            this.usuarioAtual.adicionarLancamento(l);
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

    // ==================== FÁBRICAS VISUAIS AUXILIARES ====================

    private JPanel criarCardPadrao(String titulo) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_CINZA);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(CIANO_VIVO);
        lbl.setFont(FONTE_SECAO);
        card.add(criarBlocoTituloComBarraCiano(lbl), BorderLayout.NORTH);
        return card;
    }

    private JTable criarTabelaEstilizada(DefaultTableModel modelo, int colTipo, int colValorDireita) {
        JTable tabela = new JTable(modelo);
        tabela.setBackground(BG_CAMPO);
        tabela.setForeground(TEXTO_CLARO);
        tabela.setGridColor(BORDA_CINZA);
        tabela.setRowHeight(32);
        tabela.setFont(FONTE_TEXTO);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);

        JTableHeader header = tabela.getTableHeader();
        header.setBackground(CARD_CINZA_CLARO);
        header.setForeground(CIANO_VIVO);
        header.setFont(FONTE_ROTULO);
        header.setPreferredSize(new Dimension(100, 34));

        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String textoValor = String.valueOf(value);
                boolean negativo = textoValor.startsWith("-");
                String tipoLinha = colTipo >= 0 ? String.valueOf(table.getModel().getValueAt(row, colTipo)) : (negativo ? "DESPESA" : "RECEITA");

                if (isSelected) {
                    c.setBackground(CARD_CINZA_CLARO);
                    c.setForeground(CIANO_VIVO);
                } else {
                    c.setBackground(row % 2 == 0 ? BG_CAMPO : CARD_CINZA);
                    if (column == colTipo || column == colValorDireita) {
                        c.setForeground("RECEITA".equalsIgnoreCase(tipoLinha) && !negativo ? VERDE_RECEITA : CORAL_DESPESA);
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        c.setForeground(TEXTO_CLARO);
                        setFont(FONTE_TEXTO);
                    }
                }
                setHorizontalAlignment(column == colValorDireita ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderizador);
        }
        return tabela;
    }

    private JProgressBar criarBarraProgresso(Color cor) {
        JProgressBar barra = new JProgressBar(0, 100);
        barra.setValue(0);
        barra.setForeground(cor);
        barra.setBackground(BG_CAMPO);
        barra.setBorderPainted(false);
        barra.setPreferredSize(new Dimension(100, 6));
        barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        barra.setAlignmentX(Component.LEFT_ALIGNMENT);
        return barra;
    }

    private JPanel criarCardDashboard(String titulo, JLabel lblValor, JLabel lblDetalhe, JProgressBar barra, Color corDestaque) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_CINZA);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 1, 1, 1, corDestaque),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXTO_CINZA);
        lblTitulo.setFont(FONTE_ROTULO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblValor.setForeground(corDestaque);
        lblValor.setFont(FONTE_VALOR_CARD);
        lblValor.setBorder(new EmptyBorder(4, 0, 4, 0));
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDetalhe.setForeground(TEXTO_CINZA);
        lblDetalhe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetalhe.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lblTitulo);
        card.add(lblValor);
        card.add(lblDetalhe);
        if (barra != null) {
            card.add(Box.createVerticalStrut(8));
            card.add(barra);
        }
        return card;
    }

    private JPanel criarMiniCardInsight(String rotulo, JLabel lblValor, Color corValor) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(BG_CAMPO);
        box.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblTit = new JLabel(rotulo);
        lblTit.setForeground(TEXTO_CINZA);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 10));

        lblValor.setForeground(corValor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValor.setBorder(new EmptyBorder(4, 0, 0, 0));

        box.add(lblTit);
        box.add(lblValor);
        return box;
    }

    private JPanel criarBlocoTituloComBarraCiano(JLabel labelTitulo) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel barraVertical = new JPanel();
        barraVertical.setBackground(CIANO_VIVO);
        barraVertical.setPreferredSize(new Dimension(4, 18));

        labelTitulo.setBorder(new EmptyBorder(0, 8, 0, 0));
        p.add(barraVertical);
        p.add(labelTitulo);
        return p;
    }

    private JLabel criarRotulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEXTO_CINZA);
        lbl.setFont(FONTE_ROTULO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private JLabel criarRotuloInline(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEXTO_CINZA);
        lbl.setFont(FONTE_ROTULO);
        return lbl;
    }

    private JTextField criarCampoTexto(String inicial) {
        JTextField txt = new JTextField(inicial);
        txt.setBackground(BG_CAMPO);
        txt.setForeground(TEXTO_CLARO);
        txt.setCaretColor(CIANO_VIVO);
        txt.setFont(FONTE_TEXTO);
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return txt;
    }

    private <T> void estilizarCombo(JComboBox<T> combo) {
        combo.setBackground(BG_CAMPO);
        combo.setForeground(TEXTO_CLARO);
        combo.setFont(FONTE_TEXTO);
        combo.setFocusable(false);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setBorder(new LineBorder(BORDA_CINZA, 1));
    }

    private JButton criarBotaoCiano(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(CIANO_VIVO);
        btn.setForeground(BG_PRETO);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CIANO_ESCURO, 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    private JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(CARD_CINZA_CLARO);
        btn.setForeground(TEXTO_CLARO);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(7, 13, 7, 13)
        ));
        return btn;
    }

    private JButton criarBotaoPerigo(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(CARD_CINZA_CLARO);
        btn.setForeground(CORAL_DESPESA);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CORAL_DESPESA, 1, true),
            new EmptyBorder(7, 13, 7, 13)
        ));
        return btn;
    }

    private void mostrarErro(String titulo, String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, titulo, JOptionPane.ERROR_MESSAGE);
    }
}
