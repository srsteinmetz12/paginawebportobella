package views;

import dao.ClienteDAO;
import dao.EntregasDAO;
import dao.ProdutoDAO;
import dao.SacolaDAO;
import dao.TrocasDAO;
import dao.VendasDAO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import models.Cliente;
import models.Entregas;
import models.Produto;
import models.Sacola;
import models.Trocas;
import models.Vendas;
import util.ConfigLoader;
import util.MensagemSistema;
import util.ValorMonetarioUtil;

public class TelaFinanceiro extends javax.swing.JFrame {
    
    int id;
    private final LocalDate dataDoDia = LocalDate.now();
    public static String codPeca;
    public static String idVenda; 
    public static String dataVenda;
    public static String origemVenda;
    public static String tipoPago;
    public static String obs;
    public static String status;
//    public static Double frete = 0.0;
    public static int idTrocas;
    public static String nomeCliente;
    public static String cliente;
    public List<Produto> listaCodigos;
    private Timer buscaTimer;
    private final boolean itemSelecionado = false;
    private Timer timerBusca;
    
    VendasDAO vdao = new VendasDAO();
    Vendas v = new Vendas();
    Entregas e = new Entregas();
    EntregasDAO edao = new EntregasDAO();
    TrocasDAO tdao = new TrocasDAO();
    Trocas t = new Trocas();
    SacolaDAO sdao = new SacolaDAO();
    Sacola s = new Sacola();
    Produto p = new Produto();
    ProdutoDAO pdao = new ProdutoDAO();
    ClienteDAO cdao = new ClienteDAO();
    Cliente c = new Cliente();
    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
    private JPopupMenu popupSugestoes;
    private JList<String> listaSugestoes;
    private DefaultListModel<String> listModel;
    private List<String> listaCacheClientes = new ArrayList<>();
    String produto_servico_loja = "PRODUTO/SERVICO_LOJA";
    String venda_web = "VENDA WEB";
    String venda_loja = "VENDA LOJA";
    String entregue = "ENTREGUE";
    String entrega_endereco = "ENTREGA_ENDERECO";
    String retire_loja = "RETIRE_LOJA";
    String despesa = "DESPESA";
    String em_seperacao = "EM_SEPARACAO";
    String web = "WEB";
    String loja = "LOJA";
    int sem_codigo = 0000;
    int anoAtual = java.time.Year.now().getValue();
       
    @SuppressWarnings("LeakingThisInConstructor")
    public TelaFinanceiro() {
        this.setUndecorated(true);
        java.awt.Color grafiteProfundoPop = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaroPop    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuroPop     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuroPop      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color cinzaBordasPop     = new java.awt.Color(70, 70, 70);

        // Força a receita de bolo do FlatLaf para qualquer lista suspensa ou popup de texto leve
        javax.swing.UIManager.put("Popup.background", grafiteClaroPop);
        javax.swing.UIManager.put("Popup.border", javax.swing.BorderFactory.createLineBorder(cinzaBordasPop, 1));
        
        // Pinta a lista interna que exibe os nomes dos clientes
        javax.swing.UIManager.put("List.background", grafiteClaroPop);
        javax.swing.UIManager.put("List.foreground", brancoPuroPop);
        javax.swing.UIManager.put("List.selectionBackground", douradoOuroPop);   // Fundo DOURADO no nome selecionado!
        javax.swing.UIManager.put("List.selectionForeground", grafiteProfundoPop); // Letra escura no selecionado
        
        // Remove a barra de rolagem branca fosca clássica do Windows antigo
        javax.swing.UIManager.put("ScrollPane.background", grafiteProfundoPop);
        javax.swing.UIManager.put("ScrollBar.background", grafiteProfundoPop);
        javax.swing.UIManager.put("ScrollBar.thumb", grafiteClaroPop);
        javax.swing.UIManager.put("ScrollBar.track", grafiteProfundoPop);
        
        initComponents();
        ValorMonetarioUtil.aplicarMascaraEmCampos(
            campoValorVenda
        );
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
        
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color grafiteFundoGrid = new java.awt.Color(35, 35, 35);    // #232323
        java.awt.Color cinzaBordasGrid = new java.awt.Color(60, 60, 60);    // Linhas da tabela
        java.awt.Color cinzaLinhas     = new java.awt.Color(51, 51, 51);    // Separadores discretos
        java.awt.Color vermelhoSair      = new java.awt.Color(160, 40, 40);   // Hover do X
        java.awt.Color verdePainel     = new java.awt.Color(34, 112, 63);   // Verde do painel vendas

        com.itextpdf.text.Font fS = new com.itextpdf.text.Font();
        // --- 🔥 PADRONIZAÇÃO DO CABEÇALHO (TEMA GRAFITE ESCURO PREMIUM) ---
        // --- 🔥 CORREÇÃO DEFINITIVA: CABEÇALHO CINZA CLARO TRIDIMENSIONAL (IGUAL CRÉDITOS) ---
        javax.swing.table.TableCellRenderer renderizadorMoviVendas = new javax.swing.table.TableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object v, boolean s, boolean f, int r, int c) {
                // Criamos o painel customizado para desenhar a célula do cabeçalho do zero
                javax.swing.JPanel painelCelula = new javax.swing.JPanel(new java.awt.BorderLayout());

                // 🔥 A COR CORRETA: Cinza claro tridimensional idêntico ao da imagem circular
                painelCelula.setBackground(new java.awt.Color(215, 217, 220)); 

                // Garante o texto limpo e convertido para MAIÚSCULAS
                String textoColuna = (v == null) ? "" : v.toString().trim().toUpperCase();

                javax.swing.JLabel labelTexto = new javax.swing.JLabel(textoColuna);
                labelTexto.setForeground(new java.awt.Color(30, 30, 30)); // Texto escuro forte de alta leitura
                labelTexto.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                labelTexto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT); // Alinhado à esquerda igual ao de baixo

                // Espaçamento interno de 5 pixels para o texto respirar
                labelTexto.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
                painelCelula.add(labelTexto, java.awt.BorderLayout.CENTER);

                // 🔥 O SEGREDO DO VISUAL: Borda chanfrada que cria o relevo e as linhas verticais separadoras
                painelCelula.setBorder(javax.swing.BorderFactory.createBevelBorder(
                    javax.swing.border.BevelBorder.RAISED, 
                    new java.awt.Color(245, 245, 245), // Linha clara (brilho superior)
                    new java.awt.Color(160, 160, 160)  // Linha escura (sombra divisória lateral)
                ));

                return painelCelula;
            }
        };
       // --- 2. FORÇAR A PINTURA DE TODOS OS PAINÉIS DE FUNDO ORIGINAIS ---
        this.getContentPane().setBackground(grafiteProfundo);
        jPanel1.setBackground(grafiteProfundo);   jPanel1.setOpaque(true);
        jPanel3.setBackground(grafiteProfundo);   jPanel3.setOpaque(true); // Aba Vendas
        jPanel5.setBackground(grafiteProfundo);   jPanel5.setOpaque(true); // Painel Base
        jPanel7.setBackground(grafiteProfundo);   jPanel7.setOpaque(true); // Aba Contas Fixas
        
        abaVendas.setBorder(null);
        // --- 3. ⚖️ HIERARQUIA DE FONTES OPERACIONAIS EM BRANCO PURO ---
        javax.swing.JLabel[] labelsBrancas = {
            jLabel1, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, jLabel14, 
            jLabel15, jLabel16, jLabel17, jLabel18, jLabel29, jLabel30
        };
        for (javax.swing.JLabel lbl : labelsBrancas) {
            lbl.setForeground(brancoPuro);
            lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        }
        
        buttonRetireLoja.setForeground(douradoOuro);       buttonRetireLoja.setOpaque(false);
        buttonEntregaEndereco.setForeground(douradoOuro);   buttonEntregaEndereco.setOpaque(false);
