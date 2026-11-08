package br.com.financehub.ui;

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interface gráfica desktop do FinanceHub construída integralmente em Java Swing
 * (sem HTML, CSS ou JavaScript), estilizada nas cores preto, cinza e ciano.
 * Atende ao requisito 12 do PjBL (Interface gráfica).
 */
public class FinanceHubSwingFrame extends JFrame {

    // Paleta de cores: Preto, Cinza e Ciano
    private static final Color BG_BLACK = new Color(7, 9, 12);
    private static final Color BG_INPUT = new Color(13, 17, 23);
    private static final Color SURFACE_GRAY = new Color(18, 22, 28);
    private static final Color SURFACE_GRAY_2 = new Color(26, 32, 41);
    private static final Color BORDER_GRAY = new Color(42, 51, 64);
    private static final Color TEXT_PRIMARY = new Color(230, 237, 243);
    private static final Color TEXT_MUTED = new Color(139, 150, 165);
    private static final Color CYAN_PRIMARY = new Color(34, 211, 238);
    private static final Color CYAN_DARK = new Color(6, 182, 212);
    private static final Color RECEITA_TEAL = new Color(45, 212, 191);
    private static final Color DESPESA_CORAL = new Color(248, 113, 113);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private final GerenciadorCategorias gerenciadorCategorias;
    private final GerenciadorLancamentos gerenciadorLancamentos;
    private final ImportadorArquivoService importadorArquivoService;
    private final DatabaseConfig databaseConfig;
    private final JdbcCategoriaRepository jdbcCategoriaRepository;
    private final JdbcLancamentoRepository jdbcLancamentoRepository;
    private final PersistenciaArquivoRepository persistenciaArquivoRepository;
    private boolean mysqlAtivo;

    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    // Componentes visuais de resumo
    private JLabel lblStatusPersistencia;
    private JLabel lblValorReceitas;
    private JLabel lblQtdReceitas;
    private JLabel lblValorDespesas;
    private JLabel lblQtdDespesas;
    private JLabel lblValorSaldo;
    private JLabel lblQtdTotal;
    private JLabel lblResumoPolimorfico;

    // Formulário de lançamento
    private JComboBox<String> cmbTipo;
    private JTextField txtDescricao;
    private JTextField txtValor;
    private JTextField txtData;
    private JComboBox<CategoriaItem> cmbCategoria;
    private final DefaultComboBoxModel<CategoriaItem> modeloComboCategorias = new DefaultComboBoxModel<>();

    // Categorias
    private JTextField txtNovaCategoria;
    private final DefaultListModel<CategoriaItem> modeloListaCategorias = new DefaultListModel<>();
    private JList<CategoriaItem> lstCategorias;

    // Importação rápida CSV/TXT
    private JTextArea txtAreaCsv;

    // Filtro e tabela de histórico
    private JTextField txtFiltroInicio;
    private JTextField txtFiltroFim;
    private DefaultTableModel modeloTabela;
    private JTable tabelaLancamentos;

    public FinanceHubSwingFrame(Path arquivoPersistenciaLocal) {
        super("FinanceHub — Gerenciador Financeiro Pessoal (Java Swing)");
        configurarEstiloGlobal();

        this.gerenciadorCategorias = new GerenciadorCategorias();
        this.gerenciadorLancamentos = new GerenciadorLancamentos();
        this.importadorArquivoService = new ImportadorArquivoService();
        this.databaseConfig = new DatabaseConfig();
        this.jdbcCategoriaRepository = new JdbcCategoriaRepository(this.databaseConfig);
        this.jdbcLancamentoRepository = new JdbcLancamentoRepository(this.databaseConfig);
        this.persistenciaArquivoRepository = new PersistenciaArquivoRepository(arquivoPersistenciaLocal);

        inicializarDadosPersistidos();
        construirInterface();
        atualizarTelaCompleta(null, null);
    }

    private void configurarEstiloGlobal() {
        try {
            UIManager.put("OptionPane.background", SURFACE_GRAY);
            UIManager.put("Panel.background", SURFACE_GRAY);
            UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        } catch (Exception ignored) {
        }
    }

