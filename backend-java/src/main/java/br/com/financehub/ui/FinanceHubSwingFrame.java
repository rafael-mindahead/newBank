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
 * Interface gráfica completa do FinanceHub em Java Swing.
 * Tema escuro financeiro: Preto, Cinza Escuro e Ciano.
 */
public class FinanceHubSwingFrame extends JFrame {

    // Paleta de cores (Preto, Cinza e Ciano)
    private static final Color BG_PRETO = new Color(8, 11, 16);
    private static final Color BG_CAMPO = new Color(13, 18, 26);
    private static final Color CARD_CINZA = new Color(17, 23, 33);
    private static final Color CARD_CINZA_CLARO = new Color(25, 34, 48);
    private static final Color BORDA_CINZA = new Color(40, 52, 70);
    private static final Color TEXTO_CLARO = new Color(238, 244, 250);
    private static final Color TEXTO_CINZA = new Color(142, 158, 180);
    private static final Color CIANO_VIVO = new Color(34, 211, 238);
    private static final Color CIANO_ESCURO = new Color(6, 182, 212);
    private static final Color VERDE_RECEITA = new Color(45, 212, 191);
    private static final Color CORAL_DESPESA = new Color(248, 113, 113);
    private static final Color AMBAR_ALERTA = new Color(251, 191, 36);

    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 19);
    private static final Font FONTE_SECAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_VALOR_CARD = new Font("Segoe UI", Font.BOLD, 21);
    private static final Font FONTE_ROTULO = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font FONTE_TEXTO = new Font("Segoe UI", Font.PLAIN, 13);

    // Domínio, serviços e contrato de persistência
    private final Usuario usuarioAtual;
    private final GerenciadorCategorias gerenciadorCategorias;
    private final GerenciadorLancamentos gerenciadorLancamentos;
    private final ImportadorArquivoService importadorArquivoService;
    private final CotacaoMoedaService cotacaoMoedaService;
    private final RepositorioDados repositorioDados;

    private final NumberFormat moedaBRL = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
    private final NumberFormat moedaUSD = NumberFormat.getCurrencyInstance(Locale.US);

    // Componentes do topo e 5 cards de resumo
    private JLabel lblUsuarioTopo;
    private JLabel lblCotacaoTopo;

    private JLabel lblValorReceitas;
    private JLabel lblQtdReceitas;
    private JProgressBar barraReceitas;

    private JLabel lblValorDespesas;
    private JLabel lblQtdDespesas;
    private JProgressBar barraDespesas;

    private JLabel lblValorSaldo;
    private JLabel lblTaxaPoupanca;
    private JProgressBar barraPoupanca;

    private JLabel lblValorMeta;
    private JLabel lblDetalheMeta;
    private JProgressBar barraMetaGastos;

    private JLabel lblValorSaldoDolar;
    private JLabel lblDetalheCambio;

    private JLabel lblResumoGeral;

    // Navegação lateral customizada (sem bordas brancas do JTabbedPane)
    private CardLayout cardLayoutLateral;
    private JPanel painelConteudoLateral;
    private JButton btnAbaLancamento;
    private JButton btnAbaCategorias;
    private JButton btnAbaImportar;

    // Formulário de lançamento
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

    // Painel inferior de análise visual (Categorias + Insights)
    private JPanel painelBarrasCategorias;
    private JLabel lblInsightMaiorReceita;
    private JLabel lblInsightMaiorDespesa;
    private JLabel lblInsightTicketMedio;
    private JLabel lblInsightStatusOrcamento;

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

    private void construirJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1380, 880);
        setMinimumSize(new Dimension(1180, 760));
        setLocationRelativeTo(null);

        JPanel painelRaiz = new JPanel(new BorderLayout(0, 14));
        painelRaiz.setBackground(BG_PRETO);
        painelRaiz.setBorder(new EmptyBorder(0, 0, 14, 0));
        setContentPane(painelRaiz);

        painelRaiz.add(criarBarraTopo(), BorderLayout.NORTH);

        JPanel miolo = new JPanel(new BorderLayout(0, 14));
        miolo.setBackground(BG_PRETO);
        miolo.setBorder(new EmptyBorder(0, 18, 0, 18));

        miolo.add(criarPainelCincoCards(), BorderLayout.NORTH);
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

        this.lblCotacaoTopo = new JLabel(
            "  USD R$ " + this.cotacaoMoedaService.getUltimaCotacaoDolar() +
            " | EUR R$ " + this.cotacaoMoedaService.getUltimaCotacaoEuro() + "  "
        );
        this.lblCotacaoTopo.setOpaque(true);
        this.lblCotacaoTopo.setBackground(CARD_CINZA_CLARO);
        this.lblCotacaoTopo.setForeground(CIANO_VIVO);
        this.lblCotacaoTopo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        this.lblCotacaoTopo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CIANO_ESCURO, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        JButton btnAtualizarCotacao = criarBotaoSecundario("Atualizar Câmbio (API)");
        btnAtualizarCotacao.addActionListener(e -> acaoConsultarApiCotacao());

        JButton btnMeta = criarBotaoSecundario("Definir Meta Mensal");
        btnMeta.addActionListener(e -> acaoDefinirMetaMensal());

        JButton btnEditarUsuario = criarBotaoSecundario("Perfil do Usuário");
        btnEditarUsuario.addActionListener(e -> acaoEditarUsuario());

        JButton btnImportar = criarBotaoSecundario("Importar CSV/TXT");
        btnImportar.addActionListener(e -> acaoImportarArquivoDisco());

        JButton btnExportar = criarBotaoCiano("Exportar CSV");
        btnExportar.addActionListener(e -> acaoExportarArquivoDisco());

        direita.add(this.lblCotacaoTopo);
        direita.add(btnAtualizarCotacao);
        direita.add(btnMeta);
        direita.add(btnEditarUsuario);
        direita.add(btnImportar);
        direita.add(btnExportar);

        barra.add(esquerda, BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);
        return barra;
    }

    private JPanel criarPainelCincoCards() {
        JPanel grid = new JPanel(new GridLayout(1, 5, 12, 0));
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
        this.lblTaxaPoupanca = new JLabel("Taxa de poupança: 0%");
        this.barraPoupanca = criarBarraProgresso(CIANO_VIVO);
        grid.add(criarCardDashboard("SALDO LÍQUIDO (R$)", this.lblValorSaldo, this.lblTaxaPoupanca, this.barraPoupanca, CIANO_VIVO));

        this.lblValorMeta = new JLabel("R$ 3.500,00");
        this.lblDetalheMeta = new JLabel("0% do limite utilizado");
        this.barraMetaGastos = criarBarraProgresso(CIANO_VIVO);
        grid.add(criarCardDashboard("META DE GASTOS MENSAL", this.lblValorMeta, this.lblDetalheMeta, this.barraMetaGastos, AMBAR_ALERTA));

        this.lblValorSaldoDolar = new JLabel("US$ 0.00");
        this.lblDetalheCambio = new JLabel("€ 0.00 (API Externa)");
        JProgressBar barraCambio = criarBarraProgresso(CIANO_ESCURO);
        barraCambio.setValue(100);
        grid.add(criarCardDashboard("SALDO EM USD / EUR (API)", this.lblValorSaldoDolar, this.lblDetalheCambio, barraCambio, CIANO_VIVO));

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
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(TEXTO_CINZA);
        lblTitulo.setFont(FONTE_ROTULO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblValor.setForeground(corDestaque);
        lblValor.setFont(FONTE_VALOR_CARD);
        lblValor.setBorder(new EmptyBorder(5, 0, 5, 0));
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
        JPanel painel = new JPanel(new BorderLayout(16, 0));
        painel.setOpaque(false);

        painel.add(criarColunaEsquerdaComAbasCustomizadas(), BorderLayout.WEST);
        painel.add(criarColunaDireitaCompleta(), BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarColunaEsquerdaComAbasCustomizadas() {
        JPanel containerEsquerda = new JPanel(new BorderLayout(0, 10));
        containerEsquerda.setPreferredSize(new Dimension(385, 560));
        containerEsquerda.setBackground(CARD_CINZA);
        containerEsquerda.setBorder(new LineBorder(BORDA_CINZA, 1, true));

        // Barra de botões de navegação (substitui JTabbedPane sem bordas brancas)
        JPanel barraNavegacao = new JPanel(new GridLayout(1, 3, 6, 0));
        barraNavegacao.setBackground(BG_CAMPO);
        barraNavegacao.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDA_CINZA),
            new EmptyBorder(8, 8, 8, 8)
        ));

        this.btnAbaLancamento = criarBotaoAba("1. Lançamento", true);
        this.btnAbaCategorias = criarBotaoAba("2. Categorias", false);
        this.btnAbaImportar = criarBotaoAba("3. Lote CSV", false);

        this.btnAbaLancamento.addActionListener(e -> alternarAbaLateral("ABA_LANCAMENTO", this.btnAbaLancamento));
        this.btnAbaCategorias.addActionListener(e -> alternarAbaLateral("ABA_CATEGORIAS", this.btnAbaCategorias));
        this.btnAbaImportar.addActionListener(e -> alternarAbaLateral("ABA_IMPORTAR", this.btnAbaImportar));

        barraNavegacao.add(this.btnAbaLancamento);
        barraNavegacao.add(this.btnAbaCategorias);
        barraNavegacao.add(this.btnAbaImportar);

        this.cardLayoutLateral = new CardLayout();
        this.painelConteudoLateral = new JPanel(this.cardLayoutLateral);
        this.painelConteudoLateral.setBackground(CARD_CINZA);

        this.painelConteudoLateral.add(criarAbaFormularioLancamento(), "ABA_LANCAMENTO");
        this.painelConteudoLateral.add(criarAbaCategorias(), "ABA_CATEGORIAS");
        this.painelConteudoLateral.add(criarAbaImportacaoCsv(), "ABA_IMPORTAR");

        containerEsquerda.add(barraNavegacao, BorderLayout.NORTH);
        containerEsquerda.add(this.painelConteudoLateral, BorderLayout.CENTER);
        return containerEsquerda;
    }

    private JButton criarBotaoAba(String texto, boolean ativo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        aplicarEstiloBotaoAba(btn, ativo);
        return btn;
    }

    private void aplicarEstiloBotaoAba(JButton btn, boolean ativo) {
        if (ativo) {
            btn.setBackground(CIANO_VIVO);
            btn.setForeground(BG_PRETO);
            btn.setBorder(new EmptyBorder(8, 6, 8, 6));
        } else {
            btn.setBackground(CARD_CINZA_CLARO);
            btn.setForeground(TEXTO_CINZA);
            btn.setBorder(new LineBorder(BORDA_CINZA, 1));
        }
    }

    private void alternarAbaLateral(String nomeCard, JButton botaoAtivo) {
        this.cardLayoutLateral.show(this.painelConteudoLateral, nomeCard);
        aplicarEstiloBotaoAba(this.btnAbaLancamento, botaoAtivo == this.btnAbaLancamento);
        aplicarEstiloBotaoAba(this.btnAbaCategorias, botaoAtivo == this.btnAbaCategorias);
        aplicarEstiloBotaoAba(this.btnAbaImportar, botaoAtivo == this.btnAbaImportar);
    }

    private JPanel criarAbaFormularioLancamento() {
        JPanel card = criarPainelInternoAba();

        this.lblModoFormulario = new JLabel("Cadastrar Novo Lançamento");
        this.lblModoFormulario.setForeground(CIANO_VIVO);
        this.lblModoFormulario.setFont(FONTE_SECAO);
        card.add(criarBlocoTituloComBarraCiano(this.lblModoFormulario));
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
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
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

        card.add(criarRotulo("DATA (AAAA-MM-DD)"));
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

        card.add(Box.createVerticalStrut(14));
        JLabel dicaDuploClique = new JLabel("Dica: Dê duplo clique em uma linha da tabela para editar.");
        dicaDuploClique.setForeground(TEXTO_CINZA);
        dicaDuploClique.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        dicaDuploClique.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(dicaDuploClique);

        return card;
    }

    private JPanel criarAbaCategorias() {
        JPanel card = criarPainelInternoAba();

        JLabel titulo = new JLabel("Gerenciar Categorias e Saldos");
        titulo.setForeground(CIANO_VIVO);
        titulo.setFont(FONTE_SECAO);
        card.add(criarBlocoTituloComBarraCiano(titulo));
        card.add(Box.createVerticalStrut(14));

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

        card.add(criarRotulo("CATEGORIAS E SALDO INDIVIDUAL (POLIMÓRFICO)"));
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

        JLabel titulo = new JLabel("Importar Lote CSV ou TXT");
        titulo.setForeground(CIANO_VIVO);
        titulo.setFont(FONTE_SECAO);
        card.add(criarBlocoTituloComBarraCiano(titulo));
        card.add(Box.createVerticalStrut(12));

        JLabel instrucao = new JLabel("Padrão: TIPO;DESCRICAO;VALOR;DATA;CATEGORIA");
        instrucao.setForeground(TEXTO_CINZA);
        instrucao.setFont(FONTE_ROTULO);
        instrucao.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(instrucao);
        card.add(Box.createVerticalStrut(8));

        this.txtAreaCsv = new JTextArea(11, 25);
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

    private JPanel criarColunaDireitaCompleta() {
        JPanel colunaDireita = new JPanel(new BorderLayout(0, 14));
        colunaDireita.setOpaque(false);

        colunaDireita.add(criarPainelTabelaSuperior(), BorderLayout.CENTER);
        colunaDireita.add(criarPainelInferiorAnalitico(), BorderLayout.SOUTH);
        return colunaDireita;
    }

    private JPanel criarPainelTabelaSuperior() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(CARD_CINZA);
        painel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(14, 18, 14, 18)
        ));

        // Topo com título, atalhos rápidos e filtros
        JPanel topo = new JPanel(new BorderLayout(8, 8));
        topo.setOpaque(false);

        JPanel linhaTituloEAtalhos = new JPanel(new BorderLayout());
        linhaTituloEAtalhos.setOpaque(false);

        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.setOpaque(false);

        JLabel lblHist = new JLabel("Histórico de Movimentações Financeiras");
        lblHist.setForeground(CIANO_VIVO);
        lblHist.setFont(FONTE_SECAO);

        this.lblResumoGeral = new JLabel("Resumo consolidado");
        this.lblResumoGeral.setForeground(TEXTO_CINZA);
        this.lblResumoGeral.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        titulos.add(criarBlocoTituloComBarraCiano(lblHist));
        titulos.add(Box.createVerticalStrut(3));
        titulos.add(this.lblResumoGeral);

        JPanel atalhosRapidos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        atalhosRapidos.setOpaque(false);

        JButton btnRapidoMesAtual = criarBotaoSecundario("Mês Atual");
        btnRapidoMesAtual.addActionListener(e -> {
            LocalDate hoje = LocalDate.now();
            this.txtFiltroInicio.setText(hoje.withDayOfMonth(1).toString());
            this.txtFiltroFim.setText(hoje.withDayOfMonth(hoje.lengthOfMonth()).toString());
            acaoAplicarFiltros();
        });

        JButton btnRapidoReceitas = criarBotaoSecundario("Só Receitas");
        btnRapidoReceitas.addActionListener(e -> {
            this.cmbFiltroTipo.setSelectedItem("RECEITA");
            acaoAplicarFiltros();
        });

        JButton btnRapidoDespesas = criarBotaoSecundario("Só Despesas");
        btnRapidoDespesas.addActionListener(e -> {
            this.cmbFiltroTipo.setSelectedItem("DESPESA");
            acaoAplicarFiltros();
        });

        atalhosRapidos.add(btnRapidoMesAtual);
        atalhosRapidos.add(btnRapidoReceitas);
        atalhosRapidos.add(btnRapidoDespesas);

        linhaTituloEAtalhos.add(titulos, BorderLayout.WEST);
        linhaTituloEAtalhos.add(atalhosRapidos, BorderLayout.EAST);

        JPanel barraFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        barraFiltros.setOpaque(false);

        this.txtBuscaDescricao = criarCampoTexto("");
        this.txtBuscaDescricao.setPreferredSize(new Dimension(150, 32));

        this.cmbFiltroTipo = new JComboBox<>(new String[]{"TODOS", "RECEITA", "DESPESA"});
        estilizarCombo(this.cmbFiltroTipo);
        this.cmbFiltroTipo.setPreferredSize(new Dimension(115, 32));

        this.txtFiltroInicio = criarCampoTexto("");
        this.txtFiltroInicio.setPreferredSize(new Dimension(100, 32));

        this.txtFiltroFim = criarCampoTexto("");
        this.txtFiltroFim.setPreferredSize(new Dimension(100, 32));

        JButton btnAplicarFiltro = criarBotaoCiano("Filtrar");
        btnAplicarFiltro.addActionListener(e -> acaoAplicarFiltros());

        JButton btnLimparFiltro = criarBotaoSecundario("Limpar Tudo");
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

        topo.add(linhaTituloEAtalhos, BorderLayout.NORTH);
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
        this.tabelaLancamentos.setRowHeight(32);
        this.tabelaLancamentos.setFont(FONTE_TEXTO);
        this.tabelaLancamentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tabelaLancamentos.setFillsViewportHeight(true);

        // Duplo clique carrega para edição
        this.tabelaLancamentos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabelaLancamentos.getSelectedRow() >= 0) {
                    acaoCarregarLancamentoParaEdicao();
                }
            }
        });

        JTableHeader header = this.tabelaLancamentos.getTableHeader();
        header.setBackground(CARD_CINZA_CLARO);
        header.setForeground(CIANO_VIVO);
        header.setFont(FONTE_ROTULO);
        header.setPreferredSize(new Dimension(100, 34));

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

        // Rodapé com botões de Duplicar, Editar e Excluir
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rodape.setOpaque(false);

        JButton btnDuplicar = criarBotaoSecundario("Duplicar Selecionado");
        btnDuplicar.addActionListener(e -> acaoDuplicarLancamentoSelecionado());

        JButton btnEditar = criarBotaoSecundario("Editar Selecionado");
        btnEditar.addActionListener(e -> acaoCarregarLancamentoParaEdicao());

        JButton btnExcluir = criarBotaoPerigo("Excluir Selecionado");
        btnExcluir.addActionListener(e -> acaoExcluirLancamentoSelecionado());

        rodape.add(btnDuplicar);
        rodape.add(btnEditar);
        rodape.add(btnExcluir);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(scrollTabela, BorderLayout.CENTER);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarPainelInferiorAnalitico() {
        JPanel painelAnalitico = new JPanel(new GridLayout(1, 2, 14, 0));
        painelAnalitico.setOpaque(false);
        painelAnalitico.setPreferredSize(new Dimension(100, 195));

        // Card Esquerdo: Gráfico de Barras por Categoria
        JPanel cardGraficoCat = new JPanel(new BorderLayout(0, 8));
        cardGraficoCat.setBackground(CARD_CINZA);
        cardGraficoCat.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitCat = new JLabel("Distribuição e Saldo por Categoria (Polimorfismo)");
        lblTitCat.setForeground(CIANO_VIVO);
        lblTitCat.setFont(FONTE_SECAO);
        cardGraficoCat.add(criarBlocoTituloComBarraCiano(lblTitCat), BorderLayout.NORTH);

        this.painelBarrasCategorias = new JPanel();
        this.painelBarrasCategorias.setLayout(new BoxLayout(this.painelBarrasCategorias, BoxLayout.Y_AXIS));
        this.painelBarrasCategorias.setBackground(CARD_CINZA);

        JScrollPane scrollBarras = new JScrollPane(this.painelBarrasCategorias);
        scrollBarras.setBorder(null);
        scrollBarras.getViewport().setBackground(CARD_CINZA);
        cardGraficoCat.add(scrollBarras, BorderLayout.CENTER);

        // Card Direito: Indicadores e Insights Automáticos
        JPanel cardInsights = new JPanel(new BorderLayout(0, 8));
        cardInsights.setBackground(CARD_CINZA);
        cardInsights.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDA_CINZA, 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblTitInsights = new JLabel("Indicadores e Diagnóstico do Período");
        lblTitInsights.setForeground(CIANO_VIVO);
        lblTitInsights.setFont(FONTE_SECAO);
        cardInsights.add(criarBlocoTituloComBarraCiano(lblTitInsights), BorderLayout.NORTH);

        JPanel gradeInsights = new JPanel(new GridLayout(2, 2, 10, 8));
        gradeInsights.setOpaque(false);

        this.lblInsightMaiorReceita = new JLabel("-");
        this.lblInsightMaiorDespesa = new JLabel("-");
        this.lblInsightTicketMedio = new JLabel("-");
        this.lblInsightStatusOrcamento = new JLabel("-");

        gradeInsights.add(criarMiniCardInsight("MAIOR RECEITA", this.lblInsightMaiorReceita, VERDE_RECEITA));
        gradeInsights.add(criarMiniCardInsight("MAIOR DESPESA", this.lblInsightMaiorDespesa, CORAL_DESPESA));
        gradeInsights.add(criarMiniCardInsight("TICKET MÉDIO POR ITEM", this.lblInsightTicketMedio, CIANO_VIVO));
        gradeInsights.add(criarMiniCardInsight("DIAGNÓSTICO DO ORÇAMENTO", this.lblInsightStatusOrcamento, CIANO_VIVO));

        cardInsights.add(gradeInsights, BorderLayout.CENTER);

        painelAnalitico.add(cardGraficoCat);
        painelAnalitico.add(cardInsights);
        return painelAnalitico;
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
            this.lblModoFormulario.setText("Editando Lançamento #" + l.getId());
            this.cmbTipo.setSelectedItem(l.getTipo());
            this.txtDescricao.setText(l.getDescricao());
            this.txtValor.setText(l.getValor().toPlainString());
            this.txtData.setText(l.getData().toString());
            if (l.getCategoria() != null) {
                this.cmbCategoria.setSelectedItem(l.getCategoria());
            }
            alternarAbaLateral("ABA_LANCAMENTO", this.btnAbaLancamento);
        } catch (FinanceHubException e) {
            mostrarErro("Erro ao Carregar", e.getMessage());
        }
    }

    private void acaoDuplicarLancamentoSelecionado() {
        int linha = this.tabelaLancamentos.getSelectedRow();
        if (linha < 0) {
            mostrarErro("Seleção Necessária", "Selecione um lançamento na tabela para duplicar.");
            return;
        }
        try {
            long id = Long.parseLong(String.valueOf(this.modeloTabela.getValueAt(linha, 0)));
            Lancamento original = this.gerenciadorLancamentos.buscarPorId(id);

            Lancamento copia = original.isReceita()
                ? new Receita(null, original.getDescricao() + " (Cópia)", original.getValor(), LocalDate.now(), original.getCategoria(), this.usuarioAtual.getId())
                : new Despesa(null, original.getDescricao() + " (Cópia)", original.getValor(), LocalDate.now(), original.getCategoria(), this.usuarioAtual.getId());

            registrarNovoLancamentoInterno(copia);
            salvarAlteracoes();
            atualizarInterface(null, null);
        } catch (FinanceHubException e) {
            mostrarErro("Erro ao Duplicar", e.getMessage());
        }
    }

    private void limparFormularioLancamento() {
        this.idEmEdicao = null;
        this.lblModoFormulario.setText("Cadastrar Novo Lançamento");
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
        BigDecimal usd = this.cotacaoMoedaService.buscarCotacaoDolarAtual();
        BigDecimal eur = this.cotacaoMoedaService.getUltimaCotacaoEuro();
        String status = this.cotacaoMoedaService.isObtidaDaApiOnline() ? "AwesomeAPI Online" : "Padrão Local";
        this.lblCotacaoTopo.setText("  USD R$ " + usd + " | EUR R$ " + eur + " (" + status + ")  ");
        atualizarInterface(null, null);
    }

    private void acaoDefinirMetaMensal() {
        String entrada = JOptionPane.showInputDialog(
            this,
            "Informe o limite máximo de despesas mensais (R$):",
            this.usuarioAtual.getMetaDespesaMensal().toPlainString()
        );
        if (entrada != null && !entrada.isBlank()) {
            try {
                BigDecimal novaMeta = new BigDecimal(entrada.trim().replace(",", ".")).setScale(2, RoundingMode.HALF_UP);
                this.usuarioAtual.setMetaDespesaMensal(novaMeta);
                atualizarInterface(null, null);
            } catch (Exception e) {
                mostrarErro("Meta Inválida", "Informe um valor numérico positivo.");
            }
        }
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

    // ==================== ATUALIZAÇÃO DA TELA E ANÁLISES ====================

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

            // 2. Filtrar lançamentos
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

            // 3. Atualizar os 5 Cards Superiores
            this.lblValorReceitas.setText(this.moedaBRL.format(relatorio.getTotalReceitas()));
            this.lblQtdReceitas.setText(relatorio.getQuantidadeReceitas() + " entrada(s) no período");

            this.lblValorDespesas.setText(this.moedaBRL.format(relatorio.getTotalDespesas()));
            this.lblQtdDespesas.setText(relatorio.getQuantidadeDespesas() + " saída(s) no período");

            this.lblValorSaldo.setText(this.moedaBRL.format(relatorio.getSaldoTotal()));

            int taxaPoupanca = 0;
            if (relatorio.getTotalReceitas().compareTo(BigDecimal.ZERO) > 0 && relatorio.getSaldoTotal().compareTo(BigDecimal.ZERO) > 0) {
                taxaPoupanca = relatorio.getSaldoTotal()
                    .multiply(new BigDecimal("100"))
                    .divide(relatorio.getTotalReceitas(), 0, RoundingMode.HALF_UP)
                    .intValue();
            }
            this.lblTaxaPoupanca.setText("Taxa de poupança: " + Math.min(100, Math.max(0, taxaPoupanca)) + "% das receitas");
            this.barraPoupanca.setValue(Math.min(100, Math.max(0, taxaPoupanca)));

            // Card Meta Mensal
            BigDecimal meta = this.usuarioAtual.getMetaDespesaMensal();
            this.lblValorMeta.setText(this.moedaBRL.format(meta));
            int percMeta = meta.compareTo(BigDecimal.ZERO) > 0
                ? relatorio.getTotalDespesas().multiply(new BigDecimal("100")).divide(meta, 0, RoundingMode.HALF_UP).intValue()
                : 0;
            this.lblDetalheMeta.setText(percMeta + "% do limite utilizado");
            this.barraMetaGastos.setValue(Math.min(100, percMeta));
            this.barraMetaGastos.setForeground(percMeta > 100 ? CORAL_DESPESA : (percMeta >= 80 ? AMBAR_ALERTA : VERDE_RECEITA));

            // Card Câmbio Internacional
            BigDecimal saldoUSD = this.cotacaoMoedaService.converterReaisParaDolar(relatorio.getSaldoTotal());
            BigDecimal saldoEUR = this.cotacaoMoedaService.converterReaisParaEuro(relatorio.getSaldoTotal());
            this.lblValorSaldoDolar.setText(this.moedaUSD.format(saldoUSD));
            this.lblDetalheCambio.setText(String.format("Euro: € %.2f (API Externa)", saldoEUR));

            // Barras de proporção Receitas x Despesas
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

            // 5. Atualizar Painel Analítico Inferior (Gráfico de Categorias e Insights)
            atualizarPainelAnaliticoInferior(filtrados, relatorio);
        } catch (FinanceHubException e) {
            mostrarErro("Erro no Filtro", e.getMessage());
        }
    }

    private void atualizarPainelAnaliticoInferior(List<Lancamento> filtrados, Relatorio relatorio) {
        // Atualizar barras por categoria
        this.painelBarrasCategorias.removeAll();
        BigDecimal maiorModuloCategoria = BigDecimal.ONE;
        for (Categoria c : this.gerenciadorCategorias.listar()) {
            BigDecimal mod = c.calcularSaldo().abs();
            if (mod.compareTo(maiorModuloCategoria) > 0) {
                maiorModuloCategoria = mod;
            }
        }

        for (Categoria c : this.gerenciadorCategorias.listar()) {
            BigDecimal saldoCat = c.calcularSaldo();
            int perc = saldoCat.abs().multiply(new BigDecimal("100")).divide(maiorModuloCategoria, 0, RoundingMode.HALF_UP).intValue();

            JPanel linhaCat = new JPanel(new BorderLayout(10, 0));
            linhaCat.setOpaque(false);
            linhaCat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
            linhaCat.setBorder(new EmptyBorder(2, 0, 4, 4));

            JLabel lblNome = new JLabel(c.getNome() + " (" + c.getLancamentos().size() + ")");
            lblNome.setPreferredSize(new Dimension(130, 20));
            lblNome.setForeground(TEXTO_CLARO);
            lblNome.setFont(new Font("Segoe UI", Font.BOLD, 12));

            JProgressBar barraCat = new JProgressBar(0, 100);
            barraCat.setValue(perc);
            barraCat.setBackground(BG_CAMPO);
            barraCat.setForeground(saldoCat.compareTo(BigDecimal.ZERO) >= 0 ? VERDE_RECEITA : CORAL_DESPESA);
            barraCat.setBorderPainted(false);

            JLabel lblSaldoCat = new JLabel(this.moedaBRL.format(saldoCat), SwingConstants.RIGHT);
            lblSaldoCat.setPreferredSize(new Dimension(110, 20));
            lblSaldoCat.setForeground(saldoCat.compareTo(BigDecimal.ZERO) >= 0 ? VERDE_RECEITA : CORAL_DESPESA);
            lblSaldoCat.setFont(new Font("Segoe UI", Font.BOLD, 12));

            linhaCat.add(lblNome, BorderLayout.WEST);
            linhaCat.add(barraCat, BorderLayout.CENTER);
            linhaCat.add(lblSaldoCat, BorderLayout.EAST);
            this.painelBarrasCategorias.add(linhaCat);
        }
        this.painelBarrasCategorias.revalidate();
        this.painelBarrasCategorias.repaint();

        // Calcular Insights
        Lancamento maiorRec = null;
        Lancamento maiorDesp = null;
        BigDecimal somaValores = BigDecimal.ZERO;

        for (Lancamento l : filtrados) {
            somaValores = somaValores.add(l.getValor());
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

        this.lblInsightMaiorReceita.setText(
            maiorRec != null ? maiorRec.getDescricao() + " (" + this.moedaBRL.format(maiorRec.getValor()) + ")" : "Nenhuma receita"
        );
        this.lblInsightMaiorDespesa.setText(
            maiorDesp != null ? maiorDesp.getDescricao() + " (" + this.moedaBRL.format(maiorDesp.getValor()) + ")" : "Nenhuma despesa"
        );

        if (!filtrados.isEmpty()) {
            BigDecimal media = somaValores.divide(BigDecimal.valueOf(filtrados.size()), 2, RoundingMode.HALF_UP);
            this.lblInsightTicketMedio.setText(this.moedaBRL.format(media) + " por lançamento");
        } else {
            this.lblInsightTicketMedio.setText("R$ 0,00");
        }

        if (relatorio.getSaldoTotal().compareTo(BigDecimal.ZERO) < 0) {
            this.lblInsightStatusOrcamento.setText("Déficit — Despesas superam receitas");
            this.lblInsightStatusOrcamento.setForeground(CORAL_DESPESA);
        } else if (relatorio.getTotalDespesas().compareTo(this.usuarioAtual.getMetaDespesaMensal()) > 0) {
            this.lblInsightStatusOrcamento.setText("Alerta — Limite mensal ultrapassado");
            this.lblInsightStatusOrcamento.setForeground(AMBAR_ALERTA);
        } else {
            this.lblInsightStatusOrcamento.setText("Orçamento Saudável — Dentro da meta");
            this.lblInsightStatusOrcamento.setForeground(VERDE_RECEITA);
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

    private JPanel criarPainelInternoAba() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_CINZA);
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
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
        combo.setFocusable(false);
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