//        labelValorFrete.setForeground(douradoOuro);
        // --- 4. CAMPOS DE ENTRADA DARK (JTextFields e JComboBox) ---
        javax.swing.JTextField[] todosCamposTexto = {
            campoIdVenda, campoDataVenda, campoCodPeca, campoNomeCliente, 
            campoObsVendas, campoValorVenda, campoDataContaFixa, 
            campoOrigemContaFixa, campoDescricaoContaFixa, campoValorContaFixa, campoCredorContaFixa
        };
        for (javax.swing.JTextField txt : todosCamposTexto) {
            txt.setBackground(grafiteClaro);
            txt.setForeground(brancoPuro);
            txt.setCaretColor(brancoPuro);
            txt.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        }       
        comboOrigemVenda.setBackground(grafiteClaro);   comboOrigemVenda.setForeground(brancoPuro);
        comboTipoPagamento.setBackground(grafiteClaro); comboTipoPagamento.setForeground(brancoPuro);
        
        tabelaVendas.getTableHeader().setDefaultRenderer(renderizadorMoviVendas);
        tabelaVendas.setBackground(grafiteClaro);         tabelaVendas.setForeground(brancoPuro);
        tabelaVendas.setGridColor(cinzaBordasGrid);           tabelaVendas.setRowHeight(22);
        jScrollPane5.getViewport().setBackground(grafiteProfundo);

        jTable1.getTableHeader().setDefaultRenderer(renderizadorMoviVendas);
        jTable1.setBackground(grafiteFundoGrid);              jTable1.setForeground(brancoPuro);
        jTable1.setGridColor(cinzaBordasGrid);                jTable1.setRowHeight(22);
        jScrollPane2.getViewport().setBackground(grafiteProfundo);

        // --- 6. 🔥 FIXAÇÃO DAS ABAS DO FINANCEIRO VIA RENDERIZADOR CUSTOMIZADO MATRIZ ---
        abaVendas.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(java.awt.Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                if (isSelected) {
                    g.setColor(new java.awt.Color(212, 175, 55)); // Aba clicada vira Dourado Ouro
                } else {
                    g.setColor(new java.awt.Color(45, 45, 45));    // Aba de fundo vira Grafite Claro
                }
                g.fillRect(x, y, w, h);
            }
            @Override
            protected void paintContentBorder(java.awt.Graphics g, int tabPlacement, int selectedIndex) {
                g.setColor(new java.awt.Color(28, 28, 28)); g.drawRect(0, 0, 0, 0);
            }
        });
        abaVendas.setBackground(grafiteClaro);             abaVendas.setForeground(brancoPuro);
        abaVendas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        abaVendas.putClientProperty("JTabbedPane.selectedForeground", grafiteProfundo);
         // --- 7. SEPARADORES DE LINHA ---
        javax.swing.JSeparator[] todosSeparadores = { jSeparator4, jSeparator5, jSeparator9, jSeparator10, jSeparator12 };
        for (javax.swing.JSeparator sep : todosSeparadores) {
            sep.setForeground(cinzaLinhas); sep.setBackground(cinzaLinhas);
        }

        // --- 8. VETOR DE BOTÕES DE AÇÃO PLANOS ---
        javax.swing.JButton[] todosBotoesAcao = {
            buttonSalvar, buttonInserirVendasDiaria, jButton19, buttonLimpar, buttonAtualiza, buttonLeitorQRCode, buttonCTroca, 
            buttonExcluirContaFixa, buttonSalvarContaFixa, buttonEditarContaFixa, buttonIncluirContaFixa
        };
        for (javax.swing.JButton btn : todosBotoesAcao) {
            btn.setBackground(grafiteClaro); btn.setForeground(brancoPuro);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btn.setFocusPainted(false); btn.setBorderPainted(false);
            btn.putClientProperty("JButton.buttonType", "square");
        }
        buttonPainelVendas.setBackground(verdePainel); buttonPainelVendas.setForeground(brancoPuro);
        buttonPainelVendas.setFocusPainted(false); buttonPainelVendas.setBorderPainted(false);
        buttonPainelVendas.putClientProperty("JButton.buttonType", "square");

        // 🔥 ESTILIZAÇÃO EXCLUSIVA DOS BOTÕES MENU (Bronze Acobreado)
        java.awt.Color bronzeAcobreado = new java.awt.Color(140, 120, 83); // #8C7853
        javax.swing.JButton[] botoesMenuExclusivos = { buttonMenuVendas, buttonMenuContasFixas };
        for (javax.swing.JButton btnMenu : botoesMenuExclusivos) {
            btnMenu.setBackground(bronzeAcobreado); btnMenu.setForeground(brancoPuro);
            btnMenu.setFocusPainted(false); btnMenu.setBorderPainted(false);
            btnMenu.putClientProperty("JButton.buttonType", "square");
        }
        
        // --- 9. 🔥 NOVA BARRA DE TÍTULO PREMIUM CONFIGURADA COM ESPAÇAMENTO COMPLETO ---
        javax.swing.JPanel barraTituloPremium = new javax.swing.JPanel();
        barraTituloPremium.setBackground(grafiteClaro);
        barraTituloPremium.setOpaque(true);
        barraTituloPremium.setLayout(new java.awt.BorderLayout(15, 0));
        barraTituloPremium.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0)); 
        
        // Lado Esquerdo: Identificação Comercial do Cliente
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Movimentações Financeiras e Fluxo de Caixa");
        lblClienteBarra.setForeground(brancoPuro);
        lblClienteBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        barraTituloPremium.add(lblClienteBarra, java.awt.BorderLayout.WEST);
        
        // Lado Direito: Assinatura e Agrupador do Botão de Fechar (X)
        javax.swing.JPanel painelDireitoBarra = new javax.swing.JPanel();
        painelDireitoBarra.setBackground(grafiteClaro);
        painelDireitoBarra.setOpaque(true);
        painelDireitoBarra.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 0));
        
        javax.swing.JLabel lblDevBarra = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDevBarra.setForeground(douradoOuro); 
        lblDevBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        painelDireitoBarra.add(lblDevBarra);
        
        // Botão X com hover vermelho
        javax.swing.JButton btnFecharJanela = new javax.swing.JButton(" X ");
        btnFecharJanela.setBackground(grafiteClaro); 
        btnFecharJanela.setForeground(brancoPuro);
        btnFecharJanela.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnFecharJanela.setFocusPainted(false); 
        btnFecharJanela.setBorderPainted(false);
        btnFecharJanela.setPreferredSize(new java.awt.Dimension(45, 30)); 
        btnFecharJanela.putClientProperty("JButton.buttonType", "square"); 
        
        btnFecharJanela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btnFecharJanela.setBackground(vermelhoSair); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btnFecharJanela.setBackground(grafiteClaro); }
        });
        btnFecharJanela.addActionListener(e -> { this.dispose(); });
        
        painelDireitoBarra.add(btnFecharJanela);
        barraTituloPremium.add(painelDireitoBarra, java.awt.BorderLayout.EAST);
        
        // Fixa as coordenadas físicas rígidas da barra de título no topo absoluto
        barraTituloPremium.setBounds(0, 0, this.getWidth(), 30);
         // --- 10. MOTOR DE MOVIMENTAÇÃO DA JANELA ---
        final int[] coordX = {0}; final int[] coordY = {0};
        final javax.swing.JFrame janelaAtual = this; 
        barraTituloPremium.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { coordX[0] = e.getX(); coordY[0] = e.getY(); }
        });
        barraTituloPremium.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) { janelaAtual.setLocation(e.getXOnScreen() - coordX[0], e.getYOnScreen() - coordY[0]); }
        });
        
                // --- 🔥 CONFIGURAÇÃO EXCLUSIVA PARA O POPUP DE AUTOCOMPLETE DE CLIENTES --
        // 1. Força as propriedades globais das caixas de listagem flutuantes (JList e JPopupMenu)
        javax.swing.UIManager.put("PopupMenu.background", grafiteClaroPop);
        javax.swing.UIManager.put("PopupMenu.border", javax.swing.BorderFactory.createLineBorder(cinzaBordasPop, 1));
        
        javax.swing.UIManager.put("List.background", grafiteClaroPop);
        javax.swing.UIManager.put("List.foreground", brancoPuroPop);
        javax.swing.UIManager.put("List.selectionBackground", douradoOuroPop);   // Cliente selecionado ganha fundo DOURADO!
        javax.swing.UIManager.put("List.selectionForeground", grafiteProfundoPop); // Texto do cliente selecionado fica escuro
        
        // 2. Customiza a barra de rolagem (JScrollBar) para sumir com o fundo branco fosco da imagem
        javax.swing.UIManager.put("ScrollBar.background", grafiteProfundoPop);
        javax.swing.UIManager.put("ScrollBar.thumb", grafiteClaroPop);
        javax.swing.UIManager.put("ScrollBar.track", grafiteProfundoPop);
        javax.swing.UIManager.put("ScrollBar.width", 10);

        // 3. Aplica uma varredura de segurança caso a sua biblioteca de AutoComplete use componentes encapsulados
        // Altere 'campoNomeCliente' se a variável da caixa de texto do cliente usar outro nome
        try {
            campoNomeCliente.putClientProperty("JComponent.roundRect", true);
        } catch(Exception ex) {System.err.println("Erro: "+ex);}
        // --- 11. 🔥 INSERÇÃO ABSOLUTA COM RECUO DE SEGURANÇA PARA AS ABAS APARECEREM ---
        // Adiciona a barra na camada superior flutuante para ela ficar visível no topo
        this.getLayeredPane().add(barraTituloPremium, javax.swing.JLayeredPane.PALETTE_LAYER);
        
        // 🚀 O SEGREDO DO SUCESSO: Aplica uma margem invisível de 35 pixels no topo do contêiner do NetBeans.
        // Isso empurra o painel das abas para baixo na marra, fazendo as palavras VENDAS e CONTAS FIXAS reaparecerem!
        try {
            ((javax.swing.JComponent)this.getContentPane()).setBorder(javax.swing.BorderFactory.createEmptyBorder(35, 0, 0, 0));
        } catch(Exception e) {
            System.out.println("Aviso: Tentando recuo alternativo no painel de fundo...");
            try { jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(35, 0, 0, 0)); } catch(Exception ex) {}
            try { jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(35, 0, 0, 0)); } catch(Exception ex) {}
        }
        
        // Força a janela inteira a se readequar e atualizar os pixels na tela
        this.revalidate();
        this.repaint();
        
        this.setLocationRelativeTo(null);
