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
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interface gráfica principal do FinanceHub em Java Swing.
 * Paleta visual: Preto, Cinza Escuro e Ciano.
 */
public class FinanceHubSwingFrame extends JFrame {

    // Paleta de cores (Preto, Cinza e Ciano)
    private static final Color BG_PRETO = new Color(9, 13, 20);
    private static final Color BG_CAMPO = new Color(15, 20, 30);
    private static final Color CARD_CINZA = new Color(20, 26, 36);
    private static final Color CARD_CINZA_CLARO = new Color(28, 36, 48);
    private static final Color BORDA_CINZA = new Color(45, 56, 72);
    private static final Color TEXTO_CLARO = new Color(235, 242, 248);
    private static final Color TEXTO_CINZA = new Color(148, 163, 184);
    private static final Color CIANO_VIVO = new Color(34, 211, 238);
    private static final Color CIANO_ESCURO = new Color(6, 182, 212);
    private static final Color VERDE_RECEITA = new Color(45, 212, 191);
    private static final Color CORAL_DESPESA = new Color(248, 113, 113);

    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 19);
    private static final Font FONTE_SECAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_VALOR_CARD = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONTE_ROTULO = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONTE_TEXTO = new Font("Segoe UI", Font.PLAIN, 13);

    // Objetos de domínio, serviços e contrato de persistência (Interface)
    private final Usuario usuarioAtual;
    private final GerenciadorCategorias gerenciadorCategorias;
    private final GerenciadorLancamentos gerenciadorLancamentos;
    private final ImportadorArquivoService importadorArquivoService;
    private final CotacaoMoedaService cotacaoMoedaService;
    private final RepositorioDados repositorioDados;

    private final NumberFormat moedaBRL = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private final NumberFormat moedaUSD = NumberFormat.getCurrencyInstance(Locale.US);

    // Componentes do topo e cards
    private JLabel lblUsuarioTopo;
    private JLabel lblCotacaoTopo;
    private JLabel lblValorReceitas;
    private JLabel lblQtdReceitas;
    private JProgressBar barraReceitas;
    private JLabel lblValorDespesas;
    private JLabel lblQtdDespesas;
    private JProgressBar barraDespesas;
    private JLabel lblValorSaldo;
    private JLabel lblQtdTotal;
    private JLabel lblValorSaldoDolar;
    private JLabel lblDetalheDolar;
    private JLabel lblResumoGeral;

    // Abas e formulários
    private JTabbedPane abasLateral;
    private Long idEmEdicao = null;
    private JLabel lblModoFormulario;
    private JComboBox<String> cmbTipo;
    private JTextField txtDescricao;
    private JTextField txtValor;
    private JTextField txtData;
    private final DefaultComboBoxModel<Categoria> modeloComboCategorias = new DefaultComboBoxModel<>();
    private JComboBox<Categoria> cmbCategoria;

    // Aba de categorias
    private JTextField txtNovaCategoria;
    private final DefaultListModel<String> modeloListaCategorias = new DefaultListModel<>();
    private JList<String> lstCategorias;

    // Aba de importação CSV/TXT
    private JTextArea txtAreaCsv;

    // Filtros e tabela
    private JTextField txtBuscaDescricao;
    private JComboBox<String> cmbFiltroTipo;
    private JTextField txtFiltroInicio;
    private JTextField txtFiltroFim;
    private DefaultTableModel modeloTabela;
    private JTable tabelaLancamentos;

    public FinanceHubSwingFrame(Path arquivoPersistenciaLocal) {
        super("FinanceHub — Gerenciador Financeiro Pessoal (POO Java 21)");
        configurarCoresDialogo();

        this.usuarioAtual = new Usuario(1L, "Rafael", "rafael@financehub.com");
        this.gerenciadorCategorias = new GerenciadorCategorias();
        this.gerenciadorLancamentos = new GerenciadorLancamentos();
        this.importadorArquivoService = new ImportadorArquivoService();
        this.cotacaoMoedaService = new CotacaoMoedaService();
        this.repositorioDados = new JdbcRepositorioDados(arquivoPersistenciaLocal);

        carregarDadosIniciais();
        construirJanela();
        atualizarInterface(null, null);
    }

    private void configurarCoresDialogo() {
        UIManager.put("OptionPane.background", CARD_CINZA);
        UIManager.put("Panel.background", CARD_CINZA);
        UIManager.put("OptionPane.messageForeground", TEXTO_CLARO);
        UIManager.put("TabbedPane.selected", CARD_CINZA_CLARO);
        UIManager.put("TabbedPane.background", BG_PRETO);
        UIManager.put("TabbedPane.foreground", TEXTO_CLARO);
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

    private void construirJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 840);
        setMinimumSize(new Dimension(1120, 740));
        setLocationRelativeTo(null);

        JPanel painelRaiz = new JPanel(new BorderLayout(0, 16));
        painelRaiz.setBackground(BG_PRETO);
        painelRaiz.setBorder(new EmptyBorder(0, 0, 16, 0));
        setContentPane(painelRaiz);

        painelRaiz.add(criarBarraTopo(), BorderLayout.NORTH);

        JPanel miolo = new JPanel(new BorderLayout(0, 16));
        miolo.setBackground(BG_PRETO);
        miolo.setBorder(new EmptyBorder(0, 20, 0, 20));

        miolo.add(criarPainelQuatroCards(), BorderLayout.NORTH);
        miolo.add(criarAreaTrabalhoCentral(), BorderLayout.CENTER);

        painelRaiz.add(miolo, BorderLayout.CENTER);
    }

    private JPanel criarBarraTopo() {
        JPanel barra = new JPanel(new BorderLayout(16, 0));
        barra.setBackground(CARD_CINZA);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, CIANO_ESCURO),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        esquerda.setOpaque(false);

        JLabel logo = new JLabel("FH", SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(44, 44));
        logo.setOpaque(true);
        logo.setBackground(CIANO_VIVO);
        logo.setForeground(BG_PRETO);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JPanel textosMarca = new JPanel();
        textosMarca.setLayout(new BoxLayout(textosMarca, BoxLayout.Y_AXIS));
        textosMarca.setOpaque(false);

        JLabel titulo = new JLabel("FinanceHub");
        titulo.setForeground(TEXTO_CLARO);
        titulo.setFont(FONTE_TITULO);

        this.lblUsuarioTopo = new JLabel();
        this.lblUsuarioTopo.setForeground(CIANO_VIVO);
        this.lblUsuarioTopo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        atualizarTextoUsuarioTopo();

        textosMarca.add(titulo);
        textosMarca.add(this.lblUsuarioTopo);

        esquerda.add(logo);
        esquerda.add(textosMarca);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        direita.setOpaque(false);

        this.lblCotacaoTopo = new JLabel("  US$ 1 = R$ " + this.cotacaoMoedaService.getUltimaCotacaoDolar() + "  ");
        this.lblCotacaoTopo.setOpaque(true);
        this.lblCotacaoTopo.setBackground(CARD_CINZA_CLARO);
        this.lblCotacaoTopo.setForeground(CIANO_VIVO);
        this.lblCotacaoTopo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        this.lblCotacaoTopo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CIANO_ESCURO, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));

        JButton btnAtualizarCotacao = criarBotaoSecundario("Atualizar Dólar (API)");
        btnAtualizarCotacao.addActionListener(e -> acaoConsultarApiCotacao());

        JButton btnEditarUsuario = criarBotaoSecundario("Perfil do Usuário");
        btnEditarUsuario.addActionListener(e -> acaoEditarUsuario());

        JButton btnImportar = criarBotaoSecundario("Importar CSV/TXT");
        btnImportar.addActionListener(e -> acaoImportarArquivoDisco());

        JButton btnExportar = criarBotaoCiano("Exportar CSV");
        btnExportar.addActionListener(e -> acaoExportarArquivoDisco());

        direita.add(this.lblCotacaoTopo);
        direita.add(btnAtualizarCotacao);
        direita.add(btnEditarUsuario);
        direita.add(btnImportar);
        direita.add(btnExportar);

        barra.add(esquerda, BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);
        return barra;
    }

    private JPanel criarPainelQuatroCards() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);

        this.lblValorReceitas = new JLabel("R$ 0,00");
        this.lblQtdReceitas = new JLabel("0 entradas");
        this.barraReceitas = criarBarraProgresso(VERDE_RECEITA);
        grid.add(criarCardDashboard("RECEITAS (+)", this.lblValorReceitas, this.lblQtdReceitas, this.barraReceitas, VERDE_RECEITA));

        this.lblValorDespesas = new JLabel("R$ 0,00");
        this.lblQtdDespesas = new JLabel("0 saídas");
        this.barraDespesas = criarBarraProgresso(CORAL_DESPESA);
        grid.add(criarCardDashboard("DESPESAS (-)", this.lblValorDespesas, this.lblQtdDespesas, this.barraDespesas, CORAL_DESPESA));

        this.lblValorSaldo = new JLabel("R$ 0,00");
        this.lblQtdTotal = new JLabel("0 lançamentos");
        grid.add(criarCardDashboard("SALDO EM REAIS (R$)", this.lblValorSaldo, this.lblQtdTotal, null, CIANO_VIVO));

        this.lblValorSaldoDolar = new JLabel("US$ 0.00");
        this.lblDetalheDolar = new JLabel("Conversão via CotacaoMoedaService");
        grid.add(criarCardDashboard("SALDO EM DÓLAR (API EXTERNA)", this.lblValorSaldoDolar, this.lblDetalheDolar, null, CIANO_VIVO));

        return grid;
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
            new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXTO_CINZA);
        lblTitulo.setFont(FONTE_ROTULO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblValor.setForeground(corDestaque);
        lblValor.setFont(FONTE_VALOR_CARD);
        lblValor.setBorder(new EmptyBorder(6, 0, 6, 0));
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

    private JPanel criarAreaTrabalhoCentral() {
        JPanel painel = new JPanel(new BorderLayout(18, 0));
        painel.setOpaque(false);

        this.abasLateral = new JTabbedPane();
        this.abasLateral.setPreferredSize(new Dimension(390, 520));
        this.abasLateral.setFont(new Font("Segoe UI", Font.BOLD, 12));
        this.abasLateral.setBackground(CARD_CINZA);
        this.abasLateral.setForeground(TEXTO_CLARO);

        this.abasLateral.addTab("1. Lançamento", criarAbaFormularioLancamento());
        this.abasLateral.addTab("2. Categorias", criarAbaCategorias());
        this.abasLateral.addTab("3. Importar CSV", criarAbaImportacaoCsv());

        painel.add(this.abasLateral, BorderLayout.WEST);
        painel.add(criarPainelTabelaDireita(), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarAbaFormularioLancamento() {
        JPanel card = criarPainelInternoAba();

        this.lblModoFormulario = new JLabel("▌ Cadastrar Novo Lançamento");
        this.lblModoFormulario.setForeground(CIANO_VIVO);
        this.lblModoFormulario.setFont(FONTE_SECAO);
        this.lblModoFormulario.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(this.lblModoFormulario);
        card.add(Box.createVerticalStrut(14));

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
                lbl.setBorder(new EmptyBorder(5, 8, 5, 8));
                return lbl;
            }
        });

        card.add(criarRotulo("TIPO DE MOVIMENTAÇÃO"));
        card.add(this.cmbTipo);
        card.add(Box.createVerticalStrut(12));

        card.add(criarRotulo("DESCRIÇÃO DO LANÇAMENTO"));
        card.add(this.txtDescricao);
        card.add(Box.createVerticalStrut(12));

        card.add(criarRotulo("VALOR EM REAIS (R$)"));
        card.add(this.txtValor);
        card.add(Box.createVerticalStrut(12));

        card.add(criarRotulo("DATA (FORMATO AAAA-MM-DD)"));
        card.add(this.txtData);
        card.add(Box.createVerticalStrut(12));

        card.add(criarRotulo("CATEGORIA ASSOCIADA"));
        card.add(this.cmbCategoria);
        card.add(Box.createVerticalStrut(18));

        JButton btnSalvar = criarBotaoCiano("Salvar Lançamento");
        btnSalvar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSalvar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSalvar.addActionListener(e -> acaoSalvarLancamento());
        card.add(btnSalvar);

        card.add(Box.createVerticalStrut(8));

        JButton btnCancelarEdicao = criarBotaoSecundario("Limpar / Cancelar Edição");
        btnCancelarEdicao.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancelarEdicao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnCancelarEdicao.addActionListener(e -> limparFormularioLancamento());
        card.add(btnCancelarEdicao);

        return card;
    }

    private JPanel criarAbaCategorias() {
        JPanel card = criarPainelInternoAba();

        JLabel titulo = new JLabel("▌ Gerenciar Categorias e Saldos");
        titulo.setForeground(CIANO_VIVO);
        titulo.setFont(FONTE_SECAO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titulo);
        card.add(Box.createVerticalStrut(12));

        card.add(criarRotulo("NOME DA NOVA CATEGORIA"));
        this.txtNovaCategoria = criarCampoTexto("");
        card.add(this.txtNovaCategoria);
        card.add(Box.createVerticalStrut(8));

        JButton btnAdicionar = criarBotaoCiano("Cadastrar Categoria");
        btnAdicionar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAdicionar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnAdicionar.addActionListener(e -> acaoAdicionarCategoria());
        card.add(btnAdicionar);
        card.add(Box.createVerticalStrut(16));

        card.add(criarRotulo("CATEGORIAS CADASTRADAS (COM SALDO POLIMÓRFICO)"));
        this.lstCategorias = new JList<>(this.modeloListaCategorias);
        this.lstCategorias.setBackground(BG_CAMPO);
        this.lstCategorias.setForeground(TEXTO_CLARO);
        this.lstCategorias.setSelectionBackground(CIANO_ESCURO);
        this.lstCategorias.setSelectionForeground(BG_PRETO);
        this.lstCategorias.setFont(FONTE_TEXTO);
        this.lstCategorias.setFixedCellHeight(28);

        JScrollPane scroll = new JScrollPane(this.lstCategorias);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(new LineBorder(BORDA_CINZA, 1));
        card.add(scroll);
        card.add(Box.createVerticalStrut(10));

        JButton btnRemover = criarBotaoPerigo("Remover Categoria Selecionada");
        btnRemover.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRemover.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnRemover.addActionListener(e -> acaoRemoverCategoria());
        card.add(btnRemover);

        return card;
    }

    private JPanel criarAbaImportacaoCsv() {
        JPanel card = criarPainelInternoAba();

        JLabel titulo = new JLabel("▌ Importar Lote CSV ou TXT");
        titulo.setForeground(CIANO_VIVO);
        titulo.setFont(FONTE_SECAO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titulo);
        card.add(Box.createVerticalStrut(10));

        JLabel instrucao = new JLabel("Padrão: TIPO;DESCRICAO;VALOR;DATA;CATEGORIA");
        instrucao.setForeground(TEXTO_CINZA);
        instrucao.setFont(FONTE_ROTULO);
        instrucao.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(instrucao);
        card.add(Box.createVerticalStrut(8));

        this.txtAreaCsv = new JTextArea(10, 25);
        this.txtAreaCsv.setBackground(BG_CAMPO);
        this.txtAreaCsv.setForeground(TEXTO_CLARO);
        this.txtAreaCsv.setCaretColor(CIANO_VIVO);
        this.txtAreaCsv.setFont(new Font("Consolas", Font.PLAIN, 12));
        this.txtAreaCsv.setText(
            "RECEITA;Freelance Java;950.00;" + LocalDate.now() + ";Trabalho\n" +
            "DESPESA;Livros POO;140.00;" + LocalDate.now() + ";Trabalho"
        );

        JScrollPane scroll = new JScrollPane(this.txtAreaCsv);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(new LineBorder(BORDA_CINZA, 1));
        card.add(scroll);
        card.add(Box.createVerticalStrut(12));

        JButton btnImportarTexto = criarBotaoCiano("Processar e Importar Linhas");
        btnImportarTexto.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnImportarTexto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnImportarTexto.addActionListener(e -> acaoImportarTextoCsv());
        card.add(btnImportarTexto);

        return card;
    }

    private JPanel criarPainelTabelaDireita() {
        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.setBackground(CARD_CINZA);
        painel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        // Topo com título e filtros
        JPanel topo = new JPanel(new BorderLayout(8, 10));
        topo.setOpaque(false);

        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.setOpaque(false);

        JLabel lblHist = new JLabel("▌ Histórico de Movimentações Financeiras");
        lblHist.setForeground(CIANO_VIVO);
        lblHist.setFont(FONTE_SECAO);

        this.lblResumoGeral = new JLabel("Resumo consolidado");
        this.lblResumoGeral.setForeground(TEXTO_CINZA);
        this.lblResumoGeral.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        titulos.add(lblHist);
        titulos.add(Box.createVerticalStrut(3));
        titulos.add(this.lblResumoGeral);

        JPanel barraFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barraFiltros.setOpaque(false);

        this.txtBuscaDescricao = criarCampoTexto("");
        this.txtBuscaDescricao.setPreferredSize(new Dimension(140, 32));

        this.cmbFiltroTipo = new JComboBox<>(new String[]{"TODOS", "RECEITA", "DESPESA"});
        estilizarCombo(this.cmbFiltroTipo);
        this.cmbFiltroTipo.setPreferredSize(new Dimension(110, 32));

        this.txtFiltroInicio = criarCampoTexto("");
        this.txtFiltroInicio.setPreferredSize(new Dimension(100, 32));

        this.txtFiltroFim = criarCampoTexto("");
        this.txtFiltroFim.setPreferredSize(new Dimension(100, 32));

        JButton btnAplicarFiltro = criarBotaoCiano("Filtrar");
        btnAplicarFiltro.addActionListener(e -> acaoAplicarFiltros());

        JButton btnLimparFiltro = criarBotaoSecundario("Limpar");
        btnLimparFiltro.addActionListener(e -> {
            this.txtBuscaDescricao.setText("");
            this.cmbFiltroTipo.setSelectedIndex(0);
            this.txtFiltroInicio.setText("");
            this.txtFiltroFim.setText("");
            atualizarInterface(null, null);
        });

        barraFiltros.add(criarRotuloInline("Buscar:"));
        barraFiltros.add(this.txtBuscaDescricao);
        barraFiltros.add(criarRotuloInline("Tipo:"));
        barraFiltros.add(this.cmbFiltroTipo);
        barraFiltros.add(criarRotuloInline("De:"));
        barraFiltros.add(this.txtFiltroInicio);
        barraFiltros.add(criarRotuloInline("Até:"));
        barraFiltros.add(this.txtFiltroFim);
        barraFiltros.add(btnAplicarFiltro);
        barraFiltros.add(btnLimparFiltro);

        topo.add(titulos, BorderLayout.NORTH);
        topo.add(barraFiltros, BorderLayout.SOUTH);

        // Tabela de lançamentos
        String[] colunas = {"ID", "Data", "Tipo", "Resumo (Sobrescrita getResumo)", "Categoria", "Impacto (calcularImpacto)"};
        this.modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tabelaLancamentos = new JTable(this.modeloTabela);
        this.tabelaLancamentos.setBackground(BG_CAMPO);
        this.tabelaLancamentos.setForeground(TEXTO_CLARO);
        this.tabelaLancamentos.setGridColor(BORDA_CINZA);
        this.tabelaLancamentos.setRowHeight(34);
        this.tabelaLancamentos.setFont(FONTE_TEXTO);
        this.tabelaLancamentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tabelaLancamentos.setFillsViewportHeight(true);

        JTableHeader header = this.tabelaLancamentos.getTableHeader();
        header.setBackground(CARD_CINZA_CLARO);
        header.setForeground(CIANO_VIVO);
        header.setFont(FONTE_ROTULO);
        header.setPreferredSize(new Dimension(100, 36));

        this.tabelaLancamentos.getColumnModel().getColumn(0).setPreferredWidth(45);
        this.tabelaLancamentos.getColumnModel().getColumn(1).setPreferredWidth(95);
        this.tabelaLancamentos.getColumnModel().getColumn(2).setPreferredWidth(85);
        this.tabelaLancamentos.getColumnModel().getColumn(3).setPreferredWidth(260);
        this.tabelaLancamentos.getColumnModel().getColumn(4).setPreferredWidth(120);
        this.tabelaLancamentos.getColumnModel().getColumn(5).setPreferredWidth(140);

        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String tipoLinha = String.valueOf(table.getModel().getValueAt(row, 2));
                if (isSelected) {
                    c.setBackground(CARD_CINZA_CLARO);
                    c.setForeground(CIANO_VIVO);
                } else {
                    c.setBackground(row % 2 == 0 ? BG_CAMPO : CARD_CINZA);
                    if (column == 2 || column == 5) {
                        c.setForeground("RECEITA".equalsIgnoreCase(tipoLinha) ? VERDE_RECEITA : CORAL_DESPESA);
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        c.setForeground(TEXTO_CLARO);
                        setFont(FONTE_TEXTO);
                    }
                }
                setHorizontalAlignment(column == 5 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 0; i < this.tabelaLancamentos.getColumnCount(); i++) {
            this.tabelaLancamentos.getColumnModel().getColumn(i).setCellRenderer(renderizador);
        }

        JScrollPane scrollTabela = new JScrollPane(this.tabelaLancamentos);
        scrollTabela.getViewport().setBackground(BG_CAMPO);
        scrollTabela.setBorder(new LineBorder(BORDA_CINZA, 1));

        // Rodapé com botões de Edição e Exclusão
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rodape.setOpaque(false);

        JButton btnEditar = criarBotaoSecundario("Editar Lançamento Selecionado");
        btnEditar.addActionListener(e -> acaoCarregarLancamentoParaEdicao());

        JButton btnExcluir = criarBotaoPerigo("Excluir Lançamento Selecionado");
        btnExcluir.addActionListener(e -> acaoExcluirLancamentoSelecionado());

        rodape.add(btnEditar);
        rodape.add(btnExcluir);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(scrollTabela, BorderLayout.CENTER);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    // ==================== AÇÕES DOS BOTÕES ====================

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
            atualizarInterface(null, null);
        } catch (FinanceHubException e) {
            mostrarErro("Validação (" + e.getCodigo() + ")", e.getMessage());
        } catch (Exception e) {
            mostrarErro("Dados Inválidos", "Verifique o valor numérico e a data (AAAA-MM-DD).\nDetalhe: " + e.getMessage());
        }
    }

    private void acaoCarregarLancamentoParaEdicao() {
        int linha = this.tabelaLancamentos.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Seleção Necessária", "Selecione uma linha na tabela para editar.");
            return;
        }
        try {
            long id = Long.parseLong(String.valueOf(this.modeloTabela.getValueAt(linha, 0)));
            Lancamento l = this.gerenciadorLancamentos.buscarPorId(id);

            this.idEmEdicao = l.getId();
            this.lblModoFormulario.setText("▌ Editando Lançamento #" + l.getId());
            this.cmbTipo.setSelectedItem(l.getTipo());
            this.txtDescricao.setText(l.getDescricao());
            this.txtValor.setText(l.getValor().toPlainString());
            this.txtData.setText(l.getData().toString());
            if (l.getCategoria() != null) {
                this.cmbCategoria.setSelectedItem(l.getCategoria());
            }
            this.abasLateral.setSelectedIndex(0);
        } catch (FinanceHubException e) {
            mostrarErro("Erro ao Carregar", e.getMessage());
        }
    }

    private void limparFormularioLancamento() {
        this.idEmEdicao = null;
        this.lblModoFormulario.setText("▌ Cadastrar Novo Lançamento");
        this.txtDescricao.setText("");
        this.txtValor.setText("");
        this.txtData.setText(LocalDate.now().toString());
    }

    private void acaoAdicionarCategoria() {
        try {
            String nome = this.txtNovaCategoria.getText().trim();
            long novoId = gerarProximoIdCategoria();
            Categoria nova = new Categoria(novoId, nome);
            this.gerenciadorCategorias.cadastrar(nova);
            salvarAlteracoes();
            this.txtNovaCategoria.setText("");
            atualizarInterface(null, null);
        } catch (Exception e) {
            mostrarErro("Erro na Categoria", e.getMessage());
        }
    }

    private void acaoRemoverCategoria() {
        int idx = this.lstCategorias.getSelectedIndex();
        if (idx < 0) {
            mostrarErro("Seleção Necessária", "Selecione uma categoria na lista para remover.");
            return;
        }
        Categoria selecionada = this.gerenciadorCategorias.listar().get(idx);
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
        atualizarInterface(null, null);
    }

    private void acaoExcluirLancamentoSelecionado() {
        int linha = this.tabelaLancamentos.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Seleção Necessária", "Selecione um lançamento na tabela para excluir.");
            return;
        }
        long id = Long.parseLong(String.valueOf(this.modeloTabela.getValueAt(linha, 0)));
        if (this.gerenciadorLancamentos.removerPorId(id)) {
            sincronizarLancamentosNoUsuario();
            salvarAlteracoes();
            atualizarInterface(null, null);
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
            atualizarInterface(null, null);
            JOptionPane.showMessageDialog(this, importados.size() + " lançamento(s) importado(s) com sucesso!");
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
                atualizarInterface(null, null);
                JOptionPane.showMessageDialog(this, importados.size() + " lançamento(s) importado(s) de " + arquivo.getFileName());
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
        BigDecimal cotacao = this.cotacaoMoedaService.buscarCotacaoDolarAtual();
        String status = this.cotacaoMoedaService.isObtidaDaApiOnline() ? "Online - AwesomeAPI" : "Valor Padrão Local";
        this.lblCotacaoTopo.setText("  US$ 1 = R$ " + cotacao + " (" + status + ")  ");
        atualizarInterface(null, null);
    }

    private void acaoEditarUsuario() {
        String novoNome = JOptionPane.showInputDialog(this, "Nome do Usuário:", this.usuarioAtual.getNome());
        if (novoNome != null && !novoNome.isBlank()) {
            String novoEmail = JOptionPane.showInputDialog(this, "E-mail do Usuário:", this.usuarioAtual.getEmail());
            if (novoEmail != null && !novoEmail.isBlank()) {
                this.usuarioAtual.setNome(novoNome);
                this.usuarioAtual.setEmail(novoEmail);
                atualizarTextoUsuarioTopo();
            }
        }
    }

    private void acaoAplicarFiltros() {
        try {
            String ini = this.txtFiltroInicio.getText().trim();
            String fim = this.txtFiltroFim.getText().trim();
            LocalDate dataInicio = ini.isEmpty() ? null : LocalDate.parse(ini);
            LocalDate dataFim = fim.isEmpty() ? null : LocalDate.parse(fim);
            atualizarInterface(dataInicio, dataFim);
        } catch (Exception e) {
            mostrarErro("Filtro Inválido", "Use o formato AAAA-MM-DD para as datas.");
        }
    }

    // ==================== ATUALIZAÇÃO DA TELA ====================

    private void atualizarInterface(LocalDate inicio, LocalDate fim) {
        try {
            // 1. Atualizar Categorias e seus saldos polimórficos
            Categoria selecionadaCombo = (Categoria) this.cmbCategoria.getSelectedItem();
            this.modeloComboCategorias.removeAllElements();
            this.modeloListaCategorias.clear();

            for (Categoria c : this.gerenciadorCategorias.listar()) {
                this.modeloComboCategorias.addElement(c);
                String saldoCat = this.moedaBRL.format(c.calcularSaldo());
                this.modeloListaCategorias.addElement(
                    c.getId() + " - " + c.getNome() + "  |  Saldo: " + saldoCat + " (" + c.getLancamentos().size() + " itens)"
                );
            }
            if (selecionadaCombo != null) {
                this.cmbCategoria.setSelectedItem(selecionadaCombo);
            }

            // 2. Obter lançamentos por período e aplicar filtro de busca/tipo
            List<Lancamento> base = (inicio != null && fim != null)
                ? this.gerenciadorLancamentos.listarPorPeriodo(inicio, fim)
                : this.gerenciadorLancamentos.listar();

            String termoBusca = this.txtBuscaDescricao != null ? this.txtBuscaDescricao.getText().trim().toLowerCase() : "";
            String filtroTipo = this.cmbFiltroTipo != null ? String.valueOf(this.cmbFiltroTipo.getSelectedItem()) : "TODOS";

            List<Lancamento> filtrados = new ArrayList<>();
            for (Lancamento l : base) {
                boolean bateTexto = termoBusca.isEmpty() || l.getDescricao().toLowerCase().contains(termoBusca);
                boolean bateTipo = "TODOS".equalsIgnoreCase(filtroTipo) || l.getTipo().equalsIgnoreCase(filtroTipo);
                if (bateTexto && bateTipo) {
                    filtrados.add(l);
                }
            }

            Relatorio relatorio = GerenciadorLancamentos.calcularRelatorio(filtrados);

            // 3. Atualizar os 4 cards superiores
            this.lblValorReceitas.setText(this.moedaBRL.format(relatorio.getTotalReceitas()));
            this.lblQtdReceitas.setText(relatorio.getQuantidadeReceitas() + " entrada(s)");

            this.lblValorDespesas.setText(this.moedaBRL.format(relatorio.getTotalDespesas()));
            this.lblQtdDespesas.setText(relatorio.getQuantidadeDespesas() + " saída(s)");

            this.lblValorSaldo.setText(this.moedaBRL.format(relatorio.getSaldoTotal()));
            this.lblQtdTotal.setText(relatorio.getQuantidadeLancamentos() + " lançamento(s) exibido(s)");

            BigDecimal saldoEmDolar = this.cotacaoMoedaService.converterReaisParaDolar(relatorio.getSaldoTotal());
            this.lblValorSaldoDolar.setText(this.moedaUSD.format(saldoEmDolar));
            this.lblDetalheDolar.setText("Cotação US$ 1 = R$ " + this.cotacaoMoedaService.getUltimaCotacaoDolar());

            // Atualizar barras de proporção
            BigDecimal somaMovimentado = relatorio.getTotalReceitas().add(relatorio.getTotalDespesas());
            if (somaMovimentado.compareTo(BigDecimal.ZERO) > 0) {
                int percRec = relatorio.getTotalReceitas().multiply(new BigDecimal("100")).divide(somaMovimentado, 0, RoundingMode.HALF_UP).intValue();
                this.barraReceitas.setValue(percRec);
                this.barraDespesas.setValue(100 - percRec);
            } else {
                this.barraReceitas.setValue(0);
                this.barraDespesas.setValue(0);
            }

            this.lblResumoGeral.setText(relatorio.gerarResumoTexto());

            // 4. Preencher Tabela
            this.modeloTabela.setRowCount(0);
            for (Lancamento l : filtrados) {
                String catNome = l.getCategoria() != null ? l.getCategoria().getNome() : "Geral";
                this.modeloTabela.addRow(new Object[]{
                    l.getId(),
                    l.getData().toString(),
                    l.getTipo(),
                    l.getResumo(),
                    catNome,
                    this.moedaBRL.format(l.calcularImpacto())
                });
            }
        } catch (FinanceHubException e) {
            mostrarErro("Erro no Filtro", e.getMessage());
        }
    }

    private void sincronizarLancamentosNoUsuario() {
        this.usuarioAtual.limparLancamentos();
        for (Lancamento l : this.gerenciadorLancamentos.listar()) {
            this.usuarioAtual.adicionarLancamento(l);
        }
        atualizarTextoUsuarioTopo();
    }

    private void atualizarTextoUsuarioTopo() {
        if (this.lblUsuarioTopo != null) {
            this.lblUsuarioTopo.setText(
                "Usuário: " + this.usuarioAtual.getNome() + " (" + this.usuarioAtual.getEmail() + ")"
            );
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

    // ==================== COMPONENTES AUXILIARES DE ESTILO ====================

    private JPanel criarPainelInternoAba() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_CINZA);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
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
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return txt;
    }

    private <T> void estilizarCombo(JComboBox<T> combo) {
        combo.setBackground(BG_CAMPO);
        combo.setForeground(TEXTO_CLARO);
        combo.setFont(FONTE_TEXTO);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
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
            new EmptyBorder(7, 14, 7, 14)
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
            new EmptyBorder(6, 12, 6, 12)
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
            new EmptyBorder(6, 12, 6, 12)
        ));
        return btn;
    }

    private void mostrarErro(String titulo, String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, titulo, JOptionPane.ERROR_MESSAGE);
    }
}