    private void inicializarDadosPersistidos() {
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

    private void sincronizarPersistenciaLocal() {
        try {
            this.persistenciaArquivoRepository.salvarObjetos(
                this.gerenciadorCategorias.listar(),
                this.gerenciadorLancamentos.listar()
            );
        } catch (Exception ignored) {
        }
    }

    private void construirInterface() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_BLACK);
        root.setBorder(new EmptyBorder(0, 0, 18, 0));
        setContentPane(root);

        root.add(criarBarraSuperior(), BorderLayout.NORTH);

        JPanel centroWrapper = new JPanel(new BorderLayout(0, 18));
        centroWrapper.setBackground(BG_BLACK);
        centroWrapper.setBorder(new EmptyBorder(0, 22, 0, 22));

        centroWrapper.add(criarPainelCardsResumo(), BorderLayout.NORTH);
        centroWrapper.add(criarPainelPrincipalDividido(), BorderLayout.CENTER);

        root.add(centroWrapper, BorderLayout.CENTER);
    }

    private JPanel criarBarraSuperior() {
        JPanel topbar = new JPanel(new BorderLayout(16, 0));
        topbar.setBackground(SURFACE_GRAY);
        topbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_GRAY),
            new EmptyBorder(14, 22, 14, 22)
        ));

        JPanel marcaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        marcaPanel.setOpaque(false);

        JLabel badge = new JLabel("FH", SwingConstants.CENTER);
        badge.setPreferredSize(new Dimension(42, 42));
        badge.setOpaque(true);
        badge.setBackground(CYAN_PRIMARY);
        badge.setForeground(BG_BLACK);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 16));
        badge.setBorder(new LineBorder(CYAN_DARK, 1, true));

        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.setOpaque(false);

        JLabel titulo = new JLabel("FinanceHub");
        titulo.setForeground(TEXT_PRIMARY);
        titulo.setFont(FONT_TITLE);

        JLabel subtitulo = new JLabel("Gerenciador Financeiro Pessoal — POO Java 21 + Swing");
        subtitulo.setForeground(TEXT_MUTED);
        subtitulo.setFont(FONT_SUBTITLE);

        titulos.add(titulo);
        titulos.add(subtitulo);

        marcaPanel.add(badge);
        marcaPanel.add(titulos);

        JPanel acoesDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        acoesDireita.setOpaque(false);

        String modo = this.mysqlAtivo ? "MYSQL_JDBC" : "ARQUIVO_LOCAL";
        this.lblStatusPersistencia = new JLabel("  ● Online (" + modo + ")  ");
        this.lblStatusPersistencia.setOpaque(true);
        this.lblStatusPersistencia.setBackground(SURFACE_GRAY_2);
        this.lblStatusPersistencia.setForeground(CYAN_PRIMARY);
        this.lblStatusPersistencia.setFont(new Font("Segoe UI", Font.BOLD, 12));
        this.lblStatusPersistencia.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CYAN_DARK, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));

        JButton btnImportarArquivo = criarBotaoSecundario("Importar Arquivo CSV/TXT");
        btnImportarArquivo.addActionListener(e -> acaoImportarDeArquivoDisco());

        JButton btnExportarArquivo = criarBotaoCiano("Exportar CSV");
        btnExportarArquivo.addActionListener(e -> acaoExportarParaArquivoDisco());

        acoesDireita.add(this.lblStatusPersistencia);
        acoesDireita.add(btnImportarArquivo);
        acoesDireita.add(btnExportarArquivo);

        topbar.add(marcaPanel, BorderLayout.WEST);
        topbar.add(acoesDireita, BorderLayout.EAST);
        return topbar;
    }

    private JPanel criarPainelCardsResumo() {
        JPanel grid = new JPanel(new java.awt.GridLayout(1, 3, 18, 0));
        grid.setOpaque(false);

        this.lblValorReceitas = new JLabel("R$ 0,00");
        this.lblQtdReceitas = new JLabel("0 entrada(s) registrada(s)");
        grid.add(criarCardResumo("TOTAL DE RECEITAS", this.lblValorReceitas, this.lblQtdReceitas, RECEITA_TEAL, BORDER_GRAY));

        this.lblValorDespesas = new JLabel("R$ 0,00");
        this.lblQtdDespesas = new JLabel("0 saída(s) registrada(s)");
        grid.add(criarCardResumo("TOTAL DE DESPESAS", this.lblValorDespesas, this.lblQtdDespesas, DESPESA_CORAL, BORDER_GRAY));

        this.lblValorSaldo = new JLabel("R$ 0,00");
        this.lblQtdTotal = new JLabel("0 lançamento(s) no total");
        grid.add(criarCardResumo("SALDO CONSOLIDADO (POLIMÓRFICO)", this.lblValorSaldo, this.lblQtdTotal, CYAN_PRIMARY, CYAN_DARK));

        return grid;
    }

    private JPanel criarCardResumo(String titulo, JLabel lblValor, JLabel lblSub, Color corDestaque, Color corBorda) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SURFACE_GRAY);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 1, 1, 1, corBorda == CYAN_DARK ? CYAN_PRIMARY : corDestaque),
            new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXT_MUTED);
        lblTitulo.setFont(FONT_LABEL);

        lblValor.setForeground(corDestaque);
        lblValor.setFont(FONT_CARD_VALUE);
        lblValor.setBorder(new EmptyBorder(6, 0, 6, 0));

        lblSub.setForeground(TEXT_MUTED);
        lblSub.setFont(FONT_SUBTITLE);

        card.add(lblTitulo);
        card.add(lblValor);
        card.add(lblSub);
        return card;
    }

    private JPanel criarPainelPrincipalDividido() {
        JPanel container = new JPanel(new BorderLayout(20, 0));
        container.setOpaque(false);

        JPanel colunaEsquerda = criarColunaEsquerdaFormularios();
        colunaEsquerda.setPreferredSize(new Dimension(380, 500));

        JPanel colunaDireita = criarColunaDireitaHistorico();

        container.add(colunaEsquerda, BorderLayout.WEST);
        container.add(colunaDireita, BorderLayout.CENTER);
        return container;
    }

    private JPanel criarColunaEsquerdaFormularios() {
        JPanel coluna = new JPanel();
        coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
        coluna.setOpaque(false);

        coluna.add(criarCardNovoLancamento());
        coluna.add(Box.createVerticalStrut(14));
        coluna.add(criarCardCategorias());
        coluna.add(Box.createVerticalStrut(14));
        coluna.add(criarCardImportacaoTexto());

        return coluna;
    }

    private JPanel criarCardNovoLancamento() {
        JPanel card = criarPainelCardPadrao("▌ Novo Lançamento");

        this.cmbTipo = new JComboBox<>(new String[]{"RECEITA", "DESPESA"});
        estilizarComboBox(this.cmbTipo);

        this.txtDescricao = criarCampoTexto("");
        this.txtValor = criarCampoTexto("250.00");
        this.txtData = criarCampoTexto(LocalDate.now().toString());

        this.cmbCategoria = new JComboBox<>(this.modeloComboCategorias);
        estilizarComboBox(this.cmbCategoria);

        card.add(criarRotuloCampo("TIPO DE MOVIMENTAÇÃO"));
        card.add(this.cmbTipo);
        card.add(Box.createVerticalStrut(8));

        card.add(criarRotuloCampo("DESCRIÇÃO"));
        card.add(this.txtDescricao);
        card.add(Box.createVerticalStrut(8));

        JPanel linhaValorData = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        linhaValorData.setOpaque(false);
        linhaValorData.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaValorData.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JPanel colValor = new JPanel();
        colValor.setLayout(new BoxLayout(colValor, BoxLayout.Y_AXIS));
        colValor.setOpaque(false);
        colValor.add(criarRotuloCampo("VALOR (R$)"));
        colValor.add(this.txtValor);

        JPanel colData = new JPanel();
        colData.setLayout(new BoxLayout(colData, BoxLayout.Y_AXIS));
        colData.setOpaque(false);
        colData.add(criarRotuloCampo("DATA (YYYY-MM-DD)"));
        colData.add(this.txtData);

        linhaValorData.add(colValor);
        linhaValorData.add(colData);
        card.add(linhaValorData);
        card.add(Box.createVerticalStrut(8));

        card.add(criarRotuloCampo("CATEGORIA"));
        card.add(this.cmbCategoria);
        card.add(Box.createVerticalStrut(12));

        JButton btnRegistrar = criarBotaoCiano("Registrar Lançamento");
        btnRegistrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegistrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnRegistrar.addActionListener(e -> acaoCadastrarLancamento());
        card.add(btnRegistrar);

        return card;
    }

    private JPanel criarCardCategorias() {
        JPanel card = criarPainelCardPadrao("▌ Categorias");

        JPanel linhaAdd = new JPanel(new BorderLayout(8, 0));
        linhaAdd.setOpaque(false);
        linhaAdd.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaAdd.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        this.txtNovaCategoria = criarCampoTexto("");
        JButton btnAddCat = criarBotaoSecundario("Adicionar");
        btnAddCat.addActionListener(e -> acaoAdicionarCategoria());

        linhaAdd.add(this.txtNovaCategoria, BorderLayout.CENTER);
        linhaAdd.add(btnAddCat, BorderLayout.EAST);
        card.add(linhaAdd);
        card.add(Box.createVerticalStrut(8));

        this.lstCategorias = new JList<>(this.modeloListaCategorias);
        this.lstCategorias.setBackground(BG_INPUT);
        this.lstCategorias.setForeground(TEXT_PRIMARY);
        this.lstCategorias.setSelectionBackground(CYAN_DARK);
        this.lstCategorias.setSelectionForeground(BG_BLACK);
        this.lstCategorias.setFont(FONT_BODY);
        this.lstCategorias.setFixedCellHeight(24);

        JScrollPane scrollCat = new JScrollPane(this.lstCategorias);
        scrollCat.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollCat.setPreferredSize(new Dimension(320, 82));
        scrollCat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        scrollCat.setBorder(new LineBorder(BORDER_GRAY, 1));
        card.add(scrollCat);
        card.add(Box.createVerticalStrut(8));

        JButton btnRemoverCat = criarBotaoSecundario("Remover Categoria Selecionada");
        btnRemoverCat.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRemoverCat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnRemoverCat.addActionListener(e -> acaoRemoverCategoriaSelecionada());
        card.add(btnRemoverCat);

        return card;
    }

    private JPanel criarCardImportacaoTexto() {
        JPanel card = criarPainelCardPadrao("▌ Importar Lote CSV / TXT");

        JLabel dica = new JLabel("Formato: TIPO;DESCRICAO;VALOR;DATA;CATEGORIA");
        dica.setForeground(CYAN_PRIMARY);
        dica.setFont(FONT_MONO);
        dica.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(dica);
        card.add(Box.createVerticalStrut(6));

        this.txtAreaCsv = new JTextArea(3, 24);
        this.txtAreaCsv.setBackground(BG_INPUT);
        this.txtAreaCsv.setForeground(TEXT_PRIMARY);
        this.txtAreaCsv.setCaretColor(CYAN_PRIMARY);
        this.txtAreaCsv.setFont(FONT_MONO);
        this.txtAreaCsv.setText("RECEITA;Freelance Java;950.00;" + LocalDate.now() + ";Trabalho");

        JScrollPane scrollCsv = new JScrollPane(this.txtAreaCsv);
        scrollCsv.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollCsv.setBorder(new LineBorder(BORDER_GRAY, 1));
        scrollCsv.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        card.add(scrollCsv);
        card.add(Box.createVerticalStrut(8));

        JButton btnImportarTexto = criarBotaoSecundario("Importar Linhas Acima");
        btnImportarTexto.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnImportarTexto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnImportarTexto.addActionListener(e -> acaoImportarTextoCsv());
        card.add(btnImportarTexto);

        return card;
    }

    private JPanel criarColunaDireitaHistorico() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(SURFACE_GRAY);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 1, true),
            new EmptyBorder(18, 20, 18, 20)
        ));

        // Cabeçalho + Barra de Filtro por Período
        JPanel topoHistorico = new JPanel(new BorderLayout(12, 8));
        topoHistorico.setOpaque(false);

        JPanel blocoTitulo = new JPanel();
        blocoTitulo.setLayout(new BoxLayout(blocoTitulo, BoxLayout.Y_AXIS));
        blocoTitulo.setOpaque(false);

        JLabel titulo = new JLabel("▌ Histórico de Lançamentos");
        titulo.setForeground(CYAN_PRIMARY);
        titulo.setFont(FONT_SECTION);

        this.lblResumoPolimorfico = new JLabel("Relatório Polimórfico");
        this.lblResumoPolimorfico.setForeground(TEXT_MUTED);
        this.lblResumoPolimorfico.setFont(FONT_SUBTITLE);

        blocoTitulo.add(titulo);
        blocoTitulo.add(Box.createVerticalStrut(4));
        blocoTitulo.add(this.lblResumoPolimorfico);

        JPanel barraFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barraFiltro.setOpaque(false);

        this.txtFiltroInicio = criarCampoTexto("");
        this.txtFiltroInicio.setPreferredSize(new Dimension(110, 32));
        this.txtFiltroInicio.setToolTipText("Data inicial (YYYY-MM-DD)");

        this.txtFiltroFim = criarCampoTexto("");
        this.txtFiltroFim.setPreferredSize(new Dimension(110, 32));
        this.txtFiltroFim.setToolTipText("Data final (YYYY-MM-DD)");

        JButton btnFiltrar = criarBotaoSecundario("Filtrar Período");
        btnFiltrar.addActionListener(e -> acaoFiltrarPorPeriodo());

        JButton btnLimpar = criarBotaoSecundario("Limpar");
        btnLimpar.addActionListener(e -> {
            this.txtFiltroInicio.setText("");
            this.txtFiltroFim.setText("");
            atualizarTelaCompleta(null, null);
        });

        JLabel lblDe = new JLabel("Início:");
        lblDe.setForeground(TEXT_MUTED);
        lblDe.setFont(FONT_LABEL);

        JLabel lblAte = new JLabel("Fim:");
        lblAte.setForeground(TEXT_MUTED);
        lblAte.setFont(FONT_LABEL);

        barraFiltro.add(lblDe);
        barraFiltro.add(this.txtFiltroInicio);
        barraFiltro.add(lblAte);
        barraFiltro.add(this.txtFiltroFim);
        barraFiltro.add(btnFiltrar);
        barraFiltro.add(btnLimpar);

        topoHistorico.add(blocoTitulo, BorderLayout.WEST);
        topoHistorico.add(barraFiltro, BorderLayout.EAST);

        // Tabela estilizada
        String[] colunas = {"ID", "Data", "Tipo", "Resumo (Sobrescrita POO)", "Categoria", "Impacto (R$)"};
        this.modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tabelaLancamentos = new JTable(this.modeloTabela);
        this.tabelaLancamentos.setBackground(BG_INPUT);
        this.tabelaLancamentos.setForeground(TEXT_PRIMARY);
        this.tabelaLancamentos.setGridColor(BORDER_GRAY);
        this.tabelaLancamentos.setRowHeight(32);
        this.tabelaLancamentos.setFont(FONT_BODY);
        this.tabelaLancamentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tabelaLancamentos.setSelectionBackground( new Color(34, 211, 238, 45) );
        this.tabelaLancamentos.setSelectionForeground(CYAN_PRIMARY);
        this.tabelaLancamentos.setFillsViewportHeight(true);

        JTableHeader header = this.tabelaLancamentos.getTableHeader();
        header.setBackground(SURFACE_GRAY_2);
        header.setForeground(CYAN_PRIMARY);
        header.setFont(FONT_LABEL);
        header.setPreferredSize(new Dimension(100, 34));

        this.tabelaLancamentos.getColumnModel().getColumn(0).setPreferredWidth(45);
        this.tabelaLancamentos.getColumnModel().getColumn(1).setPreferredWidth(95);
        this.tabelaLancamentos.getColumnModel().getColumn(2).setPreferredWidth(90);
        this.tabelaLancamentos.getColumnModel().getColumn(3).setPreferredWidth(280);
        this.tabelaLancamentos.getColumnModel().getColumn(4).setPreferredWidth(120);
        this.tabelaLancamentos.getColumnModel().getColumn(5).setPreferredWidth(120);

        DefaultTableCellRenderer renderizadorPadrao = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
            ) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String tipoLinha = String.valueOf(table.getModel().getValueAt(row, 2));
                if (isSelected) {
                    c.setBackground(SURFACE_GRAY_2);
                    c.setForeground(CYAN_PRIMARY);
                } else {
                    c.setBackground(row % 2 == 0 ? BG_INPUT : SURFACE_GRAY);
                    if (column == 2 || column == 5) {
                        c.setForeground("RECEITA".equalsIgnoreCase(tipoLinha) ? RECEITA_TEAL : DESPESA_CORAL);
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        c.setForeground(TEXT_PRIMARY);
                        setFont(FONT_BODY);
                    }
                }
                setHorizontalAlignment(column == 5 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 0; i < this.tabelaLancamentos.getColumnCount(); i++) {
            this.tabelaLancamentos.getColumnModel().getColumn(i).setCellRenderer(renderizadorPadrao);
        }

        JScrollPane scrollTabela = new JScrollPane(this.tabelaLancamentos);
        scrollTabela.getViewport().setBackground(BG_INPUT);
        scrollTabela.setBorder(new LineBorder(BORDER_GRAY, 1));

        // Rodapé da tabela com botão de exclusão
        JPanel rodapeHistorico = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rodapeHistorico.setOpaque(false);

        JButton btnExcluirLancamento = criarBotaoPerigo("Excluir Lançamento Selecionado");
        btnExcluirLancamento.addActionListener(e -> acaoExcluirLancamentoSelecionado());
        rodapeHistorico.add(btnExcluirLancamento);

        card.add(topoHistorico, BorderLayout.NORTH);
        card.add(scrollTabela, BorderLayout.CENTER);
        card.add(rodapeHistorico, BorderLayout.SOUTH);

        return card;
    }

    private void acaoCadastrarLancamento() {
        try {
            String tipo = String.valueOf(this.cmbTipo.getSelectedItem());
            String descricao = this.txtDescricao.getText().trim();
            String valorStr = this.txtValor.getText().trim().replace(",", ".");
            String dataStr = this.txtData.getText().trim();
            CategoriaItem catItem = (CategoriaItem) this.cmbCategoria.getSelectedItem();

            if (valorStr.isEmpty()) {
                throw new FinanceHubException("VALOR_OBRIGATORIO", "Informe o valor do lançamento.");
            }

            BigDecimal valor = new BigDecimal(valorStr).setScale(2, RoundingMode.HALF_UP);
            LocalDate data = LocalDate.parse(dataStr);
            Categoria categoria = catItem != null ? catItem.categoria : null;

            Lancamento novo = "RECEITA".equalsIgnoreCase(tipo)
                ? new Receita(null, descricao, valor, data, categoria, 1L)
                : new Despesa(null, descricao, valor, data, categoria, 1L);

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

            this.txtDescricao.setText("");
            this.txtValor.setText("");
            atualizarTelaCompleta(null, null);
        } catch (FinanceHubException ex) {
            mostrarErro("Erro de Validação (" + ex.getCodigo() + ")", ex.getMessage());
        } catch (Exception ex) {
            mostrarErro("Dados Inválidos", "Verifique o valor numérico e a data (YYYY-MM-DD).\nDetalhe: " + ex.getMessage());
        }
    }

    private void acaoAdicionarCategoria() {
        try {
            String nome = this.txtNovaCategoria.getText().trim();
            if (nome.isEmpty()) {
                mostrarErro("Categoria Inválida", "Digite o nome da nova categoria.");
                return;
            }
            long novoId = gerarProximoIdCategoria();
            Categoria nova = new Categoria(novoId, nome);
            this.gerenciadorCategorias.cadastrar(nova);

            if (this.mysqlAtivo) {
                try {
                    this.jdbcCategoriaRepository.salvar(nova);
                } catch (Exception ignored) {
                }
            }
            sincronizarPersistenciaLocal();
            this.txtNovaCategoria.setText("");
            atualizarTelaCompleta(null, null);
        } catch (Exception ex) {
            mostrarErro("Erro ao Cadastrar Categoria", ex.getMessage());
        }
    }

    private void acaoRemoverCategoriaSelecionada() {
        CategoriaItem selecionada = this.lstCategorias.getSelectedValue();
        if (selecionada == null) {
            mostrarErro("Seleção Necessária", "Selecione uma categoria na lista para remover.");
            return;
        }
        long id = selecionada.categoria.getId();
        if (this.gerenciadorCategorias.removerPorId(id)) {
            if (this.mysqlAtivo) {
                try {
                    this.jdbcCategoriaRepository.removerPorId(id);
                } catch (Exception ignored) {
                }
            }
            sincronizarPersistenciaLocal();
            atualizarTelaCompleta(null, null);
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
            if (this.mysqlAtivo) {
                for (Lancamento l : importados) {
                    try {
                        this.jdbcLancamentoRepository.salvar(l);
                    } catch (Exception ignored) {
                    }
                }
            }
            sincronizarPersistenciaLocal();
            atualizarTelaCompleta(null, null);
            JOptionPane.showMessageDialog(
                this,
                importados.size() + " lançamento(s) importado(s) com sucesso!",
                "Importação Concluída",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            mostrarErro("Erro na Importação CSV/TXT", ex.getMessage());
        }
    }

    private void acaoImportarDeArquivoDisco() {
        JFileChooser chooser = new JFileChooser(Path.of(".").toFile());
        chooser.setDialogTitle("Selecionar Arquivo CSV ou TXT para Importação");
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos CSV ou TXT (*.csv, *.txt)", "csv", "txt"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path arquivo = chooser.getSelectedFile().toPath();
                ArrayList<Lancamento> importados = this.importadorArquivoService.importarDeArquivo(
                    arquivo,
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
                atualizarTelaCompleta(null, null);
                JOptionPane.showMessageDialog(
                    this,
                    importados.size() + " lançamento(s) importado(s) de " + arquivo.getFileName(),
                    "Importação de Arquivo Concluída",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                mostrarErro("Erro ao Ler Arquivo", ex.getMessage());
            }
        }
    }

    private void acaoExportarParaArquivoDisco() {
        JFileChooser chooser = new JFileChooser(Path.of(".").toFile());
        chooser.setDialogTitle("Salvar Lançamentos em Arquivo CSV");
        chooser.setSelectedFile(Path.of("financehub-lancamentos.csv").toFile());

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path destino = chooser.getSelectedFile().toPath();
                this.importadorArquivoService.exportarParaArquivo(destino, this.gerenciadorLancamentos.listar());
                JOptionPane.showMessageDialog(
                    this,
                    "Arquivo exportado com sucesso em:\n" + destino.toAbsolutePath(),
                    "Exportação Concluída",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                mostrarErro("Erro ao Exportar CSV", ex.getMessage());
            }
        }
    }

    private void acaoFiltrarPorPeriodo() {
        try {
            String iniStr = this.txtFiltroInicio.getText().trim();
            String fimStr = this.txtFiltroFim.getText().trim();
            if (iniStr.isEmpty() || fimStr.isEmpty()) {
                mostrarErro("Filtro Incompleto", "Informe as datas de Início e Fim no formato YYYY-MM-DD.");
                return;
            }
            LocalDate inicio = LocalDate.parse(iniStr);
            LocalDate fim = LocalDate.parse(fimStr);
            atualizarTelaCompleta(inicio, fim);
        } catch (Exception ex) {
            mostrarErro("Erro no Filtro de Período", ex.getMessage());
        }
    }

    private void acaoExcluirLancamentoSelecionado() {
        int linha = this.tabelaLancamentos.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Seleção Necessária", "Clique em um lançamento na tabela para excluí-lo.");
            return;
        }
        long id = Long.parseLong(String.valueOf(this.modeloTabela.getValueAt(linha, 0)));
        if (this.gerenciadorLancamentos.removerPorId(id)) {
            if (this.mysqlAtivo) {
                try {
                    this.jdbcLancamentoRepository.removerPorId(id);
                } catch (Exception ignored) {
                }
            }
            sincronizarPersistenciaLocal();
            atualizarTelaCompleta(null, null);
        }
    }

    private void atualizarTelaCompleta(LocalDate inicio, LocalDate fim) {
        try {
            // 1. Atualizar Categorias
            CategoriaItem selecionadaAntes = (CategoriaItem) this.cmbCategoria.getSelectedItem();
            this.modeloComboCategorias.removeAllElements();
            this.modeloListaCategorias.clear();

            for (Categoria c : this.gerenciadorCategorias.listar()) {
                CategoriaItem item = new CategoriaItem(c);
                this.modeloComboCategorias.addElement(item);
                this.modeloListaCategorias.addElement(item);
            }
            if (selecionadaAntes != null) {
                for (int i = 0; i < this.modeloComboCategorias.getSize(); i++) {
                    if (this.modeloComboCategorias.getElementAt(i).categoria.getId() == selecionadaAntes.categoria.getId()) {
                        this.cmbCategoria.setSelectedIndex(i);
                        break;
                    }
                }
            }

            // 2. Atualizar Relatório e Lançamentos
            List<Lancamento> lancamentos;
            Relatorio relatorio;
            if (inicio != null && fim != null) {
                lancamentos = this.gerenciadorLancamentos.listarPorPeriodo(inicio, fim);
                relatorio = this.gerenciadorLancamentos.gerarRelatorioPorPeriodo(inicio, fim);
            } else {
                lancamentos = this.gerenciadorLancamentos.listar();
                relatorio = this.gerenciadorLancamentos.gerarRelatorio();
            }

            this.lblValorReceitas.setText(this.formatoMoeda.format(relatorio.getTotalReceitas()));
            this.lblQtdReceitas.setText(relatorio.getQuantidadeReceitas() + " entrada(s) registrada(s)");

            this.lblValorDespesas.setText(this.formatoMoeda.format(relatorio.getTotalDespesas()));
            this.lblQtdDespesas.setText(relatorio.getQuantidadeDespesas() + " saída(s) registrada(s)");

            this.lblValorSaldo.setText(this.formatoMoeda.format(relatorio.getSaldoTotal()));
            this.lblQtdTotal.setText(relatorio.getQuantidadeLancamentos() + " lançamento(s) no total");
            this.lblResumoPolimorfico.setText(relatorio.gerarResumoTexto());

            // 3. Popular Tabela
            this.modeloTabela.setRowCount(0);
            for (Lancamento l : lancamentos) {
                String nomeCat = l.getCategoria() != null ? l.getCategoria().getNome() : "Geral";
                String impactoFormatado = this.formatoMoeda.format(l.calcularImpacto());
                this.modeloTabela.addRow(new Object[]{
                    l.getId(),
                    l.getData().toString(),
                    l.getTipo(),
                    l.getResumo(),
                    nomeCat,
                    impactoFormatado
                });
            }
        } catch (FinanceHubException ex) {
            mostrarErro("Erro ao Atualizar Dados", ex.getMessage());
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

    private JPanel criarPainelCardPadrao(String tituloTexto) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SURFACE_GRAY);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel titulo = new JLabel(tituloTexto);
        titulo.setForeground(CYAN_PRIMARY);
        titulo.setFont(FONT_SECTION);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titulo);
        card.add(Box.createVerticalStrut(10));
        return card;
    }

    private JLabel criarRotuloCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(TEXT_MUTED);
        lbl.setFont(FONT_LABEL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 3, 0));
        return lbl;
    }

    private JTextField criarCampoTexto(String valorInicial) {
        JTextField txt = new JTextField(valorInicial);
        txt.setBackground(BG_INPUT);
        txt.setForeground(TEXT_PRIMARY);
        txt.setCaretColor(CYAN_PRIMARY);
        txt.setFont(FONT_BODY);
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return txt;
    }

    private <T> void estilizarComboBox(JComboBox<T> combo) {
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_BODY);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        combo.setBorder(new LineBorder(BORDER_GRAY, 1));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                comp.setBackground(isSelected ? CYAN_DARK : BG_INPUT);
                comp.setForeground(isSelected ? BG_BLACK : TEXT_PRIMARY);
                comp.setBorder(new EmptyBorder(4, 8, 4, 8));
                return comp;
            }
        });
    }

    private JButton criarBotaoCiano(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(CYAN_PRIMARY);
        btn.setForeground(BG_BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CYAN_DARK, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
        return btn;
    }

    private JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(SURFACE_GRAY_2);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_GRAY, 1, true),
            new EmptyBorder(7, 12, 7, 12)
        ));
        return btn;
    }

    private JButton criarBotaoPerigo(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(SURFACE_GRAY_2);
        btn.setForeground(DESPESA_CORAL);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(DESPESA_CORAL, 1, true),
            new EmptyBorder(7, 14, 7, 14)
        ));
        return btn;
    }

    private void mostrarErro(String titulo, String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, titulo, JOptionPane.ERROR_MESSAGE);
    }

    private static class CategoriaItem {
        private final Categoria categoria;

        private CategoriaItem(Categoria categoria) {
            this.categoria = categoria;
        }

        @Override
        public String toString() {
            return this.categoria.getId() + " - " + this.categoria.getNome();
        }
    }
}