//        this.setIconImage(new ImageIcon(getClass().getResource("/images/favicon.png")).getImage());
        this.setTitle(ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI Ltda");
        
        new Thread(() -> {
            try {
                System.out.println("Carregando balanços e cadastros da Cloud em background silencioso...");
                
                // Descarrega as buscas pesadas da internet de forma assíncrona
                carregarTabelaContasFixas(); 
                carregarCacheBuscaClientes();
                
                System.out.println("Módulo Financeiro alimentado e caches sincronizados!");
            } catch (Exception ex) {
                System.err.println("Erro na Thread secundária financeira: " + ex.getMessage());
            }
        }).start();

        configurarAbaContasFixas(); // 1. Prepara a tabela e o ouvinte de cliques
//        carregarTabelaContasFixas(); // 2. Vai na nuvem e descarrega as contas na grad
        configurarCampoCliente();
        configurarFoco();
        configurarCampo();
        configurarBuscaAutomatica();
//        carregarCacheBuscaClientes();
     
        listModel = new DefaultListModel<>();
        listaSugestoes = new JList<>(listModel);
        listaSugestoes.setBackground(new Color(255, 255, 255));
        listaSugestoes.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        listaSugestoes.setFixedCellHeight(25);      
        // Estilo da lista
        listaSugestoes.setSelectionBackground(new Color(0, 120, 215));
        listaSugestoes.setSelectionForeground(Color.WHITE);       
        popupSugestoes = new JPopupMenu();
        popupSugestoes.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(listaSugestoes);
        scrollPane.setPreferredSize(new Dimension(campoNomeCliente.getWidth(), 150));
        popupSugestoes.add(scrollPane);       
        timerBusca = new Timer(500, (ActionEvent e) -> {
            try {
                String busca = campoNomeCliente.getText().trim();
                if (busca.length() >= 2) {
                    buscarSugestaoNome();
                }
            } catch (ClassNotFoundException | SQLException ex) {
                System.err.println("Erro no timer: "+ex);
            }
        });
        timerBusca.setRepeats(false);
        campoNomeCliente.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { timerBusca.restart(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { timerBusca.restart(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { timerBusca.restart(); }
        });       
         // 4. Adicionar o evento de clique na lista (MousePressed)
        listaSugestoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String selecionado = listaSugestoes.getSelectedValue();
                if (selecionado != null) {
                    campoNomeCliente.setText(selecionado);
                    popupSugestoes.setVisible(false);
                }
            }
        });       
        listaSugestoes.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String selecionado = listaSugestoes.getSelectedValue();
                    if (selecionado != null) {
                        campoNomeCliente.setText(selecionado);
                        popupSugestoes.setVisible(false);
                    }
                }
            }
        });
        
                // --- 🔥 INTERCEPTADOR GRÁFICO DEFINITIVO DO POPUP DE CLIENTES ---
        // Monitora as subcamadas do JTextField para capturar a caixinha branca e forçar o Dark na marra
        campoNomeCliente.addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                // Roda uma fração de segundo após qualquer mudança para pegar o popup abrindo na RAM
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        java.awt.Color grafiteProfundoPop = new java.awt.Color(28, 28, 28);    // #1C1C1C
                        java.awt.Color grafiteClaroPop    = new java.awt.Color(45, 45, 45);    // #2D2D2D
                        java.awt.Color douradoOuroPop     = new java.awt.Color(212, 175, 55);  // #D4AF37
                        java.awt.Color brancoPuroPop      = new java.awt.Color(255, 255, 255); // #FFFFFF
                        java.awt.Color cinzaBordasPop     = new java.awt.Color(60, 60, 60);

                        // Varre todas as janelas ativas flutuantes abertas neste milissegundo no Java Swing
                        java.awt.Window[] todasJanelas = java.awt.Window.getWindows();
                        for (java.awt.Window janela : todasJanelas) {
                            // Localiza o painel de sugestões (normalmente um JWindow ou Popup flutuante leve)
                            if (janela.getClass().getName().contains("Popup") || janela instanceof javax.swing.JWindow) {
                                
                                // 1. Arranca o fundo branco e aplica a borda reta fina de grife
                                janela.setBackground(grafiteClaroPop);
                                if (janela instanceof javax.swing.RootPaneContainer) {
                                    ((javax.swing.RootPaneContainer) janela).getRootPane().setBorder(
                                        javax.swing.BorderFactory.createLineBorder(cinzaBordasPop, 1)
                                    );
                                }

                                // 2. Mini Classe interna que varre as entranhas trancadas do AutoComplete
                                class ComponentScanner {
                                    void processarPinturaHorizontal(java.awt.Container container, java.awt.Color fundo, java.awt.Color claro, java.awt.Color ouro, java.awt.Color branco) {
                                        for (java.awt.Component comp : container.getComponents()) {
                                            
                                            // Encontrou a lista interna onde ficam os nomes? Reescreve o renderizador de células!
                                            if (comp instanceof javax.swing.JList) {
                                                final javax.swing.JList<?> listaNomes = (javax.swing.JList<?>) comp;
                                                listaNomes.setBackground(claro);
                                                listaNomes.setForeground(branco);
                                                listaNomes.setSelectionBackground(ouro);
                                                listaNomes.setSelectionForeground(fundo);
                                                
                                                // Golpe de mestre: Obriga cada linha de nome a se pintar em Grafite Claro e Dourado Ouro
                                                listaNomes.setCellRenderer(new javax.swing.DefaultListCellRenderer() {
                                                    @Override
                                                    public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> l, Object v, int idx, boolean isSel, boolean chkFoc) {
                                                        javax.swing.JLabel c = (javax.swing.JLabel) super.getListCellRendererComponent(l, v, idx, isSel, chkFoc);
                                                        c.setOpaque(true);
                                                        if (isSel) {
                                                            c.setBackground(ouro);    // Nome selecionado vira DOURADO Ouro
                                                            c.setForeground(fundo);   // Letra do selecionado fica Grafite Escuro
                                                        } else {
                                                            c.setBackground(claro);   // Linha normal fica Grafite Claro plano
                                                            c.setForeground(branco);  // Letra normal fica Branco Puro nítido
                                                        }
                                                        c.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
                                                        return c;
                                                    }
                                                });
                                            }

                                            // Limpa o painel de rolagem JScrollPane e remove as setinhas brancas
                                            if (comp instanceof javax.swing.JScrollPane) {
                                                javax.swing.JScrollPane scroll = (javax.swing.JScrollPane) comp;
                                                scroll.setBackground(fundo);
                                                scroll.getViewport().setBackground(claro);
                                                scroll.setBorder(null);
                                                scroll.getVerticalScrollBar().setBackground(fundo);
                                            }

                                            // Se o componente tiver mais subcamadas, continua a busca profunda recursiva
                                            if (comp instanceof java.awt.Container) {
                                                processarPinturaHorizontal((java.awt.Container) comp, fundo, claro, ouro, branco);
                                            }
                                        }
                                    }
                                }

                                // Dispara o scanner na raiz da janela flutuante capturada na memória RAM
                                new ComponentScanner().processarPinturaHorizontal(janela, grafiteProfundoPop, grafiteClaroPop, douradoOuroPop, brancoPuroPop);
                                
                                // Atualiza a renderização de pixels no monitor imediatamente
                                janela.revalidate();
                                janela.repaint();
                            }
                        }
                    }
                });
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        abaVendas = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tabelaVendas = new javax.swing.JTable();
        buttonSalvar = new javax.swing.JButton();
        buttonInserirVendasDiaria = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        buttonMenuVendas = new javax.swing.JButton();
        campoDataVenda = new javax.swing.JTextField();
        comboOrigemVenda = new javax.swing.JComboBox<>();
        comboTipoPagamento = new javax.swing.JComboBox<>();
        campoCodPeca = new javax.swing.JTextField();
        campoNomeCliente = new javax.swing.JTextField();
        campoObsVendas = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        buttonLimpar = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator9 = new javax.swing.JSeparator();
        campoValorVenda = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        buttonAtualiza = new javax.swing.JButton();
        campoIdVenda = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        buttonRetireLoja = new javax.swing.JRadioButton();
        buttonEntregaEndereco = new javax.swing.JRadioButton();
        buttonLeitorQRCode = new javax.swing.JButton();
        buttonCTroca = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JSeparator();
        buttonPainelVendas = new javax.swing.JButton();
        buttonEtiquetas = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        buttonExcluirContaFixa = new javax.swing.JButton();
        buttonSalvarContaFixa = new javax.swing.JButton();
        buttonEditarContaFixa = new javax.swing.JButton();
        buttonIncluirContaFixa = new javax.swing.JButton();
        buttonMenuContasFixas = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        campoDataContaFixa = new javax.swing.JTextField();
        campoOrigemContaFixa = new javax.swing.JTextField();
        campoDescricaoContaFixa = new javax.swing.JTextField();
        campoValorContaFixa = new javax.swing.JTextField();
        campoCredorContaFixa = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jSeparator10 = new javax.swing.JSeparator();

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        abaVendas.setBackground(new java.awt.Color(204, 204, 255));
        abaVendas.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        abaVendas.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N

        jPanel3.setBackground(new java.awt.Color(204, 204, 255));

        tabelaVendas.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        tabelaVendas.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        tabelaVendas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "VENDA", "DATA", "ORIGEM", "TIPO PAGAMENTO", "VALOR", "ITENS", "CLIENTE", "OBSERVAÇÔES"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelaVendas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tabelaVendas.setRowHeight(25);
        jScrollPane5.setViewportView(tabelaVendas);
        if (tabelaVendas.getColumnModel().getColumnCount() > 0) {
            tabelaVendas.getColumnModel().getColumn(0).setPreferredWidth(3);
            tabelaVendas.getColumnModel().getColumn(1).setPreferredWidth(28);
            tabelaVendas.getColumnModel().getColumn(2).setPreferredWidth(55);
            tabelaVendas.getColumnModel().getColumn(3).setPreferredWidth(50);
            tabelaVendas.getColumnModel().getColumn(4).setPreferredWidth(25);
            tabelaVendas.getColumnModel().getColumn(5).setPreferredWidth(25);
        }

        buttonSalvar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonSalvar.setText("SALVAR");
        buttonSalvar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSalvarActionPerformed(evt);
            }
        });

        buttonInserirVendasDiaria.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonInserirVendasDiaria.setText("EDITAR");
        buttonInserirVendasDiaria.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonInserirVendasDiaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonInserirVendasDiariaActionPerformed(evt);
            }
        });

        jButton19.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton19.setText("EXCLUIR");
        jButton19.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });

        buttonMenuVendas.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenuVendas.setText("MENU");
        buttonMenuVendas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonMenuVendas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuVendasActionPerformed(evt);
            }
        });

        campoDataVenda.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        campoDataVenda.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoDataVenda.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoDataVenda.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoDataVendaMouseClicked(evt);
            }
        });

        comboOrigemVenda.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        comboOrigemVenda.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "VENDA LOJA", "VENDA WEB", "DESPESA", "FRETE", " " }));
        comboOrigemVenda.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        comboTipoPagamento.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        comboTipoPagamento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PIX", "CRÉDITO", "DÉBITO", "DINHEIRO", "C.TROCA", "OUTROS", " " }));
        comboTipoPagamento.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        campoCodPeca.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCodPeca.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCodPeca.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoCodPeca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoCodPecaActionPerformed(evt);
            }
        });

        campoNomeCliente.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        campoNomeCliente.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoNomeCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoNomeClienteMouseClicked(evt);
            }
        });
        campoNomeCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                campoNomeClienteKeyReleased(evt);
            }
        });

        campoObsVendas.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        campoObsVendas.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel1.setText("DATA");

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel2.setText("ORIGEM");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel3.setText("PAGAMENTO");

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel4.setText("ITEM");

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel5.setText("NOME CLIENTE");

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel6.setText("OBSERVAÇÕES");

        buttonLimpar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonLimpar.setText("LIMPAR");
        buttonLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLimparActionPerformed(evt);
            }
        });

        campoValorVenda.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoValorVenda.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoValorVenda.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoValorVenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoValorVendaActionPerformed(evt);
            }
        });

        jLabel29.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel29.setText("VALOR");

        buttonAtualiza.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonAtualiza.setText("ATUALIZAR");
        buttonAtualiza.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAtualizaActionPerformed(evt);
            }
        });

        campoIdVenda.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        campoIdVenda.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoIdVenda.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoIdVenda.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoIdVendaMouseClicked(evt);
            }
        });

        jLabel30.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel30.setText("ID ");

        buttonRetireLoja.setBackground(new java.awt.Color(204, 204, 255));
        buttonRetireLoja.setFont(new java.awt.Font("Times New Roman", 1, 10)); // NOI18N
        buttonRetireLoja.setText("RETIRE LOJA");

        buttonEntregaEndereco.setBackground(new java.awt.Color(204, 204, 255));
        buttonEntregaEndereco.setFont(new java.awt.Font("Times New Roman", 1, 10)); // NOI18N
        buttonEntregaEndereco.setText("ENTREGA ENDEREÇO");
        buttonEntregaEndereco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEntregaEnderecoActionPerformed(evt);
            }
        });

        buttonLeitorQRCode.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        buttonLeitorQRCode.setText("QRCODE");
        buttonLeitorQRCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLeitorQRCodeActionPerformed(evt);
            }
        });

        buttonCTroca.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        buttonCTroca.setText("C. TROCA");
        buttonCTroca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCTrocaActionPerformed(evt);
            }
        });

        buttonPainelVendas.setBackground(new java.awt.Color(102, 255, 102));
        buttonPainelVendas.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonPainelVendas.setForeground(new java.awt.Color(51, 51, 51));
        buttonPainelVendas.setText("PAINEL VENDAS");
        buttonPainelVendas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPainelVendasActionPerformed(evt);
            }
        });

        buttonEtiquetas.setBackground(new java.awt.Color(153, 153, 0));
        buttonEtiquetas.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonEtiquetas.setText("ETIQUETAS");
        buttonEtiquetas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEtiquetasActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 880, Short.MAX_VALUE)
                            .addComponent(jSeparator9))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(campoIdVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jLabel30)))
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoDataVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(30, 30, 30)
                                        .addComponent(jLabel1)))
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(28, 28, 28)
                                        .addComponent(jLabel2)
                                        .addGap(51, 51, 51))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(comboOrigemVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addComponent(buttonRetireLoja)
                                .addGap(18, 18, 18)
                                .addComponent(buttonEntregaEndereco)
                                .addGap(34, 34, 34)))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(buttonEtiquetas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(buttonPainelVendas)
                                .addGap(96, 96, 96)
                                .addComponent(buttonCTroca, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonLeitorQRCode, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(comboTipoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(25, 25, 25)
                                        .addComponent(jLabel29)
                                        .addGap(28, 28, 28))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoValorVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(campoCodPeca, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoNomeCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(14, 14, 14)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(44, 44, 44)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(9, 9, 9)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(campoObsVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(194, 194, 194))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator12, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator4)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(buttonMenuVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonAtualiza, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonInserirVendasDiaria, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(195, 195, 195))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel29)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoIdVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoDataVenda, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(comboOrigemVenda)
                    .addComponent(comboTipoPagamento)
                    .addComponent(campoValorVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoCodPeca, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(campoNomeCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoObsVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(buttonRetireLoja)
                        .addComponent(buttonEntregaEndereco))
                    .addComponent(buttonLeitorQRCode, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                    .addComponent(buttonCTroca, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(buttonPainelVendas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonEtiquetas)))
                .addGap(27, 27, 27)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jSeparator12, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonMenuVendas)
                    .addComponent(buttonAtualiza, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonLimpar)
                    .addComponent(jButton19)
                    .addComponent(buttonInserirVendasDiaria)
                    .addComponent(buttonSalvar))
                .addContainerGap(65, Short.MAX_VALUE))
        );

        campoCodPeca.getAccessibleContext().setAccessibleName("");

        abaVendas.addTab("VENDAS", jPanel3);

        jPanel7.setBackground(new java.awt.Color(204, 204, 255));

        jTable1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jTable1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DATA", "ORIGEM", "DESCRIÇÃO", "VALOR", "CONTA PAGA"
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jTable1.setRowHeight(25);
        jScrollPane2.setViewportView(jTable1);

        buttonExcluirContaFixa.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonExcluirContaFixa.setText("EXCLUIR");
        buttonExcluirContaFixa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonExcluirContaFixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExcluirContaFixaActionPerformed(evt);
            }
        });

        buttonSalvarContaFixa.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonSalvarContaFixa.setText("SALVAR");
        buttonSalvarContaFixa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonSalvarContaFixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSalvarContaFixaActionPerformed(evt);
            }
        });

        buttonEditarContaFixa.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonEditarContaFixa.setText("EDITAR");
        buttonEditarContaFixa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonEditarContaFixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditarContaFixaActionPerformed(evt);
            }
        });

        buttonIncluirContaFixa.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonIncluirContaFixa.setText("INCLUIR");
        buttonIncluirContaFixa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonIncluirContaFixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonIncluirContaFixaActionPerformed(evt);
            }
        });

        buttonMenuContasFixas.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenuContasFixas.setText("MENU");
        buttonMenuContasFixas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuContasFixasActionPerformed(evt);
            }
        });

        campoDataContaFixa.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoDataContaFixa.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoDataContaFixa.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoDataContaFixa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoDataContaFixaMouseClicked(evt);
            }
        });

        campoOrigemContaFixa.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoOrigemContaFixa.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoOrigemContaFixa.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        campoDescricaoContaFixa.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoDescricaoContaFixa.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoDescricaoContaFixa.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        campoValorContaFixa.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoValorContaFixa.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoValorContaFixa.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        campoCredorContaFixa.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCredorContaFixa.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCredorContaFixa.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel14.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel14.setText("DATA");

        jLabel15.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel15.setText("ORIGEM");

        jLabel16.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel16.setText("DESCRIÇAO");

        jLabel17.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel17.setText("VALOR");

        jLabel18.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel18.setText("CREDOR");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(127, 127, 127)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(74, 74, 74)
                .addComponent(jLabel16)
                .addGap(109, 109, 109)
                .addComponent(jLabel17)
                .addGap(129, 129, 129)
                .addComponent(jLabel18)
                .addGap(164, 164, 164))
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jSeparator10, javax.swing.GroupLayout.DEFAULT_SIZE, 848, Short.MAX_VALUE)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(buttonMenuContasFixas, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonIncluirContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonEditarContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonExcluirContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSalvarContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSeparator5)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                                .addComponent(campoDataContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoOrigemContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoDescricaoContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoValorContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoCredorContaFixa)))
                        .addGap(72, 72, 72))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 848, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 222, Short.MAX_VALUE))))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 438, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoDataContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoOrigemContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoDescricaoContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoValorContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoCredorContaFixa, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSalvarContaFixa)
                    .addComponent(buttonExcluirContaFixa)
                    .addComponent(buttonEditarContaFixa)
                    .addComponent(buttonIncluirContaFixa)
                    .addComponent(buttonMenuContasFixas))
                .addContainerGap(71, Short.MAX_VALUE))
        );

        abaVendas.addTab("CONTAS FIXAS", jPanel7);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(abaVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 1025, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(abaVendas)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void campoDataContaFixaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoDataContaFixaMouseClicked
        LocalDateTime date = LocalDateTime.now();
        String data = date.format(DateTimeFormatter.ISO_DATE);
        String dia = data.substring(8,10);
        String mes = data.substring(5,7);
        String ano = data.substring(0,4);
        campoDataVenda.setText(""+dia+"/"+mes+"/"+ano+"");
    }//GEN-LAST:event_campoDataContaFixaMouseClicked

////////// Media Mensal //////////
    private void buttonMenuContasFixasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuContasFixasActionPerformed
        new TelaMenu().setVisible(true);
        dispose();
    }//GEN-LAST:event_buttonMenuContasFixasActionPerformed

    private void campoIdVendaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoIdVendaMouseClicked
        int x = 0;
        try {
            vdao.selectIdVendaCloud(v);
            int codigo = v.getIdVenda();
            if(codigo != 0){
                x = (codigo);
                int codId = Integer.hashCode(x+1);
                campoIdVenda.setText(""+codId+"");
                System.out.println("Próximo código ID é: "+codId);
                System.out.println("----------------------------------");
            }else{
                campoIdVenda.setText("01");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_campoIdVendaMouseClicked

    private void buttonAtualizaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAtualizaActionPerformed
        String data;
        String dataDia;
        int n = 0;
        try {
            vdao.carregaTabelaValores(v);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
        do{
            campoIdVenda.setText(String.valueOf(v.getIdVenda()));
            System.out.println(campoIdVenda.getText());
            dataDia = (v.getDataVenda().toString());
            campoDataVenda.setText(v.getDataVenda().toString().replaceAll("-", "/"));
            System.out.println(campoDataVenda.getText());
            data = campoDataVenda.getText();
            System.out.println("Data do banco: "+data);
            System.out.println("Data do dia: "+dataDia);
            String dia = data.substring(8, 10);
            System.out.println(dia);
            String mes = data.substring(5, 7);
            System.out.println(mes);
            String ano = data.substring(0, 4);
            System.out.println(ano);
            campoDataVenda.setText(""+dia+"/"+mes+"/"+ano+"");
            System.out.println(dia+"/"+mes+"/"+ano);
            comboOrigemVenda.setSelectedItem(v.getOrigemVenda());
            System.out.println(comboOrigemVenda.getSelectedItem());
            comboTipoPagamento.setSelectedItem(v.getTipoPag());
            System.out.println(comboTipoPagamento.getSelectedItem());
            campoValorVenda.setText(v.getValorVenda());
            System.out.println(campoValorVenda.getText());
            campoCodPeca.setText(v.getCodPecas());
            System.out.println(campoCodPeca.getText());
            campoNomeCliente.setText(v.getNomeCliente());
            System.out.println(campoNomeCliente.getText());
            campoObsVendas.setText(v.getObservacao());
            System.out.println(campoObsVendas.getText());

            DefaultTableModel vendas = (DefaultTableModel) tabelaVendas.getModel();
            vendas.setNumRows(n);
            vendas.addRow(new Object[]{
                this.campoIdVenda.getText(),
                this.campoDataVenda.getText(),
                this.comboOrigemVenda.getSelectedItem(),
                this.comboTipoPagamento.getSelectedItem(),
                this.campoValorVenda.getText(),
                this.campoCodPeca.getText().trim(),
                this.campoNomeCliente.getText(),
                this.campoObsVendas.getText()
            });
            if (tabelaVendas.getColumnModel().getColumnCount() >= 3) {
                tabelaVendas.getColumnModel().getColumn(0).setPreferredWidth(30);
                tabelaVendas.getColumnModel().getColumn(1).setPreferredWidth(40);
                tabelaVendas.getColumnModel().getColumn(2).setPreferredWidth(30);
                tabelaVendas.getColumnModel().getColumn(5).setPreferredWidth(30);
                tabelaVendas.getColumnModel().getColumn(6).setPreferredWidth(150);
            }
        }while(dataDoDia.equals(dataDia));
        limpaCamposTransacao();
    }//GEN-LAST:event_buttonAtualizaActionPerformed

    private void buttonLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimparActionPerformed
        limpaCamposTransacao();
    }//GEN-LAST:event_buttonLimparActionPerformed

    private void campoDataVendaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoDataVendaMouseClicked
        System.out.println("Iniciando ajuste da data para tela...");
        LocalDateTime date = LocalDateTime.now();
        dataVenda = date.format(DateTimeFormatter.ISO_DATE);
//        System.out.println("Data venda sem ajuste: "+dataVenda);
        String dia = dataVenda.substring(8,10);
        String mes = dataVenda.substring(5,7);
        String ano = dataVenda.substring(0,4);
        campoDataVenda.setText(""+dia+"/"+mes+"/"+ano+"");
        System.out.println("Data Venda ajustada: "+dia+"/"+mes+"/"+ano);
        System.out.println("-----------------------------------------");
    }//GEN-LAST:event_campoDataVendaMouseClicked

/////////// Vendas ///////////
    private void buttonMenuVendasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuVendasActionPerformed
        new TelaMenu().setVisible(true);
        dispose();
    }//GEN-LAST:event_buttonMenuVendasActionPerformed

//////////Contas Fixas /////////
    private void buttonInserirVendasDiariaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonInserirVendasDiariaActionPerformed
        //EM CONSTRUCAO
    }//GEN-LAST:event_buttonInserirVendasDiariaActionPerformed

    private void buttonSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSalvarActionPerformed
        // ==========================================
        // 🔥 TRATAR VALOR DA VENDA
        // ==========================================
        String valorDigitado = campoValorVenda.getText().trim();
        double valorDouble = ValorMonetarioUtil.converterParaDouble(valorDigitado);
        String valorParaBanco = ValorMonetarioUtil.formatarParaBanco(valorDouble);

        // ==========================================
        // 🔥 EXIBIR FORMATADO PARA O USUÁRIO
        // ==========================================
        campoValorVenda.setText(ValorMonetarioUtil.formatarParaExibicao(valorDouble));

        // ==========================================
        // USAR O VALOR FORMATADO
        // ==========================================
        v.setValorVenda(valorParaBanco);
        s.setValorCompra(valorParaBanco);

        System.out.println("💰 Valor digitado: " + valorDigitado);
        System.out.println("💰 Valor convertido: " + valorDouble);
        System.out.println("💰 Valor para banco: " + valorParaBanco);
        String campoId = null;
        idVenda = campoIdVenda.getText();
        origemVenda = String.valueOf(comboOrigemVenda.getSelectedItem());
        tipoPago = String.valueOf(comboTipoPagamento.getSelectedItem());
        String formaPagamento = tipoPago.toUpperCase().trim();
        String clienteVenda = campoNomeCliente.getText().trim();
        String valorTexto = campoValorVenda.getText().trim().replace(",", ".");
        
        try {
            vdao.selectIdVendaCloud(v);
            id = v.getIdVenda();
            campoId = Integer.toString(id);
            System.out.println("Id: "+campoId);
        } catch (HeadlessException | ClassNotFoundException | SQLException ex) {
            System.err.println("Erro :"+ex);
        }
        // CENÁRIO A: FLUXO DE DESPESA
        if (origemVenda.equals(despesa)) {
            processarDespesa(campoId, origemVenda, tipoPago);
            DefaultTableModel despesas = (DefaultTableModel) tabelaVendas.getModel();
                if (despesas.getRowCount() < 30) {
                    despesas.addRow(new Object[]{
                        this.campoIdVenda.getText(), 
                        this.campoDataVenda.getText(),                      // deixar busca por nome no mesmo padrao da tela entregas
                        this.comboOrigemVenda.getSelectedItem(), 
                        this.comboTipoPagamento.getSelectedItem(), 
                        this.campoValorVenda.getText(), 
                        this.campoCodPeca.getText().trim(), 
                        this.campoNomeCliente.getText(), 
                        this.campoObsVendas.getText()
                    });
                    if (tabelaVendas.getColumnModel().getColumnCount() >= 3) {
                        tabelaVendas.getColumnModel().getColumn(0).setPreferredWidth(30);
                        tabelaVendas.getColumnModel().getColumn(1).setPreferredWidth(40);
                        tabelaVendas.getColumnModel().getColumn(2).setPreferredWidth(30);
                        tabelaVendas.getColumnModel().getColumn(5).setPreferredWidth(30);
                        tabelaVendas.getColumnModel().getColumn(6).setPreferredWidth(150);
                    }
                } else {
                    MensagemSistema.mostrarAvisoDark(this, "Limite máximo de 30 vendas atingido!");
                }
            limpaCamposTransacao();
            return;
        }

        // CENÁRIO B: FLUXO DE CRÉDITO DE TROCA (BINGO!)
        if (tipoPago.equalsIgnoreCase("C.TROCA") || formaPagamento.contains("C.TROCA")) {
            DefaultTableModel trocas = (DefaultTableModel) tabelaVendas.getModel();
                    if (trocas.getRowCount() < 30) {
                        trocas.addRow(new Object[]{
                            this.campoIdVenda.getText(), 
                            this.campoDataVenda.getText(), 
                            this.comboOrigemVenda.getSelectedItem(), 
                            this.comboTipoPagamento.getSelectedItem(), 
                            this.campoValorVenda.getText(), 
                            this.campoCodPeca.getText().trim(), 
                            this.campoNomeCliente.getText(), 
                            this.campoObsVendas.getText()
                        });
                        if (tabelaVendas.getColumnModel().getColumnCount() >= 3) {
                            tabelaVendas.getColumnModel().getColumn(0).setPreferredWidth(30);
                            tabelaVendas.getColumnModel().getColumn(1).setPreferredWidth(40);
                            tabelaVendas.getColumnModel().getColumn(2).setPreferredWidth(30);
                            tabelaVendas.getColumnModel().getColumn(5).setPreferredWidth(30);
                            tabelaVendas.getColumnModel().getColumn(6).setPreferredWidth(150);
                        }
                        limpaCamposTransacao();
                    } else {
                        MensagemSistema.mostrarAvisoDark(this, "Limite máximo de 30 vendas atingido!");
                    }
            // Dispara o validador. Se retornar false (sem saldo), aborta a operação na hora
            if (!processarCreditoTroca(clienteVenda, valorTexto)) {
                return; 
            }
            MensagemSistema.mostrarAvisoDark(this, "Venda por Vale-Troca finalizada com sucesso!");
        
            limpaCampos(); // Reseta a tela para a próxima venda do brechó
            return; 
        }

        // CENÁRIO C: DEVOLUÇÃO SECUNDÁRIA DE CRÉDITO (Caso use outra forma mas tenha Obs)
        if (!tipoPago.equals("C.TROCA")) {               
            try {                
                idTrocas = Integer.valueOf(campoObsVendas.getText());
                tdao.atualizaStatusCredito(t);
            } catch (ClassNotFoundException | NumberFormatException | SQLException ex) {
                System.out.println("Sem Credito de troca auxiliar para essa operacao");
            }             
        }
        if(!campoIdVenda.getText().isEmpty() && !campoDataVenda.getText().isEmpty() && !campoValorVenda.getText().isEmpty() && !campoCodPeca.getText().isEmpty() && !campoNomeCliente.getText().isEmpty()){           
            v.setOrigemVenda((String) comboOrigemVenda.getSelectedItem());
            if(!(buttonRetireLoja.isSelected() || buttonEntregaEndereco.isSelected()) && comboOrigemVenda.getSelectedItem()== venda_web){
                MensagemSistema.mostrarAvisoDark(this, "Escolha uma forma de entrega!");
            }
            if(!comboOrigemVenda.getSelectedItem().equals(venda_web) && (!comboOrigemVenda.getSelectedItem().equals(despesa))){
                v.setStatus(entregue);
                try {
                    vdao.atualizaStatusVendaLojaCloud();
                    System.out.println("Status VENDA LOJA atualizado para entregue!");
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println("Erro: "+ex);
                } catch (SQLException ex) {
                    Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println("Erro: "+ex);
                }
            }else{
                if(comboOrigemVenda.getSelectedItem() == despesa){
                    v.setStatus(entregue);
                }else{   
                    if(buttonRetireLoja.isSelected()){
                        v.setEntrega(retire_loja);
                    }else{               
                        v.setEntrega(entrega_endereco);
                    }              
                }
            }          
            v.setIdVenda(Integer.parseInt(campoIdVenda.getText()));
//            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            try {
                v.setDataVenda(fmt.parse(campoDataVenda.getText()));
                dataVenda = campoDataVenda.getText();
            } catch (ParseException ex) {
                Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Erro: Data fora do formato esperado!");
                MensagemSistema.mostrarAvisoDark(this, "Data com formato incorreto!");
            }          
            v.setTipoPag((String) comboTipoPagamento.getSelectedItem());
            System.out.println("Tipo de pagamento: "+comboTipoPagamento.getSelectedItem());
            if(!comboTipoPagamento.getSelectedItem().equals("C.TROCA")){               
                try {                
                    idTrocas = Integer.valueOf(campoObsVendas.getText());
                    tdao.atualizaStatusCredito(t);
                } catch (ClassNotFoundException | NumberFormatException | SQLException ex) {
                    System.out.println("Sem Credito de troca para essa operacao");
                }             
            }
            v.setValorVenda((String) campoValorVenda.getText());
            s.setValorCompra((String) campoValorVenda.getText());
            v.setCodPecas(campoCodPeca.getText().trim());
            System.out.println("Item(s): "+campoCodPeca.getText());
            v.setNomeCliente(campoNomeCliente.getText());
            v.setObservacao(campoObsVendas.getText());           
            try{                              
                String vendaId = campoIdVenda.getText();
                System.out.println("ID venda atual: "+vendaId);
                if(campoId.equals(vendaId)){
                    System.out.println("Erro ao tentar registar Id de vendas duplicado!");
                    MensagemSistema.mostrarAvisoDark(this, "Erro ao inserir ID de venda duplicado!");
                }else{                  
                    if(comboOrigemVenda.getSelectedItem() == venda_web){    //DADOS ENTREGA ENDERECO                  
                        v.setStatus(em_seperacao);
                        s.setStatus(em_seperacao);
                        salvarVendas();
                        atualizaEstoqueItem();
                        e.setId(Integer.parseInt(vendaId)); // inicio dados de entrega
                        s.setVendaId(Integer.parseInt(vendaId));                      
                        try {
                            e.setDatavenda(fmt.parse(campoDataVenda.getText()));
                            System.out.println(fmt.parse(campoDataVenda.getText()));
                            s.setDataCompra(fmt.parse(campoDataVenda.getText()));
                            System.out.println(fmt.parse(campoDataVenda.getText()));
                        } catch (ParseException ex) {
                            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Erro: "+ex);
                        }
                        e.setNomecli(campoNomeCliente.getText());
                        s.setNomeCliente(campoNomeCliente.getText());
                        System.out.println("Codigo(s): "+campoCodPeca.getText());
                        if(campoCodPeca.getText().contains(";")){
                            adicionaListaDeItens();                           
                        }
                        s.setCodigoPeca(campoCodPeca.getText().trim());
                        e.setCodpeca(campoCodPeca.getText().trim());                                                               
                        if(buttonRetireLoja.isSelected()){
                            e.setEntregue(false);
                            e.setStatus(em_seperacao);
                            e.setTipoentrega(retire_loja);
                            s.setTipoentrega(retire_loja);
                            e.setCanal(web);
                        }else{
                            e.setEntregue(false);
                            e.setStatus(em_seperacao);
                            e.setTipoentrega(entrega_endereco);
                            s.setTipoentrega(entrega_endereco);
                            e.setCanal(web);
                        } // FIM DADOS DE ENTREGA
                        salvarSacola();   
                        salvarFrete();
                    }else{ // DADOS RETIRE LOJA
                        v.setEntrega(buttonRetireLoja.getText());
                        e.setId(Integer.parseInt(vendaId));
                        try {
                            e.setDatavenda(fmt.parse(campoDataVenda.getText()));
                            s.setDataCompra(fmt.parse(campoDataVenda.getText()));
                            System.out.println("Data de Venda: "+campoDataVenda.getText());
                        } catch (ParseException ex) {
                            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
                            System.err.println("Erro: "+ex);
                        }
                        e.setNomecli(campoNomeCliente.getText());
                        s.setNomeCliente(campoNomeCliente.getText());
                        System.out.println("Codigo(s): "+campoCodPeca.getText());
                        if(campoCodPeca.getText().contains(";")){
                            adicionaListaDeItens();
                        }
                        s.setCodigoPeca(campoCodPeca.getText().trim());
                        e.setCodpeca(campoCodPeca.getText().trim());                       
                        s.setValorCompra(campoValorVenda.getText());
                        s.setVendaId(Integer.parseInt(vendaId));
                        e.setValorfrete(0.0);
                        e.setFretepago(false);
                        if(comboOrigemVenda.getSelectedItem().equals(despesa)){
                            v.setStatus(entregue);
                            e.setStatus(entregue);
                            s.setStatus(entregue);
                            e.setTipoentrega(loja);
                            s.setTipoentrega(loja);
                            v.setEntrega(loja);
                        }else{
                            e.setStatus(em_seperacao);
                            String status = v.getStatus();
                            if(status.equals(entregue)){
                                v.setStatus(status);
                                s.setStatus(status);
                                s.setTipoentrega(retire_loja);
                                e.setTipoentrega(retire_loja);
                                e.setStatus(status);
                                e.setEntregue(true);
                                e.setDataentrega(Date.valueOf(dataDoDia));
                                e.setCanal(loja);
                                salvarVendas();
                                salvarSacola();
                                salvarFrete();
                            }else{
                                v.setStatus(em_seperacao);
                                s.setStatus(em_seperacao);
                                e.setEntregue(false);
                                e.setTipoentrega(retire_loja);
                                s.setTipoentrega(retire_loja);
                                e.setCanal(web);
                                salvarVendas();
                                salvarSacola();
                                atualizaEstoqueItem();
                                salvarFrete();
                            }
                        }                      
                    }                                                                                      
                    DefaultTableModel vendas = (DefaultTableModel) tabelaVendas.getModel();
                    if (vendas.getRowCount() < 30) {
                        vendas.addRow(new Object[]{
                            this.campoIdVenda.getText(), 
                            this.campoDataVenda.getText(), 
                            this.comboOrigemVenda.getSelectedItem(), 
                            this.comboTipoPagamento.getSelectedItem(), 
                            this.campoValorVenda.getText(), 
                            this.campoCodPeca.getText().trim(), 
                            this.campoNomeCliente.getText(), 
                            this.campoObsVendas.getText()
                        });
                        if (tabelaVendas.getColumnModel().getColumnCount() >= 3) {
                            tabelaVendas.getColumnModel().getColumn(0).setPreferredWidth(30);
                            tabelaVendas.getColumnModel().getColumn(1).setPreferredWidth(40);
                            tabelaVendas.getColumnModel().getColumn(2).setPreferredWidth(30);
                            tabelaVendas.getColumnModel().getColumn(5).setPreferredWidth(30);
                            tabelaVendas.getColumnModel().getColumn(6).setPreferredWidth(150);
                        }
                    } else {
                        MensagemSistema.mostrarAvisoDark(this, "Limite máximo de 30 vendas atingido!");
                    }
                }
                adicionarEmailParaEnvioCupom();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Erro: "+ex);
            }          
        }else{ //CENARIO 4:FLUXO DE FRETE
            if(comboOrigemVenda.getSelectedItem() == "FRETE"){
                v.setIdVenda(Integer.valueOf(idVenda));
                try {
                    v.setDataVenda(fmt.parse(campoDataVenda.getText()));
                } catch (ParseException ex) {
                    Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro: "+ex);
                }
                v.setOrigemVenda(comboOrigemVenda.getSelectedItem().toString());
                v.setTipoPag((String) comboTipoPagamento.getSelectedItem());
                v.setValorVenda((String) campoValorVenda.getText());
                v.setCodPecas(""+sem_codigo+"");
                v.setNomeCliente(campoNomeCliente.getText());
                v.setObservacao(campoObsVendas.getText());
                v.setEntrega(entrega_endereco);
                v.setStatus(entregue);              
                salvarSomenteFrete();
                DefaultTableModel frete = (DefaultTableModel) tabelaVendas.getModel();
                    if (frete.getRowCount() < 30) {
                        frete.addRow(new Object[]{
                            this.campoIdVenda.getText(), 
                            this.campoDataVenda.getText(), 
                            this.comboOrigemVenda.getSelectedItem(), 
                            this.comboTipoPagamento.getSelectedItem(), 
                            this.campoValorVenda.getText(), 
                            this.campoCodPeca.getText().trim(), 
                            this.campoNomeCliente.getText(), 
                            this.campoObsVendas.getText()
                        });
                        if (tabelaVendas.getColumnModel().getColumnCount() >= 3) {
                            tabelaVendas.getColumnModel().getColumn(0).setPreferredWidth(30);
                            tabelaVendas.getColumnModel().getColumn(1).setPreferredWidth(40);
                            tabelaVendas.getColumnModel().getColumn(2).setPreferredWidth(30);
                            tabelaVendas.getColumnModel().getColumn(5).setPreferredWidth(30);
                            tabelaVendas.getColumnModel().getColumn(6).setPreferredWidth(150);
                        }
                        limpaCamposTransacao();
                    } else {
                        MensagemSistema.mostrarAvisoDark(this, "Limite máximo de 30 vendas atingido!");
                    }
            }else{
                MensagemSistema.mostrarAvisoDark(this, "Campos obrigatórios devem ser preenchidos!");
                System.out.println("Campos obrigatórios não preenchidos!");
            }
        }
        limpaCamposTransacao();
    }//GEN-LAST:event_buttonSalvarActionPerformed

    private void buttonEntregaEnderecoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEntregaEnderecoActionPerformed
        if(buttonEntregaEndereco.isSelected()){
            System.out.println("Selecionou Entrega Endereco!");
        }
    }//GEN-LAST:event_buttonEntregaEnderecoActionPerformed

    private void buttonLeitorQRCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLeitorQRCodeActionPerformed
        System.out.println("Acessando leitor QR Code...");       
//        LeitorQrcode lqrc = new LeitorQrcode();
//        lqrc.setVisible(true);
//        lqrc.setLocationRelativeTo(this);
//        lqrc.setTitle("Leitor QR Code");    
    }//GEN-LAST:event_buttonLeitorQRCodeActionPerformed

    private void buttonCTrocaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCTrocaActionPerformed
        try {
            tdao.atualizaTabelaCreditoLojaCloud();                  
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_buttonCTrocaActionPerformed

    private void campoNomeClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoNomeClienteMouseClicked
        try {
            buscarSugestaoNome();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_campoNomeClienteMouseClicked

    private void campoNomeClienteKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_campoNomeClienteKeyReleased
        try {
            buscarSugestaoNome();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_campoNomeClienteKeyReleased

    private void buttonPainelVendasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPainelVendasActionPerformed
        TelaDashboard td = new TelaDashboard();
        td.setLocationRelativeTo(null);
        td.atualizarDashboard();
        td.setVisible(true);
        td.fadeIn();
    }//GEN-LAST:event_buttonPainelVendasActionPerformed

    private void campoValorVendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoValorVendaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoValorVendaActionPerformed

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton19ActionPerformed

    private void buttonSalvarContaFixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSalvarContaFixaActionPerformed
        // Substitua os nomes 'campoDescricao', 'campoValor' e 'campoVencimento' pelos nomes reais dos seus JTextFields
        if (campoDescricaoContaFixa.getText().trim().isEmpty() || campoValorContaFixa.getText().trim().isEmpty() || campoDataContaFixa.getText().trim().isEmpty()) {
            MensagemSistema.mostrarAvisoDark(this, "Preencha todos os campos para cadastrar a conta!");
            return;
        }

        try {
            models.ContasFixas cf = new models.ContasFixas();
            cf.setDescricao(campoDescricaoContaFixa.getText().trim());

            // Trata o padrão contábil BigDecimal limpando vírgulas [links: 10]
            String valorLimpo = campoValorContaFixa.getText().trim().replace(",", ".");
            cf.setValor(new java.math.BigDecimal(valorLimpo));

            // Pega apenas os números do vencimento (ex: se digitou '05' ou '5')
            cf.setVencimento(Integer.parseInt(campoDataContaFixa.getText().trim().replaceAll("[^0-9]", "")));

            dao.ContasFixasDAO cfdao = new dao.ContasFixasDAO();
            cfdao.salvarNovaContaCloud(cf);

            MensagemSistema.mostrarAvisoDark(this, "Conta Fixa cadastrada com sucesso!");
            carregarTabelaContasFixas(); // Recarrega a JTable atualizada na hora [links: 10]

            // Limpa os campos após salvar
            campoDescricaoContaFixa.setText("");
            campoValorContaFixa.setText("");
            campoDataContaFixa.setText("");

        } catch (HeadlessException | ClassNotFoundException | NumberFormatException | SQLException ex) {
            MensagemSistema.mostrarAvisoDark(this, "Erro ao salvar conta: " + ex.getMessage());
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_buttonSalvarContaFixaActionPerformed

    private void buttonEditarContaFixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditarContaFixaActionPerformed
        if (campoDescricaoContaFixa.getText().trim().isEmpty() || campoValorContaFixa.getText().trim().isEmpty() || campoDataContaFixa.getText().trim().isEmpty()) {
            MensagemSistema.mostrarAvisoDark(this, "Selecione uma conta na tabela e preencha os dados para editar!");
            return;
        }
        
        try {
            models.ContasFixas cf = new models.ContasFixas();
            cf.setDescricao(campoDescricaoContaFixa.getText().trim()); // A descrição serve como chave para localizar a conta
            
            String valorLimpo = campoValorContaFixa.getText().trim().replace(",", ".");
            cf.setValor(new java.math.BigDecimal(valorLimpo));
            cf.setVencimento(Integer.parseInt(campoDataContaFixa.getText().trim().replaceAll("[^0-9]", "")));
            
            dao.ContasFixasDAO cfdao = new dao.ContasFixasDAO();
            cfdao.editarContaCloud(cf); // Dispara o UPDATE parametrizado na Aiven Cloud
            
            MensagemSistema.mostrarAvisoDark(this, "Conta Fixa atualizada com sucesso na nuvem!");
            carregarTabelaContasFixas(); // Redesenha a tabela síncrona com os novos valores [links: 10]
            
            // Limpa o formulário após a alteração
            campoDescricaoContaFixa.setText("");
            campoValorContaFixa.setText("");
            campoDataContaFixa.setText("");
            
        } catch (HeadlessException | ClassNotFoundException | NumberFormatException | SQLException ex) {
            MensagemSistema.mostrarAvisoDark(this, "Erro operacional ao editar conta: " + ex.getMessage());
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_buttonEditarContaFixaActionPerformed

    private void buttonExcluirContaFixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExcluirContaFixaActionPerformed
        String descricaoConta = campoDescricaoContaFixa.getText().trim();
        if (descricaoConta.isEmpty()) {
            MensagemSistema.mostrarAvisoDark(this, "Selecione uma conta na tabela para poder excluir!");
            return;
        }
        
        boolean confirma = MensagemSistema.mostrarDecisaoDark(this, 
            "Deseja realmente excluir a conta '" + descricaoConta + "' permanentemente da base Cloud?");
            
        if (confirma == true) {
            try {
                dao.ContasFixasDAO cfdao = new dao.ContasFixasDAO();
                cfdao.excluirContaCloud(descricaoConta);
                
                MensagemSistema.mostrarAvisoDark(this, "Conta fixa removida com sucesso do ecossistema!");
                carregarTabelaContasFixas(); // Recarrega o grid limpo
                
                campoDescricaoContaFixa.setText("");
                campoValorContaFixa.setText("");
                campoDataContaFixa.setText("");
            } catch (HeadlessException | ClassNotFoundException | SQLException ex) {
                MensagemSistema.mostrarAvisoDark(this, "Erro ao excluir registro: " + ex.getMessage());
                System.err.println("Erro: "+ex);
            }
        }
    }//GEN-LAST:event_buttonExcluirContaFixaActionPerformed

    private void buttonIncluirContaFixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonIncluirContaFixaActionPerformed
        // 1. VALIDAÇÃO: Verifica se os campos inferiores foram preenchidos [links: 10]
        if (campoDescricaoContaFixa.getText().trim().isEmpty() || campoValorContaFixa.getText().trim().isEmpty() || campoDataContaFixa.getText().trim().isEmpty()) {
            MensagemSistema.mostrarAvisoDark(this, "Preencha o DATA, a DESCRIÇÃO e o VALOR para incluir a nova conta!");
            return;
        }

        try {
            // 2. CONFIGURAÇÃO DO MODELO: Coleta os dados digitados na tela [links: 10]
            models.ContasFixas cf = new models.ContasFixas();
            cf.setDescricao(campoDescricaoContaFixa.getText().trim());

            // Trata o padrão BigDecimal substituindo a vírgula por ponto [links: 10]
            String valorTextoLimpo = campoValorContaFixa.getText().trim().replace(",", ".");
            cf.setValor(new java.math.BigDecimal(valorTextoLimpo));

            // Isola apenas os números do dia do vencimento digitado
            int diaVencimento = Integer.parseInt(campoDataContaFixa.getText().trim().replaceAll("[^0-9]", ""));
            cf.setVencimento(diaVencimento);
            cf.setPago(false); // Toda conta nova nasce pendente/desmarcada

            // 3. CAMADA DE DADOS: Grava a nova linha na Aiven Cloud [links: 10]
            dao.ContasFixasDAO cfdao = new dao.ContasFixasDAO();
            cfdao.salvarNovaContaCloud(cf);

            // 4. 🔥 INJEÇÃO DIRETA NA JTABLE (O que você precisava!):
            // Adiciona a linha de forma instantânea no grid visual da sua aba [links: 10]
            DefaultTableModel modeloAba = (DefaultTableModel) tabelaVendas.getModel(); // Tabela real da aba capturada no seu código
            modeloAba.addRow(new Object[]{
                String.format("Dia %02d", diaVencimento), // DATA: Formata o dia como "Dia 05" para bater com o padrão [links: 10]
                "CONTA FIXA",                             // ORIGEM: Texto fixo identificador
                campoDescricaoContaFixa.getText().trim(),          // OCORRENCIA: Descrição digitada
                valorTextoLimpo,                          // VALOR: Valor numérico limpo com duas casas [links: 10]
                false                                     // CONTA PAGA: Envia 'false' para o Checkbox nascer em branco! [links: 10]
            });

            MensagemSistema.mostrarAvisoDark(this, 
                "Nova obrigação incluída com sucesso!\n" +
                "Conta: " + campoDescricaoContaFixa.getText().trim() + "\n" +
                "Valor: R$ " + valorTextoLimpo);

            // 5. LIMPEZA DOS CAMPOS: Reseta os inputs inferiores para o próximo cadastro
            campoDescricaoContaFixa.setText("");
            campoValorContaFixa.setText("");
            campoDataContaFixa.setText("");

        } catch (NumberFormatException nfe) {
            MensagemSistema.mostrarAvisoDark(this, "Digite apenas o dia (número) no vencimento e valores válidos!");
        } catch (HeadlessException | ClassNotFoundException | SQLException ex) {
            MensagemSistema.mostrarAvisoDark(this, "Erro operacional ao incluir conta: " + ex.getMessage());
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_buttonIncluirContaFixaActionPerformed

    private void campoCodPecaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoCodPecaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoCodPecaActionPerformed

    private void buttonEtiquetasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEtiquetasActionPerformed
        // 1. Resgata dinamicamente o caminho das pastas de etiquetas da TelaEstoque
        String caminhoDasEtiquetas = views.TelaEstoque.caminho_inicial + views.TelaEstoque.user + views.TelaEstoque.itens_loja;

        // 2. CORREÇÃO: Passa os parâmetros exatos que a TelaSelecaoEtiquetas precisa para funcionar! 🚀
        TelaSelecaoEtiquetas tse = new TelaSelecaoEtiquetas(this, true, caminhoDasEtiquetas, this);
        tse.setVisible(true);
    }//GEN-LAST:event_buttonEtiquetasActionPerformed

    /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaUsuario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaFinanceiro().setVisible(true);
            }
        });
    }
    ///////////////// METODOS ABA VENDAS /////////////////// 

    private boolean processarCreditoTroca(String clienteVenda, String valorTexto) {
        if (clienteVenda.isEmpty() || valorTexto.isEmpty()) {
            MensagemSistema.mostrarAvisoDark(this, "Informe o NOME DO CLIENTE e o VALOR DA VENDA para validar o crédito!");
            return false;
        }
        try {
            // 1. 🔥 PADRÃO BIGDECIMAL: Captura o valor da venda atual forçando 2 casas decimais comerciais
            java.math.BigDecimal valorVendaAtual = new java.math.BigDecimal(valorTexto.replace(",", "."))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            
            TrocasDAO tdao = new TrocasDAO();
            java.math.BigDecimal saldoVale = new java.math.BigDecimal(Double.toString(tdao.buscarSaldoValeAtivoCloud(clienteVenda)));
            
            System.out.println("Saldo retornado do Banco Cloud: R$ " + saldoVale);

            // Comparações com zero usando compareTo (0 significa igual, -1 menor, 1 maior)
            if (saldoVale.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                MensagemSistema.mostrarAvisoDark(this, "Nenhum CRÉDITO DE TROCA ativo localizado para o cliente: " + clienteVenda);
                return false; 
            }

            // Compara se o saldo disponível é menor que o valor da venda atual (Retorna -1)
            if (saldoVale.compareTo(valorVendaAtual) < 0) {
                // Realiza a subtração exata centesimal sem gerar dízimas
                java.math.BigDecimal diferenca = valorVendaAtual.subtract(saldoVale);
                
                MensagemSistema.mostrarAvisoDark(this, 
                    "O Crédito de Troca cobre R$ " + saldoVale + ".\n" +
                    "O cliente deve pagar a diferença de R$ " + diferenca + " em dinheiro/pix.");
            } else {
                MensagemSistema.mostrarAvisoDark(this, 
                    "Crédito de Troca validado com sucesso!\n" +
                    "Saldo disponível: R$ " + saldoVale);
            }
            // Dá baixa contábil na nuvem
            tdao.baixarValeUtilizadoCloud(clienteVenda);
            return true;

        } catch (NumberFormatException nfe) {
            MensagemSistema.mostrarAvisoDark(this, "O campo VALOR possui caracteres inválidos!");
            System.err.println("Erro: "+nfe);
            return false;
        } catch (HeadlessException | ClassNotFoundException | SQLException ex) {
            MensagemSistema.mostrarAvisoDark(this, "Erro de comunicação Cloud ao validar Crédito de Troca: " + ex.getMessage());
            System.err.println("Erro: "+ex);
            return false;
        }
    }

    /**
     * Executa a lógica de persistência e logs de uma DESPESA.
     */
    private void processarDespesa(String campoId, String origemVenda, String tipoPago) {
        System.out.println("Iniciando registro de DESPESA na base...");       
        String valorDigitado = campoValorVenda.getText().trim();
        double valorDouble = ValorMonetarioUtil.converterParaDouble(valorDigitado);
        String valorParaBanco = ValorMonetarioUtil.formatarParaBanco(valorDouble);
        v.setIdVenda(Integer.valueOf(campoIdVenda.getText()));

        try {
            v.setDataVenda(fmt.parse(campoDataVenda.getText()));
        } catch (ParseException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }

        v.setOrigemVenda(origemVenda);
        v.setTipoPag(tipoPago);
        v.setValorVenda(valorParaBanco);
        v.setCodPecas(String.valueOf(sem_codigo));
        v.setNomeCliente(campoNomeCliente.getText());
        v.setObservacao(campoObsVendas.getText());
        v.setEntrega(produto_servico_loja);
        v.setStatus(entregue);

        try {
            salvarVendas();
            MensagemSistema.mostrarAvisoDark(this, "Registro de DESPESAS atualizado na base!");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }
 
    public void ultimoId() throws ClassNotFoundException, SQLException{
        int x = 0;
        try {
            vdao.selectIdVenda(v);
            int codigoId = v.getIdVenda();
            if(codigoId != 0){
                x = codigoId;
                int vendaId = (x+1);
                System.out.println(vendaId);
            }else{
                System.out.println("ID" + codigoId + " --> incorreto!");
            }           
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }      
    }
    
    public void atualizaEstoqueItem(){
        codPeca = campoCodPeca.getText().trim();       
        atualizaEstoqueLocal();
        atualizaEstoqueCloud();
        MensagemSistema.mostrarAvisoDark(this, "ESTOQUE Atualizado!");   
        System.out.println("Atualizou Estoque Item");
        System.out.println("-----------------------");
    }
    
    public void atualizaEstoqueLocal(){
        try {
            pdao.atualizaStatusEstoque(p);
            System.out.println("Atualizou Estoque Local");
            System.out.println("-----------------------");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Mensagem: "+ex);
        }
    }
    
    public void atualizaEstoqueCloud(){
        try {
            pdao.atualizaStatusEstoqueCloud(p);
            System.out.println("Atualizou Estoque Cloud");
            System.out.println("-----------------------");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Mensagem: "+ex);
        }
    }
    
    public void salvarVendas() throws ClassNotFoundException{       
        salvaVendasCloud();
        salvaVendasLocal();
        MensagemSistema.mostrarAvisoDark(this, "Tabela VENDAS Atualizada!");   
        System.out.println("Atualizou Tabela Vendas");
    }
    
    public void salvaVendasLocal() throws ClassNotFoundException{
        try{
            vdao.saveVendas(v);
            System.out.println("Atualizou VENDAS Local");
            System.out.println("-----------------------");
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void salvaVendasCloud() throws ClassNotFoundException{
        try{
            vdao.saveVendasCloud(v);
            System.out.println("Atualizou Vendas Cloud");
            System.out.println("-----------------------");
        }catch(SQLException ex){
            System.err.println("Erro: "+ex);
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void salvarSacola() {
        salvaSacolaLocal();
        salvaSacolaCloud();
        MensagemSistema.mostrarAvisoDark(this, "Tabela SACOLAS Atualizada!");   
        System.out.println("Atualizou Tabela Sacolas");
    }
    
    public void salvaSacolaLocal(){
        try{
            sdao.saveSacola(s);
            System.out.println("Atualizou Sacola Local");
            System.out.println("-----------------------");
        }catch(ClassNotFoundException | SQLException ex){           
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }
    
    public void salvaSacolaCloud(){
        try{
            sdao.saveSacolaCloud(s);
            System.out.println("Atualizou Sacola Cloud");
            System.out.println("-----------------------");
        }catch(ClassNotFoundException | SQLException ex){
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }
    
    private void salvarFrete() throws ClassNotFoundException {
        salvaFreteLocal();
        salvaFreteCloud();
        MensagemSistema.mostrarAvisoDark(this, "Tabela ENTREGAS Atualizada!");
        System.out.println("Atualizou Tabela Entregas");
        System.out.println("-----------------------");
        
    }
    
    public void salvaFreteLocal(){
        try {
            edao.inserirDadosComFrete(e);
            System.out.println("Atualizou Frete Local");
            System.out.println("-----------------------");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }
    
    public void salvaFreteCloud(){
        try {
            edao.inserirDadosComFreteCloud(e);
            System.out.println("Atualizou Frete Cloud");
            System.out.println("-----------------------");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }
    
    private void salvarSomenteFrete() {
        System.out.println("Iniciando registro de frete na base...");
        try {
            vdao.saveVendasCloud(v);
            vdao.saveVendas(v);
            edao.confirmarPagamentoRateioFreteTabelaEntregas(nomeCliente, BigDecimal.ZERO);
            MensagemSistema.mostrarAvisoDark(this, "Frete Confirmado e eventual rateio efetuado!");
            System.out.println("Finalizando registro de frete na base...");
            System.out.println("----------------------------------------");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaFinanceiro.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }
    
    public void limpaCamposTransacao(){       
        limpaButtons();
        limpaCampos();
    }
    
    public void limpaButtons(){
        ButtonGroup bg = new ButtonGroup();
        bg.add(buttonRetireLoja);
        bg.add(buttonEntregaEndereco);
        bg.clearSelection();
    }
    
    public void limpaCampos(){
        campoIdVenda.setText(String.valueOf(""));
        campoDataVenda.setText("");
        comboTipoPagamento.setSelectedIndex(0);
        comboOrigemVenda.setSelectedIndex(0);
        campoValorVenda.setText("");
        campoNomeCliente.setText("");
        campoCodPeca.setText("");
        campoObsVendas.setText("");
    }
    
    public void carregarCacheBuscaClientes() {
        if (listaCacheClientes != null && !listaCacheClientes.isEmpty()) {
            return;
        }
        try {
            System.out.println("Iniciando busca por nomes na base de clientes...");
            System.out.println("----------------------------------");
            cdao.buscaNomeClienteCloud(c); // Pega a string com todos os nomes separada por ";"
            String nomesStr = c.getNomeCli();
            if (nomesStr != null && !nomesStr.isEmpty()) {
                listaCacheClientes = Arrays.asList(nomesStr.split(";"));
                System.out.println("Lista Carregada da Base: "+listaCacheClientes);
                System.out.println("----------------------------------");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Erro ao carregar cache: " + ex.getMessage());
            System.out.println("----------------------------------");
        }
    }
    
    public void buscarSugestaoNome() throws ClassNotFoundException, SQLException {      
        nomeCliente = campoNomeCliente.getText().trim();
        System.out.println("Buscando: " + nomeCliente);       
        // Limpar resultados anteriores da caixinha visual
        listModel.clear();       
        // Esconder popup se a busca tiver menos de 2 caracteres
        if (nomeCliente.length() < 2) {
            if (popupSugestoes != null) {
                popupSugestoes.setVisible(false);
            }
            return;
        }      
        if (listaCacheClientes == null || listaCacheClientes.isEmpty()) {
            carregarCacheBuscaClientes();
        }       
        // FILTRAGEM LOCAL NA MEMÓRIA RAM
        if (listaCacheClientes != null && !listaCacheClientes.isEmpty()) {
            String termoUpper = nomeCliente.toUpperCase();
            for (String clienteItem : listaCacheClientes) {
                if (clienteItem.toUpperCase().contains(termoUpper)) {
                    listModel.addElement(clienteItem.trim());
                }
            }
        }
        
        // MOSTRAR POPUP SE HOUVER RESULTADOS
        if (listModel.getSize() > 0 && popupSugestoes != null) {
            if (!popupSugestoes.isVisible()) {
                SwingUtilities.invokeLater(() -> {
                    int larguraAtual = campoNomeCliente.getWidth() > 0 ? campoNomeCliente.getWidth() : 280;
                    popupSugestoes.getComponent(0).setPreferredSize(new Dimension(larguraAtual, 150));
                    popupSugestoes.pack();
                    
                    popupSugestoes.show(campoNomeCliente, 0, campoNomeCliente.getHeight());
                    campoNomeCliente.requestFocusInWindow();
                    System.out.println("Popup mostrado");
                });
            }
        } else {
            if (popupSugestoes != null) {
                popupSugestoes.setVisible(false);
            }
        }  
    }
    
    private void configurarBuscaAutomatica() {
        // Usar DocumentListener (melhor que KeyListener)
        campoNomeCliente.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                iniciarBuscaComDelay();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                iniciarBuscaComDelay();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                iniciarBuscaComDelay();
            }
            
            private void iniciarBuscaComDelay() {
                // Cancelar busca anterior
                if (buscaTimer != null && buscaTimer.isRunning()) {
                    buscaTimer.stop();
                }              
                // Aguardar 500ms após parar de digitar
                buscaTimer = new Timer(500, (ActionEvent evt) -> {
                    try {
                        if (!itemSelecionado) {
                            buscarSugestaoNome();
//                            buscarSugestaoNome2();
                        }
                    } catch (ClassNotFoundException | SQLException ex) {
                        System.err.println("Erro: "+ex);
                    }
                });
                buscaTimer.setRepeats(false);
                buscaTimer.start();
            }
        });
    }
    
    private void configurarCampoCliente() {
        campoNomeCliente.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    // Seta para baixo: navegar para próximo item
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                            if (popupSugestoes != null && popupSugestoes.isVisible() && listModel.getSize() > 0) {
                                listaSugestoes.requestFocus();
                                listaSugestoes.setSelectedIndex(0);
                            }   break;
                        case KeyEvent.VK_UP:
                            if (popupSugestoes != null && popupSugestoes.isVisible() && listModel.getSize() > 0) {
                                listaSugestoes.requestFocus();
                                listaSugestoes.setSelectedIndex(listModel.getSize() - 1);
                            }   break;
                        case KeyEvent.VK_ESCAPE:
                            if (popupSugestoes != null) {
                                popupSugestoes.setVisible(false);
                            }   break;
                        default:
                            buscarSugestaoNome();
//                            buscarSugestaoNome2();
                            break;
                    }
                } catch (ClassNotFoundException | SQLException ex) {
                    System.err.println("Erro: "+ex);
                }
            }
        });
    }
    
    private void configurarFoco() {
        campoNomeCliente.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Pequeno delay para permitir clique no popup
                Timer timer = new Timer(5000, event -> {
                    if (popupSugestoes != null && !listaSugestoes.hasFocus() && !campoNomeCliente.hasFocus()) {
                        popupSugestoes.setVisible(false);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }
    
    private void configurarCampo() {
        campoNomeCliente.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Ignorar teclas de navegação
                if (e.getKeyCode() == KeyEvent.VK_DOWN || 
                    e.getKeyCode() == KeyEvent.VK_UP ||
                    e.getKeyCode() == KeyEvent.VK_ENTER ||
                    e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    return;
                }              
                try {
                    buscarSugestaoNome();
//                    buscarSugestaoNome2();
                } catch (ClassNotFoundException | SQLException ex) {
                    System.err.println("Erro: "+ex);
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                // Navegar no popup
                if (e.getKeyCode() == KeyEvent.VK_DOWN && popupSugestoes.isVisible()) {
                    listaSugestoes.requestFocus();
                    if (listModel.getSize() > 0) {
                        listaSugestoes.setSelectedIndex(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && popupSugestoes.isVisible()) {
                    popupSugestoes.setVisible(false);
//                    popupAtivo = false;
                }
            }
        });
    }
    
    private void mostrarPopup() {
        if (popupSugestoes != null && campoNomeCliente.isShowing()) {
            // Garantir que o popup tem tamanho
            popupSugestoes.pack();
            // Mostrar abaixo do campo
            popupSugestoes.show(campoNomeCliente, 0, campoNomeCliente.getHeight());
            // Garantir que o popup fica visível
            popupSugestoes.setVisible(true);
        }
    }
    
    public void testarPopup() {
        if (listModel == null) {
            listModel = new DefaultListModel<>();
            listaSugestoes = new JList<>(listModel);
            listaSugestoes.setBackground(Color.WHITE);
            listaSugestoes.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            listaSugestoes.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    String selected = listaSugestoes.getSelectedValue();
                    if (selected != null) {
                        campoNomeCliente.setText(selected);
                        popupSugestoes.setVisible(false);
                    }
                }
            });
            JScrollPane scroll = new JScrollPane(listaSugestoes);
            scroll.setPreferredSize(new Dimension(250, 150));

            popupSugestoes = new JPopupMenu();
            popupSugestoes.add(scroll);
        }
        listModel.clear();
        listModel.addElement("Cliente Teste 1");
        listModel.addElement("Cliente Teste 2");
        listModel.addElement("Cliente Teste 3");

        // Mostrar popup
        popupSugestoes.show(campoNomeCliente, 0, campoNomeCliente.getHeight());
        System.out.println("Popup testado!");
    }
    
     private void esconderPopup() {
        if (popupSugestoes != null) {
            popupSugestoes.setVisible(false);
        }
    }
    
    private void setSelectedValue(String value) {
        campoNomeCliente.setText(value);
        popupSugestoes.setVisible(true);
        
        // Dispara evento para carregar dados do cliente
        campoNomeCliente.postActionEvent();
        
        // Coloca o cursor no final do texto
        campoNomeCliente.setCaretPosition(value.length());
    }
      
    private void adicionaListaDeItens() {
        ArrayList<String> produtoLista = new ArrayList();
        codPeca = campoCodPeca.getText().trim();   
        try {
            if(codPeca.contains(";")){                
                String[] listaProduto = codPeca.split(";");
                for(String produto : listaProduto){
                    String produtoLimpo = produto.trim();
                    if(!produtoLimpo.isEmpty()){
                        produtoLista.add(produtoLimpo);
                        System.out.println("Item: "+produtoLimpo);
                    }
                }
            }else{
                produtoLista.add(codPeca);
            }           
            System.out.println("Array de Itens: "+produtoLista.toString());
        } catch (NumberFormatException ex) {
            System.err.println("Erro: "+ex);
        }      
    }
    
    public void adicionarEmailParaEnvioCupom(){
        // ============================================================
        // INJEÇÃO DA CHAMADA DE E-MAIL COM BUSCA REAL NO BANCO
        // ============================================================
        String emailCliente = util.MensagemSistema.mostrarInputDark(this, "Enviar cupom não fiscal por e-mail?");
        String marcaPecaBanco = "BRECHÓ";
        String descricaoPecaBanco = "PEÇA";
        String formaPagamento = comboTipoPagamento.getSelectedItem().toString();
        double valorVenda = Double.parseDouble(campoValorVenda.getText());
        if (emailCliente != null && !emailCliente.isEmpty()) {
            if (emailCliente.contains("@") && emailCliente.contains(".")) {

                double totalCompra = 0.0;
                try { totalCompra = valorVenda; } catch (Exception ignored) {}

                // Recupera o modelo de dados da sua JTable de vendas
                javax.swing.table.DefaultTableModel modeloTabelaReal = (javax.swing.table.DefaultTableModel) tabelaVendas.getModel();

                // GERAÇÃO DINÂMICA DAS LINHAS COM BUSCA NO BANCO
                StringBuilder linhasItensHtml = new StringBuilder();
                int totalLinhas = modeloTabelaReal.getRowCount();

                for (int i = 0; i < totalLinhas; i++) {
                    String codPeca = String.valueOf(modeloTabelaReal.getValueAt(i, 5)).trim();
                    String valorItemTexto = String.valueOf(modeloTabelaReal.getValueAt(i, 4)).trim().replace(",", ".");

                    // 🔥 BUSCA REAL NO BANCO: Puxa os dados atualizados da peça usando o código dela
                    descricaoPecaBanco = "PEÇA";
                    marcaPecaBanco = "BRECHÓ";

                    try {
                        // Instancia o modelo temporário e passa o código
                        models.Produto pecaTemp = new models.Produto();
                        pecaTemp.setCodpeca(codPeca);

                        // Executa o seu método de busca do DAO (Ajuste o nome do método/classe se for diferente no seu projeto)
                        pdao.selectItemCloud(obs, p);
                        if (pecaTemp.getItemdescricao() != null) {
                            // Pega apenas a primeira palavra da descrição (Ex: "Vestido Longo" -> "VESTIDO")
                            descricaoPecaBanco = pecaTemp.getItemdescricao().trim().split(" ")[0].toUpperCase();
                        }
                        if (pecaTemp.getMarca() != null) {
                            marcaPecaBanco = pecaTemp.getMarca().trim().toUpperCase();
                        }
                    } catch (ClassNotFoundException | SQLException ex) {
                        System.err.println("Erro ao buscar dados da peça " + codPeca + " no banco: " + ex);
                    }

                    // Monta a linha com os dados REAIS vindos do banco de dados
                    linhasItensHtml.append("<tr>")
                                   .append("  <td>").append(codPeca).append(" ").append(descricaoPecaBanco).append(" ").append(marcaPecaBanco).append("</td>")
                                   .append("  <td style=\"text-align: center;\">1</td>")
                                   .append("  <td style=\"text-align: right;\">R$ ").append(valorItemTexto).append("</td>")
                                   .append("</tr>");
                }

                // Gera o HTML final passando as linhas já processadas com os nomes certos das roupas
                String htmlCupom = util.TemplateCupom.gerarHtmlCupom(idVenda, modeloTabelaReal, descricaoPecaBanco, marcaPecaBanco, totalCompra, formaPagamento);
                

                // Dispara o Socket TCP
                util.EmailService.enviarCupomAssincrono(emailCliente, htmlCupom, idVenda);

                util.MensagemSistema.mostrarAvisoDark(this, "Cupom digital enviado com sucesso!");
            } else {
                util.MensagemSistema.mostrarAvisoDark(this, "E-mail inválido! Cupom não enviado.");
            }
        }
        // ============================================================
    }
    
    //////////// METODOS ABA CONTAS FIXAS ////////////////////////
    
    private void configurarAbaContasFixas() {
        DefaultTableModel modeloExistente = new DefaultTableModel(
            new Object[][]{},
            new String[]{"DATA", "ORIGEM", "CREDOR", "DESCRIÇÃO", "VALOR", "CONTA PAGA"}
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Boolean.class; 
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; 
            }
        };

        jTable1.setModel(modeloExistente);

        jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);  // DATA
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(110); // ORIGEM
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(110); // CREDOR
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(220); // DESCRIÇÃO
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(80);  // VALOR
        jTable1.getColumnModel().getColumn(5).setPreferredWidth(80);  // CONTA PAGA (Checkbox)

        jTable1.getModel().addTableModelListener(e -> {
            // 🔥 TRAVA CRÍTICA ANTI-TRAVAMENTO: Se a tabela está carregando ou atualizando a estrutura, ignora!
            if (e.getType() == javax.swing.event.TableModelEvent.INSERT || e.getColumn() == javax.swing.event.TableModelEvent.ALL_COLUMNS) {
                return;
            }

            int row = e.getFirstRow();
            int column = e.getColumn();

            // Garante que a linha acessada é válida e que a alteração ocorreu estritamente no Checkbox (coluna 5)
            if (row >= 0 && row < modeloExistente.getRowCount() && column == 5) { 
                try {
                    Object valorCheckbox = modeloExistente.getValueAt(row, 5);

                    if (valorCheckbox instanceof Boolean) {
                        boolean statusPago = (Boolean) valorCheckbox;

                        // 🛡️ CORREÇÃO OPERACIONAL: Evita NullPointerException e quebra de tipos usando o .toString() seguro
                        Object objDescricao = modeloExistente.getValueAt(row, 3);
                        Object objValor = modeloExistente.getValueAt(row, 4);

                        if (objDescricao == null || objValor == null) {
                            return; // Linha vazia ou incompleta temporariamente
                        }

                        String ocorrencia = objDescricao.toString().trim(); 
                        String valorTexto = objValor.toString().trim(); 

                        dao.ContasFixasDAO cfdao = new dao.ContasFixasDAO();
                        cfdao.atualizarStatusPagamentoPorOcorrenciaCloud(ocorrencia, statusPago);

                        if (statusPago) {
                            models.Vendas despesaNova = new models.Vendas();
                            despesaNova.setOrigemVenda("DESPESA");
                            despesaNova.setTipoPag("DINHEIRO"); 
                            despesaNova.setValorVenda(valorTexto.replace(",", "."));
                            despesaNova.setObservacao("PGTO CONTA FIXA: " + ocorrencia);
                            despesaNova.setNomeCliente("FORNECEDOR / CONTA FIXA");
                            despesaNova.setCodPecas(String.valueOf(sem_codigo));
                            despesaNova.setStatus("ENTREGUE");
                            despesaNova.setDataVenda(new java.util.Date());

                            // dao.VendasDAO vendasDao = new dao.VendasDAO();
                            // vendasDao.salvarVendasAutomaticasCloud(despesaNova);

                            MensagemSistema.mostrarAvisoDark(this, 
                                "Conta paga com sucesso!\nUma despesa automática de R$ " + valorTexto + 
                                "\nfoi lançada no fluxo de caixa do dia de hoje.");
                        }
                        System.out.println("Status de Conta Fixa atualizado na Cloud para -> " + ocorrencia + ": " + statusPago);
                    }
                } catch (HeadlessException | ClassNotFoundException | SQLException ex) {
                    System.err.println("Aviso operacional controlado no Listener: " + ex.getMessage());
                }
            }
        });
    }


    public void carregarTabelaContasFixas() {
        try {
            // Força a remontagem limpa do esqueleto de 6 colunas
            configurarAbaContasFixas();
            
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0); 
            
            dao.ContasFixasDAO cfdao = new dao.ContasFixasDAO();
            List<models.ContasFixas> lista = cfdao.listarContasCloud();
            
            // Descarrega as 6 colunas visíveis da interface de forma coordenada e reta [links: 10]
            lista.forEach((cf) -> {
                model.addRow(new Object[]{
                    cf.getDataVencimentoTexto(), // DATA      (Coluna index 0) [links: 10]
                    "CONTA FIXA",                 // ORIGEM    (Coluna index 1)
                    cf.getCredor(),               // CREDOR    (Coluna index 2) ➔ Puxa o Credor da base!
                    cf.getDescricao(),            // DESCRIÇÃO (Coluna index 3)
                    cf.getValor().toString(),     // VALOR     (Coluna index 4) [links: 10]
                    cf.isPago()                   // CONTA PAGA(Coluna index 5) ➔ Boolean para o Checkbox [links: 10]
                });
            });
            System.out.println("Aba Contas Fixas carregada com sucesso e com a coluna CREDOR ativa.");
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Erro ao descarregar lista de contas fixas: " + ex.getMessage());
        }
    }

    

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane abaVendas;
    private javax.swing.JButton buttonAtualiza;
    private javax.swing.JButton buttonCTroca;
    private javax.swing.JButton buttonEditarContaFixa;
    private javax.swing.JRadioButton buttonEntregaEndereco;
    private javax.swing.JButton buttonEtiquetas;
    private javax.swing.JButton buttonExcluirContaFixa;
    private javax.swing.JButton buttonIncluirContaFixa;
    private javax.swing.JButton buttonInserirVendasDiaria;
    private javax.swing.JButton buttonLeitorQRCode;
    private javax.swing.JButton buttonLimpar;
    private javax.swing.JButton buttonMenuContasFixas;
    private javax.swing.JButton buttonMenuVendas;
    private javax.swing.JButton buttonPainelVendas;
    private javax.swing.JRadioButton buttonRetireLoja;
    private javax.swing.JButton buttonSalvar;
    private javax.swing.JButton buttonSalvarContaFixa;
    public javax.swing.JTextField campoCodPeca;
    private javax.swing.JTextField campoCredorContaFixa;
    private javax.swing.JTextField campoDataContaFixa;
    private javax.swing.JTextField campoDataVenda;
    private javax.swing.JTextField campoDescricaoContaFixa;
    private javax.swing.JTextField campoIdVenda;
    private javax.swing.JTextField campoNomeCliente;
    private javax.swing.JTextField campoObsVendas;
    private javax.swing.JTextField campoOrigemContaFixa;
    private javax.swing.JTextField campoValorContaFixa;
    public javax.swing.JTextField campoValorVenda;
    private javax.swing.JComboBox<String> comboOrigemVenda;
    private javax.swing.JComboBox<String> comboTipoPagamento;
    private javax.swing.JButton jButton19;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTable jTable1;
    public javax.swing.JTable tabelaVendas;
    // End of variables declaration//GEN-END:variables
 
}
