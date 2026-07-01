package views;

import dao.FornecedorDAO;
import java.awt.Image;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import dao.ProdutoDAO;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import models.Fornecedor;
import models.Produto;
import util.ConfigLoader;
import static views.TelaFornecedor.s;
import util.GeradorQRCode;
import util.ManipularImagem;
import util.MensagemSistema;
import util.ValorMonetarioUtil;

public class TelaEstoque extends javax.swing.JFrame { 
    
    public static String codigoDoItem;
    public static String precoDoItem;
    public static Date dataCadastroItem;
    public static String data;
    public static String codePeca;
    public static String lotePecas;
    LocalDateTime date;
    BufferedImage imagem;
    public static String caminho_inicial = ConfigLoader.get("sistema.caminho_inicial_pasta");
    public static String itens_loja = ConfigLoader.get("sistema.pasta_itens_loja");
    public static String entrada_itens = ConfigLoader.get("sistema.pasta_entrada_itens");
    public static String user = ConfigLoader.get("sistema.user");
    public final String pasta_loja = ""+caminho_inicial+user+itens_loja+"";
    public final String pasta_entrada = ""+caminho_inicial+user+entrada_itens+"";
    ButtonGroup bg = new ButtonGroup();
    ProdutoDAO pdao = new ProdutoDAO();
    Produto p = new Produto();
    FornecedorDAO fdao = new FornecedorDAO();
    Fornecedor f = new Fornecedor();
    int anoAtual = java.time.Year.now().getValue();
 
    @SuppressWarnings("LeakingThisInConstructor")
    public TelaEstoque() {
        this.setUndecorated(true);
        initComponents();
        ValorMonetarioUtil.aplicarMascaraEmCampos(
            campoValorPago,
            campoPrecoSugerido,
            campoLucroEstimado
        );
        configurarListenersCalculo();
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
                // --- 1. PALETA LUXO/MODA PREMIUM (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C

        jPanel1.setBackground(grafiteProfundo);
        jPanel1.setOpaque(true);       
        jLabel16.setForeground(brancoPuro);
        jLabel16.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));

        jLabel1.setForeground(brancoPuro);  jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        jLabel2.setForeground(brancoPuro);  jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        jLabel7.setForeground(brancoPuro);  jLabel7.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        javax.swing.JLabel[] labelsCamposComuns = {
            jLabel3, jLabel4, jLabel5, jLabel6, jLabel8, jLabel9, jLabel10, 
            jLabel11, jLabel12, jLabel13, jLabel14, jLabel15, jLabel17, jLabel18, jLabel19
        };
        for (javax.swing.JLabel lbl : labelsCamposComuns) {
            lbl.setForeground(brancoPuro);
            lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        }
        rButtonTipoDesapego.setForeground(douradoOuro);   rButtonTipoDesapego.setOpaque(false);
        rButtonTipoOutlet.setForeground(douradoOuro);     rButtonTipoOutlet.setOpaque(false);
        rButtonTipoConsignado.setForeground(douradoOuro); rButtonTipoConsignado.setOpaque(false);
        
        bg.add(rButtonTipoDesapego);
        bg.add(rButtonTipoOutlet);
        bg.add(rButtonTipoConsignado);
        
        javax.swing.JTextField[] todosCamposTexto = {
            campoCodigoFornecedor, campoNomeFornecedor, campoDescricaoItem, campoMarca, 
            campoTamanho, campoValorPago, campoPrecoSugerido, campoLucroEstimado, 
            campoPercentualLucro, campoCodigoItem, campoDataVenda, campoLotePecas
        };
        for (javax.swing.JTextField txt : todosCamposTexto) {
            txt.setBackground(grafiteClaro);
            txt.setForeground(brancoPuro);
            txt.setCaretColor(brancoPuro);
            txt.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        }       
        campoDataCadastroItem.setBackground(grafiteClaro);
        campoDataCadastroItem.setForeground(brancoPuro);
        campoDataCadastroItem.setCaretColor(brancoPuro);
        
        campoObservacoes.setOpaque(true);
        jScrollPane1.setOpaque(true);
        jScrollPane1.getViewport().setOpaque(true);

        // 2. Aplica as cores diretamente usando a UI básica (Burlar o Look and Feel)
        campoObservacoes.setBackground(grafiteClaro);
        campoObservacoes.setForeground(brancoPuro);
        campoObservacoes.setCaretColor(brancoPuro);

        jScrollPane1.setBackground(grafiteClaro);
        jScrollPane1.getViewport().setBackground(grafiteClaro);

        // 3. 🔥 O TRUQUE DEFINITIVO: Remove a borda padrão que força o fundo branco e cria uma customizada
        jScrollPane1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1),
            javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // 4. Limpa e reinicia o gerenciador de renderização do campo de texto
        campoObservacoes.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
        campoObservacoes.repaint();
        jScrollPane1.repaint();
        
        labelImagemItem.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
        javax.swing.JSeparator[] todosSeparadores = {
            jSeparator1, jSeparator2, jSeparator3, jSeparator4, jSeparator5, 
            jSeparator6, jSeparator7, jSeparator8, jSeparator9
        };
        for (javax.swing.JSeparator sep : todosSeparadores) {
            sep.setForeground(cinzaLinhas);
            sep.setBackground(cinzaLinhas);
        }
        javax.swing.JButton[] todosBotoesAcao = {
            buttonLimpar, buttonSalvar, buttonEditar, buttonExcluir, buttonPesquisar, 
            buttonMenu, buttonPesquisarFornecedor, buttonBarcode, buttonSelecionaImagem, buttonTirarFotos
        };
        for (javax.swing.JButton btn : todosBotoesAcao) {
            btn.setBackground(grafiteClaro);
            btn.setForeground(brancoPuro);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.putClientProperty("JButton.buttonType", "square");
        }                
        java.awt.Color bronzeAcobreado = new java.awt.Color(140, 120, 83); // #8C7853
        java.awt.Color textoBrancoPuro  = new java.awt.Color(255, 255, 255); // #FFFFFF        
        buttonMenu.setBackground(bronzeAcobreado);
        buttonMenu.setForeground(textoBrancoPuro);
        buttonMenu.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        buttonMenu.setFocusPainted(false);
        buttonMenu.setBorderPainted(false);
        buttonMenu.putClientProperty("JButton.buttonType", "square");
                // --- CONFIGURAÇÃO DA BARRA DE TÍTULO PREMIUM NA TELA DE ESTOQUE ---
        java.awt.Color grafiteClaroBarra = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuroBarra  = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuroBarra   = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color vermelhoSair      = new java.awt.Color(160, 40, 40);   // Hover vermelho para o X

        // 1. Cria o container da barra física
        javax.swing.JPanel barraTituloPremium = new javax.swing.JPanel();
        barraTituloPremium.setBackground(grafiteClaroBarra);
        barraTituloPremium.setOpaque(true);
        barraTituloPremium.setLayout(new java.awt.BorderLayout(15, 0));
        barraTituloPremium.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0)); 
        
        // 2. Lado Esquerdo: Nome do Cliente e do Sistema
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Módulo de Controle de Estoque");
        lblClienteBarra.setForeground(brancoPuroBarra);
        lblClienteBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        barraTituloPremium.add(lblClienteBarra, java.awt.BorderLayout.WEST);
        
        // 3. Lado Direito: Bloco Agrupador (Assinatura + Botão X)
        javax.swing.JPanel painelDireitoBarra = new javax.swing.JPanel();
        painelDireitoBarra.setBackground(grafiteClaroBarra);
        painelDireitoBarra.setOpaque(true);
        painelDireitoBarra.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 0));
        
        // Texto de Desenvolvimento de Grife
        javax.swing.JLabel lblDevBarra = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDevBarra.setForeground(douradoOuroBarra); 
        lblDevBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        painelDireitoBarra.add(lblDevBarra);
        
        // Botão X: Plano e integrado para fechar a tela (Usa dispose para voltar ao menu sem derrubar o sistema)
        javax.swing.JButton btnFecharJanela = new javax.swing.JButton(" X ");
        btnFecharJanela.setBackground(grafiteClaroBarra);
        btnFecharJanela.setForeground(brancoPuroBarra);
        btnFecharJanela.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnFecharJanela.setFocusPainted(false);
        btnFecharJanela.setBorderPainted(false);
        btnFecharJanela.setPreferredSize(new java.awt.Dimension(45, 30)); 
        btnFecharJanela.putClientProperty("JButton.buttonType", "square"); 
        
        // Efeito Visual de acender em vermelho ao passar o mouse
        btnFecharJanela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnFecharJanela.setBackground(vermelhoSair);
                btnFecharJanela.setForeground(brancoPuroBarra);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnFecharJanela.setBackground(grafiteClaroBarra);
                btnFecharJanela.setForeground(brancoPuroBarra);
            }
        });
        
        // Ação de fechamento seguro (Apenas fecha o formulário atual e limpa a memória)
        btnFecharJanela.addActionListener(e -> {
            this.dispose();
        });
        
        painelDireitoBarra.add(btnFecharJanela);
        barraTituloPremium.add(painelDireitoBarra, java.awt.BorderLayout.EAST);
        
        // 4. Posiciona e estica a barra no topo exato da tela de estoque
        barraTituloPremium.setBounds(0, 0, this.getWidth(), 30);

        // 5. Motor de Movimentação utilizando a referência corrigida de arrays finais
        final int[] coordX = {0};
        final int[] coordY = {0};
        final javax.swing.JFrame janelaAtual = this; 
        
        barraTituloPremium.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                coordX[0] = evt.getX();
                coordY[0] = evt.getY();
            }
        });
        
        barraTituloPremium.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                janelaAtual.setLocation(evt.getXOnScreen() - coordX[0], evt.getYOnScreen() - coordY[0]);
            }
        });

        // 6. Inserção Inteligente no topo do painel principal (Tratamento para jPanel1 da sua tela)
        try {
            jPanel1.add(barraTituloPremium);
            jPanel1.revalidate();
            jPanel1.repaint();
        } catch(Exception e) {
            this.getContentPane().add(barraTituloPremium);
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
        }
        
        campoPrecoSugerido.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
		@Override public void insertUpdate(javax.swing.event.DocumentEvent e) { calcularLucroAutomatico(); }
		@Override public void removeUpdate(javax.swing.event.DocumentEvent e) { calcularLucroAutomatico(); }
		@Override public void changedUpdate(javax.swing.event.DocumentEvent e) { calcularLucroAutomatico(); }
	});
	campoValorPago.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
		@Override public void insertUpdate(javax.swing.event.DocumentEvent e) { calcularLucroAutomatico(); }
		@Override public void removeUpdate(javax.swing.event.DocumentEvent e) { calcularLucroAutomatico(); }
		@Override public void changedUpdate(javax.swing.event.DocumentEvent e) { calcularLucroAutomatico(); }
	});
	rButtonTipoDesapego.requestFocus();
        
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(grafiteProfundo);
        this.setTitle(util.ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI LTDA"); 
    }  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jFrame2 = new javax.swing.JFrame();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jProgressBar1 = new javax.swing.JProgressBar();
        jSeparator8 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rButtonTipoDesapego = new javax.swing.JRadioButton();
        rButtonTipoOutlet = new javax.swing.JRadioButton();
        rButtonTipoConsignado = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        campoCodigoFornecedor = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        campoNomeFornecedor = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        campoDescricaoItem = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        campoMarca = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        campoTamanho = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        campoValorPago = new javax.swing.JTextField();
        campoPrecoSugerido = new javax.swing.JTextField();
        campoLucroEstimado = new javax.swing.JTextField();
        campoPercentualLucro = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        campoCodigoItem = new javax.swing.JTextField();
        buttonLimpar = new javax.swing.JButton();
        buttonSalvar = new javax.swing.JButton();
        buttonEditar = new javax.swing.JButton();
        buttonExcluir = new javax.swing.JButton();
        buttonPesquisar = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        campoObservacoes = new javax.swing.JTextPane();
        labelImagemItem = new javax.swing.JLabel();
        buttonMenu = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        buttonPesquisarFornecedor = new javax.swing.JButton();
        campoDataCadastroItem = new javax.swing.JFormattedTextField();
        buttonBarcode = new javax.swing.JButton();
        campoDataVenda = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator9 = new javax.swing.JSeparator();
        buttonSelecionaImagem = new javax.swing.JButton();
        buttonTirarFotos = new javax.swing.JButton();
        campoLotePecas = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame2Layout = new javax.swing.GroupLayout(jFrame2.getContentPane());
        jFrame2.getContentPane().setLayout(jFrame2Layout);
        jFrame2Layout.setHorizontalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame2Layout.setVerticalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SóBrechó");
        setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setPreferredSize(new java.awt.Dimension(1000, 700));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 30)); // NOI18N
        jLabel1.setText("ESTOQUE");

        rButtonTipoDesapego.setBackground(new java.awt.Color(204, 204, 255));
        rButtonTipoDesapego.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        rButtonTipoDesapego.setText("DESAPEGO");
        rButtonTipoDesapego.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rButtonTipoDesapego.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rButtonTipoDesapegoActionPerformed(evt);
            }
        });

        rButtonTipoOutlet.setBackground(new java.awt.Color(204, 204, 255));
        rButtonTipoOutlet.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        rButtonTipoOutlet.setText("OUTLET");
        rButtonTipoOutlet.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rButtonTipoOutlet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rButtonTipoOutletActionPerformed(evt);
            }
        });

        rButtonTipoConsignado.setBackground(new java.awt.Color(204, 204, 255));
        rButtonTipoConsignado.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        rButtonTipoConsignado.setText("CONSIGNADO");
        rButtonTipoConsignado.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rButtonTipoConsignado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rButtonTipoConsignadoActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        jLabel2.setText("TIPO");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel3.setText("FORNECEDOR");

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel4.setText("CÓDIGO");

        campoCodigoFornecedor.setColumns(10);
        campoCodigoFornecedor.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCodigoFornecedor.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCodigoFornecedor.setToolTipText("max 10 números");
        campoCodigoFornecedor.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoCodigoFornecedor.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel5.setText("NOME");

        campoNomeFornecedor.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoNomeFornecedor.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel6.setText("DATA");

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setText("ITEM");

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel8.setText("DESCRIÇÃO");

        campoDescricaoItem.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoDescricaoItem.setToolTipText("Usar pelo menos 3 caracteristicas");
        campoDescricaoItem.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel9.setText("MARCA");

        campoMarca.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoMarca.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel10.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel10.setText("TAMANHO");

        campoTamanho.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoTamanho.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoTamanho.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoTamanho.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoTamanhoActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel11.setText("PREÇO SUGERIDO");

        jLabel12.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel12.setText("LUCRO ESTIMADO");

        jLabel13.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel13.setText("% LUCRO");

        campoValorPago.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoValorPago.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoValorPago.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        campoPrecoSugerido.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoPrecoSugerido.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoPrecoSugerido.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        campoLucroEstimado.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoLucroEstimado.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoLucroEstimado.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoLucroEstimado.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoLucroEstimadoMouseClicked(evt);
            }
        });
        campoLucroEstimado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoLucroEstimadoActionPerformed(evt);
            }
        });

        campoPercentualLucro.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoPercentualLucro.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoPercentualLucro.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoPercentualLucro.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoPercentualLucroMouseClicked(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel14.setText("VALOR PAGO");

        jLabel15.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel15.setText("CÓDIGO ");

        campoCodigoItem.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCodigoItem.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCodigoItem.setToolTipText("Ex:1425874521");
        campoCodigoItem.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoCodigoItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoCodigoItemMouseClicked(evt);
            }
        });
        campoCodigoItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoCodigoItemActionPerformed(evt);
            }
        });

        buttonLimpar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonLimpar.setText("LIMPAR");
        buttonLimpar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLimparActionPerformed(evt);
            }
        });

        buttonSalvar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonSalvar.setText("SALVAR");
        buttonSalvar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSalvarActionPerformed(evt);
            }
        });

        buttonEditar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonEditar.setText("EDITAR");
        buttonEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditarActionPerformed(evt);
            }
        });

        buttonExcluir.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonExcluir.setText("EXCLUIR");
        buttonExcluir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExcluirActionPerformed(evt);
            }
        });

        buttonPesquisar.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonPesquisar.setText("PESQUISAR");
        buttonPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPesquisarActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel16.setText("FOTO DO ITEM");

        jLabel18.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel18.setText("OBSERVAÇÕES");

        campoObservacoes.setBackground(new java.awt.Color(0, 0, 0));
        campoObservacoes.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoObservacoes.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jScrollPane1.setViewportView(campoObservacoes);

        labelImagemItem.setBackground(new java.awt.Color(255, 255, 255));
        labelImagemItem.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        buttonMenu.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenu.setText("MENU");
        buttonMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuActionPerformed(evt);
            }
        });

        buttonPesquisarFornecedor.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonPesquisarFornecedor.setText("PESQUISAR");
        buttonPesquisarFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPesquisarFornecedorActionPerformed(evt);
            }
        });

        campoDataCadastroItem.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoDataCadastroItem.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter()));
        campoDataCadastroItem.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoDataCadastroItem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        campoDataCadastroItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoDataCadastroItemMouseClicked(evt);
            }
        });

        buttonBarcode.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonBarcode.setText("QR CODE");
        buttonBarcode.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBarcodeActionPerformed(evt);
            }
        });

        campoDataVenda.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoDataVenda.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoDataVenda.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoDataVendaMouseClicked(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel17.setText("DATA VENDA");

        buttonSelecionaImagem.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        buttonSelecionaImagem.setText("Selecionar Imagem");
        buttonSelecionaImagem.setBorder(null);
        buttonSelecionaImagem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelecionaImagemActionPerformed(evt);
            }
        });

        buttonTirarFotos.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        buttonTirarFotos.setText("Tirar Fotos");
        buttonTirarFotos.setBorder(null);
        buttonTirarFotos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTirarFotosActionPerformed(evt);
            }
        });

        campoLotePecas.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoLotePecas.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoLotePecas.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoLotePecas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoLotePecasActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel19.setText("LOTE");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(rButtonTipoDesapego, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(rButtonTipoOutlet, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(rButtonTipoConsignado))
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(buttonTirarFotos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(buttonSelecionaImagem, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jSeparator7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelImagemItem, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(105, 105, 105))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 837, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 838, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 838, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 838, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 838, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel17)))
                                        .addGap(10, 10, 10)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(campoDataVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(buttonBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(16, 16, 16)
                                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(campoDescricaoItem, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(campoLotePecas, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel8)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(campoCodigoItem, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(36, 36, 36)
                                                .addComponent(jLabel15)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(campoMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel9)
                                                .addGap(47, 47, 47)))))
                                .addComponent(jLabel18))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(campoCodigoFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(campoNomeFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 432, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoDataCadastroItem, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(buttonPesquisarFornecedor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel14)
                                .addGap(95, 95, 95))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(campoTamanho, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoValorPago, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(campoPrecoSugerido, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(campoLucroEstimado, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(campoPercentualLucro, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel16))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(11, 11, 11)))
                        .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(buttonSelecionaImagem, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(rButtonTipoDesapego)
                                    .addComponent(rButtonTipoOutlet)
                                    .addComponent(rButtonTipoConsignado)))
                            .addComponent(buttonTirarFotos, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(jLabel3))
                    .addComponent(labelImagemItem, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoCodigoFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoNomeFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonPesquisarFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoDataCadastroItem, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(campoDataVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel17)
                        .addComponent(buttonBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel15)
                    .addComponent(jLabel9)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(campoLotePecas, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(campoDescricaoItem, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(campoCodigoItem, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(campoMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(jLabel14)
                        .addComponent(jLabel12)
                        .addComponent(jLabel13)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(campoTamanho, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoValorPago, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoPrecoSugerido, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoLucroEstimado, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoPercentualLucro, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(buttonMenu)
                        .addComponent(buttonLimpar)
                        .addComponent(buttonEditar)
                        .addComponent(buttonExcluir)
                        .addComponent(buttonSalvar)))
                .addGap(49, 49, 49))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1018, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 734, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
/////// Botao Menu //////
    private void buttonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuActionPerformed
        new TelaMenu().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonMenuActionPerformed
////// Botao Pesquisar Item //////
    private void buttonPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPesquisarActionPerformed
        s = MensagemSistema.mostrarInputDark(this, "Entre com Código do item:");
        buscarItemNaBase();
        if(!(codePeca == null)){            
            if("Desapego".equals(p.getTipoitem())){
                rButtonTipoDesapego.doClick();
                System.out.println("Desapego");
            }
            if("Outlet".equals(p.getTipoitem())){
                rButtonTipoOutlet.doClick();
                System.out.println("Outlet");
            }
            if("Consignado".equals(p.getTipoitem())){
                rButtonTipoConsignado.doClick();
                System.out.println("Consignado");
            }
            
            campoCodigoFornecedor.setText(p.getCodforn());
            campoNomeFornecedor.setText(p.getNomeforn());
            campoDataCadastroItem.setText(p.getData().toString().replaceAll("-", "/"));
            String data = campoDataCadastroItem.getText();
            System.out.println(data);
            String dia = data.substring(8,10);
            String mes = data.substring(5,7);
            String ano = data.substring(0,4);
            campoDataCadastroItem.setText(""+dia+"/"+mes+"/"+ano+"");
            System.out.println("Data cadastro item: "+campoDataCadastroItem.getText());
            campoDescricaoItem.setText(p.getItemdescricao());
            campoCodigoItem.setText(p.getCodpeca());
            campoMarca.setText(p.getMarca());
            campoTamanho.setText(p.getTamanho());
            campoValorPago.setText(String.valueOf(p.getValorpago()));
            campoPrecoSugerido.setText(String.valueOf(p.getPrecosugerido()));
            campoLucroEstimado.setText(String.valueOf(p.getLucroestimado()));
            campoPercentualLucro.setText(String.valueOf(p.getPercentlucro()));
            campoObservacoes.setText(p.getObservacao());
            selecionaImagemItem();
            if(selecionaImagemItem().equals("") || selecionaImagemItem() == null){
                System.out.println(">>>>>>>>>>Item não possui imagem<<<<<<<<<<<!");
            }
            campoDataVenda.setText(String.valueOf(p.getDatavenda()));
            System.out.println("Data Venda: "+(p.getDatavenda()));
            if(p.getDatavenda() == null){
                System.out.println(">>>>>>>>>Item sem data de venda<<<<<<<<<<<<<");
                campoDataVenda.setText("");
            }else{
                String dataVenda = campoDataVenda.getText();
                System.out.println(dataVenda);
                String diaVenda = dataVenda.substring(8,10);
                String mesVenda = dataVenda.substring(5,7);
                String anoVenda = dataVenda.substring(0,4);
                campoDataVenda.setText(""+diaVenda+"/"+mesVenda+"/"+anoVenda+"");
                System.out.println("Data Venda: "+campoDataVenda.getText());
            }
            MensagemSistema.mostrarAvisoDark(this, "PESQUISA REALIZA COM SUCESSO!");
            buttonSalvar.setEnabled(false);
        }else{
            MensagemSistema.mostrarAvisoDark(this, "ITEM NÃO ENCONTRADO NA BASE!");
            System.err.println("Código não encontrado na pesquisa");
            System.err.println("---------------------------------");
        }
    }//GEN-LAST:event_buttonPesquisarActionPerformed
////// Botao Excluir //////
    private void buttonExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExcluirActionPerformed
        boolean resposta = MensagemSistema.mostrarDecisaoDark(this, "Deseja excluir o registro?");
        if(resposta == true){
            if(!(rButtonTipoDesapego.isSelected() || rButtonTipoOutlet.isSelected() || rButtonTipoConsignado.isSelected()) && campoCodigoFornecedor.getText().isEmpty() && campoDataCadastroItem.getText().isEmpty() && campoDescricaoItem.getText().isEmpty() && campoCodigoItem.getText().isEmpty() && campoValorPago.getText().isEmpty() && campoPrecoSugerido.getText().isEmpty() && campoLucroEstimado.getText().isEmpty() && campoPercentualLucro.getText().isEmpty()){
                MensagemSistema.mostrarAvisoDark(this, "Nenhum campo preenchido para ser excluido!");
            }else{
                cleanFields();
                apagarItemNaBase();
                System.out.println("Registro excluido com sucesso!");
                MensagemSistema.mostrarAvisoDark(this, "Registro excluido com sucesso!");
                buttonSalvar.setEnabled(true);
                buttonPesquisar.setEnabled(true);
            }
        }else{
            JOptionPane.showMessageDialog(this, "Registro não foi excluido!");
            System.err.println("Registro não foi excluido!");
        }        
    }//GEN-LAST:event_buttonExcluirActionPerformed
////// Botao Editar ///////
    private void buttonEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditarActionPerformed
        boolean resposta = MensagemSistema.mostrarDecisaoDark(this, "Deseja alterar esse item?");
        if(resposta == true){
            if(!(rButtonTipoDesapego.isSelected() || rButtonTipoOutlet.isSelected() || rButtonTipoConsignado.isSelected()) && campoCodigoFornecedor.getText().isEmpty() && campoNomeFornecedor.getText().isEmpty() && campoDataCadastroItem.getText().isEmpty() && campoDescricaoItem.getText().isEmpty() && campoCodigoItem.getText().isEmpty() && campoValorPago.getText().isEmpty() && campoPrecoSugerido.getText().isEmpty() && campoLucroEstimado.getText().isEmpty() && campoPercentualLucro.getText().isEmpty()){
                MensagemSistema.mostrarAvisoDark(this, "Campos obrigatórios devem estar preenchidos para edição!");
            }else{
                p.setTipoitem(rButtonTipoDesapego.getText());
                if(rButtonTipoDesapego.getText() == null){
                    p.setTipoitem(rButtonTipoOutlet.getText());
                }
                if(rButtonTipoDesapego.getText() == null && rButtonTipoOutlet.getText() == null){
                    p.setTipoitem(rButtonTipoConsignado.getText());
                }
                p.setCodforn(campoCodigoFornecedor.getText());
                p.setNomeforn(campoNomeFornecedor.getText());
                System.out.println(campoCodigoFornecedor.getText());
                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    p.setData(fmt.parse(campoDataCadastroItem.getText()));
                    System.out.println(campoDataCadastroItem.getText());                       
                    System.out.println("-------------------------------");

                } catch (ParseException ex) {
                    Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro na data: "+ex.getMessage());
                    System.out.println("-----------------------------------");
                }
                p.setItemdescricao(campoDescricaoItem.getText());
                p.setCodpeca(campoCodigoItem.getText());
                p.setMarca(campoMarca.getText());
                p.setTamanho(campoTamanho.getText());
                p.setValorpago(Double.parseDouble(campoValorPago.getText()));
                p.setPrecosugerido(Double.parseDouble(campoPrecoSugerido.getText()));
                p.setLucroestimado(Double.parseDouble(campoLucroEstimado.getText()));
                p.setPercentlucro(Integer.parseInt(campoPercentualLucro.getText()));
                p.setObservacao(campoObservacoes.getText());                   
                if((campoValorPago.getText().isEmpty()) || (campoPrecoSugerido.getText().isEmpty()) || (campoCodigoFornecedor.getText().isEmpty())){
                    MensagemSistema.mostrarAvisoDark(this, "Campos mandatórios devem estar preenchidos: Tipo, Código, Nome, Data!");
                }else{
                    atualizarItemNaBase();
                    cleanFields();
                    buttonSalvar.setEnabled(true);
                    buttonPesquisar.setEnabled(true);
                }               
            }
        }else{
            System.err.println("Cancelou a alteração");
            System.err.println("-----------------------");
        }
    }//GEN-LAST:event_buttonEditarActionPerformed
////// Botao Salvar //////
    private void buttonSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSalvarActionPerformed
        if(!(rButtonTipoDesapego.isSelected() || rButtonTipoOutlet.isSelected() || rButtonTipoConsignado.isSelected()) || campoCodigoFornecedor.getText().isEmpty() || campoNomeFornecedor.getText().isEmpty() || campoDataCadastroItem.getText().isEmpty() || campoDescricaoItem.getText().isEmpty() || campoCodigoItem.getText().isEmpty() || campoValorPago.getText().isEmpty() || campoPrecoSugerido.getText().isEmpty() || campoLucroEstimado.getText().isEmpty() || campoPercentualLucro.getText().isEmpty()){
            MensagemSistema.mostrarAvisoDark(this, "Campo obrigatórios como Tipo de Item, Cod Fornecedor, Nome Fornecedor, Data Cadastro, Descrição do item, Lote, Código peça, Valor Pago, Preço Sugerido, Lucro Estimado e %Lucro devem estar preenchidos!");
        }else{
            if(rButtonTipoDesapego.isSelected()){
                p.setTipoitem(rButtonTipoDesapego.getText());
            }
            if(rButtonTipoOutlet.isSelected()){
                p.setTipoitem(rButtonTipoOutlet.getText());
            }
            if(rButtonTipoConsignado.isSelected()){
                p.setTipoitem(rButtonTipoConsignado.getText());
            }            
            p.setCodforn(campoCodigoFornecedor.getText());
            p.setNomeforn(campoNomeFornecedor.getText());          
            p.setData(Date.valueOf(data));
            System.out.println("Data cadastro do item para base: "+data);           
            p.setItemdescricao(campoDescricaoItem.getText());
            p.setUltimoLote(campoLotePecas.getText());
            p.setCodpeca(campoCodigoItem.getText());
            p.setMarca(campoMarca.getText());
            p.setTamanho(campoTamanho.getText());
            
            // 🚀 TRATAMENTO BLINDADO CONTRA VÍRGULAS E PONTOS DE MILHAR
            String txtValorPago = campoValorPago.getText().trim().replace(".", "").replace(",", ".");
            String txtPrecoSugerido = campoPrecoSugerido.getText().trim().replace(".", "").replace(",", ".");
            String txtLucroEstimado = campoLucroEstimado.getText().trim().replace(".", "").replace(",", ".");
            
            // Remove o sinal de '%' e trata a vírgula para pegar apenas a parte numérica inteira
            String txtPercentual = campoPercentualLucro.getText().trim().replace("%", "").replace(".", "").replace(",", ".");
            // Caso venha algo como "15.4", divide no ponto e pega apenas a parte inteira "15"
            if(txtPercentual.contains(".")) {
                txtPercentual = txtPercentual.split("\\.")[0];
            }

            // Envia os valores limpos e convertidos com segurança para o objeto
            p.setValorpago(Double.parseDouble(txtValorPago));
            p.setPrecosugerido(Double.parseDouble(txtPrecoSugerido));
            p.setLucroestimado(Double.parseDouble(txtLucroEstimado));
            p.setPercentlucro(Integer.parseInt(txtPercentual));
            
            p.setObservacao(campoObservacoes.getText());
            p.setStatus("DISPONIVEL");            
            if(labelImagemItem.getIcon() != null){
                salvaImagemBanco();
            }else{
                System.err.println("Ocorreu um erro!");
                MensagemSistema.mostrarAvisoDark(this, "Não foi possivel salvar a imagem no banco");
            }                           
            salvarItemNaBase();
            cleanFields();
//            try {
//                atualizarSiteBrecho();
//            } catch (InterruptedException ex) {
//                Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }       
    }//GEN-LAST:event_buttonSalvarActionPerformed
/////// Botao Limpar //////
    private void buttonLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimparActionPerformed
        cleanFields();
    }//GEN-LAST:event_buttonLimparActionPerformed

///// RB Tipo Consignado //////
    private void rButtonTipoConsignadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rButtonTipoConsignadoActionPerformed
        if(rButtonTipoConsignado.isSelected()){
            System.out.println("Selecionou CONSIGNADO");
        }
    }//GEN-LAST:event_rButtonTipoConsignadoActionPerformed
/////// RB Tipo Outlet //////
    private void rButtonTipoOutletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rButtonTipoOutletActionPerformed
        if(rButtonTipoOutlet.isSelected()){
            System.out.println("Selecionou OUTLET");
        }
    }//GEN-LAST:event_rButtonTipoOutletActionPerformed
/////// RB Tipo Desapego //////
    private void rButtonTipoDesapegoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rButtonTipoDesapegoActionPerformed
        if(rButtonTipoDesapego.isSelected()){
            System.out.println("Selecionou DESAPEGO");
        }
    }//GEN-LAST:event_rButtonTipoDesapegoActionPerformed
////// Botao Pesquisa Fornecedor //////
    private void buttonPesquisarFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPesquisarFornecedorActionPerformed
        s = MensagemSistema.mostrarInputDark(this, "Entre com Código do Fornecedor");       
        if(!(s == null || s.isEmpty() || s.length() <= 0 )){//valida dados de pesquisa
            try{
                fdao.selectFornecedorCloud(s, f);
                campoCodigoFornecedor.setText(f.getCodforn());
                campoNomeFornecedor.setText(f.getNomeforn());
                campoLotePecas.setText(f.getUltimoLote());
                System.out.println(f.getUltimoLote());
            } catch(IOException | ClassNotFoundException | SQLException ex){
                System.err.println("Erro: "+ ex.getMessage());
                System.err.println("----------------------");
            }
        }else{
            if(campoCodigoFornecedor.getText() == null || campoNomeFornecedor.getText() == null || campoCodigoFornecedor.getText().isEmpty() || campoNomeFornecedor.getText().isEmpty()){
                MensagemSistema.mostrarAvisoDark(this, "Fornecedor inválido ou não cadastrado!");
                System.out.println("Fornecedor não cadastrado!"); 
            }else{
                MensagemSistema.mostrarAvisoDark(this, "Erro desconhecido!");
                System.err.println("Erro desconhecido!");
            }            
        }
    }//GEN-LAST:event_buttonPesquisarFornecedorActionPerformed

///// Campo código peça //////
    private void campoCodigoItemMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoCodigoItemMouseClicked
        int x = 0;
        try {
            pdao.selectCodPecaCloud(p);
            String codigo = p.getCodpeca();
            System.out.println("Ultimo Codigo na base: "+codigo);
            if(codigo != null){
                x = Integer.parseInt(codigo);
                String codPeca = String.valueOf(x + 1);
                campoCodigoItem.setText(codPeca);
                System.out.println("Novo Codigo: "+codPeca);
            }else{
                campoCodigoItem.setText("1");
            }          
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_campoCodigoItemMouseClicked
////// Campo Lucro estimado //////
    private void campoLucroEstimadoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoLucroEstimadoMouseClicked
        calcularLucroEstimado();
    }//GEN-LAST:event_campoLucroEstimadoMouseClicked
////// Campo Percentual Lucro //////
    private void campoPercentualLucroMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoPercentualLucroMouseClicked
//        if(!(campoValorPago.getText().isEmpty() && campoLucroEstimado.getText().isEmpty())){
//            String vp = campoValorPago.getText();
//            double valorPago = Double.parseDouble(vp);
//            String le = campoLucroEstimado.getText();
//            double lucroEst = Double.parseDouble(le);
//            double calc = (lucroEst*100);
//            double perc = calc/valorPago;
//            System.out.println("Percentual: "+perc);
//            int percentual = (int)perc;
//            System.out.println(percentual);
//            campoPercentualLucro.setText(Integer.toString(percentual));lll
//        }else{
//            System.err.println("Campos Valor Pago e Lucro Estimado não foram preenchidos!");
//            MensagemSistema.mostrarAvisoDark(this, "Campos Valor Pago e Lucro Estimado devem estar preenchidos!");
//        }
        calcularPercentualLucro();
    }//GEN-LAST:event_campoPercentualLucroMouseClicked

    private void campoDataCadastroItemMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoDataCadastroItemMouseClicked
        date = LocalDateTime.now();
        data = date.format(DateTimeFormatter.ISO_DATE);
        String dia = data.substring(8,10);
        String mes = data.substring(5,7);
        String ano = data.substring(0,4);
        campoDataCadastroItem.setText(""+dia+"/"+mes+"/"+ano+"");
    }//GEN-LAST:event_campoDataCadastroItemMouseClicked

    private void campoTamanhoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoTamanhoActionPerformed
        String tamanho = campoTamanho.getText();
        campoTamanho.setText(tamanho.toUpperCase());
    }//GEN-LAST:event_campoTamanhoActionPerformed

    private void buttonBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBarcodeActionPerformed
        gerarQRCodeItemEstoque();
    }//GEN-LAST:event_buttonBarcodeActionPerformed

    private void campoDataVendaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoDataVendaMouseClicked
        campoDataVenda.setText("");
    }//GEN-LAST:event_campoDataVendaMouseClicked

    private void buttonSelecionaImagemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelecionaImagemActionPerformed
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File arquivo = fc.getSelectedFile();
            try {
                imagem = ManipularImagem.setImagemDimensao(arquivo.getAbsolutePath(), 170, 250);
                labelImagemItem.setIcon(new ImageIcon(imagem));
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                System.err.println("Erro ao carregar imagem!");
            }
            arquivo.delete();
        } else {
            MensagemSistema.mostrarAvisoDark(this, "NENHUM ARQUIVO SELECIONADO!");
        }
    }//GEN-LAST:event_buttonSelecionaImagemActionPerformed

    private void buttonTirarFotosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTirarFotosActionPerformed
        new Foto().setVisible(true);
        this.setLocationRelativeTo(null);
    }//GEN-LAST:event_buttonTirarFotosActionPerformed

    private void campoLotePecasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoLotePecasActionPerformed
        lotePecas = campoLotePecas.getText();
    }//GEN-LAST:event_campoLotePecasActionPerformed

    private void campoLucroEstimadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoLucroEstimadoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoLucroEstimadoActionPerformed

    private void campoCodigoItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoCodigoItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoCodigoItemActionPerformed

    ////////////////// METODOS DA CLASSE ///////////////////////
    // ==========================================
    // CONFIGURAR LISTENERS
    // ==========================================
    private void configurarListenersCalculo() {
        // KeyReleased - calcula ao digitar
        campoValorPago.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularLucroEstimado();
            }
        });

        campoPrecoSugerido.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularLucroEstimado();
            }
        });

        // FocusLost - calcula ao sair do campo
        campoValorPago.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                calcularLucroEstimado();
            }
        });

        campoPrecoSugerido.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                calcularLucroEstimado();
            }
        });

        campoLucroEstimado.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                calcularPercentualLucro();
            }
        });
    }

    // ==========================================
    // CALCULAR LUCRO ESTIMADO
    // ==========================================
    private void calcularLucroEstimado() {
        try {
            // ==========================================
            // 🔥 TRATAR VALORES DIGITADOS
            // ==========================================
            double precoSug = ValorMonetarioUtil.getValorDoCampo(campoPrecoSugerido);
            double valorPago = ValorMonetarioUtil.getValorDoCampo(campoValorPago);

            // Verifica se os campos estão preenchidos
            if (precoSug == 0 && valorPago == 0) {
                return;
            }

            double lucro = precoSug - valorPago;

            // ==========================================
            // 🔥 ARREDONDA E EXIBE
            // ==========================================
            java.math.BigDecimal lucroArredondado = new java.math.BigDecimal(lucro)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            ValorMonetarioUtil.setValorNoCampo(campoLucroEstimado, lucroArredondado.doubleValue());

            // Atualiza percentual
            calcularPercentualLucro();

            System.out.println("💰 Preço Sugerido: " + precoSug);
            System.out.println("💰 Valor Pago: " + valorPago);
            System.out.println("💰 Lucro: " + lucroArredondado.doubleValue());

        } catch (Exception e) {
            System.err.println("❌ Erro ao calcular lucro: " + e.getMessage());
        }
    }
    
    // ==========================================
    // CALCULAR PERCENTUAL DE LUCRO
    // ==========================================
    private void calcularPercentualLucro() {
        try {
            String vp = campoValorPago.getText().trim().replace(",", ".");
            String le = campoLucroEstimado.getText().trim().replace(",", ".");

            // Verifica se os campos estão preenchidos
            if (vp.isEmpty() || le.isEmpty()) {
                System.out.println("⚠️ Campos Valor Pago ou Lucro Estimado vazios");
                return;
            }

            double valorPago = Double.parseDouble(vp);
            double lucroEst = Double.parseDouble(le);

            // ==========================================
            // 🔥 VERIFICA SE VALOR PAGO É ZERO
            // ==========================================
            if (valorPago == 0) {
                System.out.println("⚠️ Valor Pago é zero, não é possível calcular percentual");
                campoPercentualLucro.setText("0");
                return;
            }

            // ==========================================
            // 🔥 CALCULA PERCENTUAL CORRETAMENTE
            // ==========================================
            double percentual = (lucroEst / valorPago) * 100;

            // ==========================================
            // 🔥 ARREDONDA PARA INTEIRO
            // ==========================================
            int percentualInt = (int) Math.round(percentual);

            System.out.println("📊 Percentual de lucro:");
            System.out.println("   Lucro: R$ " + lucroEst);
            System.out.println("   Valor Pago: R$ " + valorPago);
            System.out.println("   Percentual: " + percentualInt + "%");

            campoPercentualLucro.setText(Integer.toString(percentualInt));

        } catch (NumberFormatException ex) {
            System.err.println("❌ Erro ao calcular percentual: " + ex.getMessage());
            campoPercentualLucro.setText("0");
        }
    }
    
    public void gerarQRCodeItemEstoque(){
         // 1. Captura com segurança o que está digitado nos inputs
        String codigo = campoCodigoItem.getText().trim();
        String preco = campoPrecoSugerido.getText().trim();

        if (codigo.isEmpty() || preco.isEmpty()) {
            MensagemSistema.mostrarAvisoDark(this, "Insira o CÓDIGO DO ITEM e o PREÇO antes de gerar o QR Code!");
            return;
        }
        // Como deve ficar agora: (Troca a vírgula do preço por hífen ou mantém para separar no split)
        String precoLimpo = preco.replace(".", ","); // Padroniza com vírgula para a tela
        String nomeDoArquivoPng = "etiqueta_" + codigo + "_" + precoLimpo;

        try {
            // 3. Alimenta as variáveis na classe DAO e executa a chamada estática segura
            codigoDoItem = codigo;
            precoDoItem = preco;

            // Chama o gerador passando a sua variável global 'pasta_loja' como destino dinâmico!
            BufferedImage qrGerado = util.GeradorQRCode.criarQR(precoLimpo, pasta_loja, nomeDoArquivoPng);

            if (qrGerado != null) {
                // 4. Redimensiona de forma suave para caber perfeitamente no seu quadrado de visualização "FOTO DO ITEM"
                java.awt.Image imgRedimensionada = qrGerado.getScaledInstance(labelImagemItem.getWidth(), labelImagemItem.getHeight(), java.awt.Image.SCALE_SMOOTH);
                labelImagemItem.setIcon(new ImageIcon(imgRedimensionada));

                MensagemSistema.mostrarAvisoDark(this, "QR Code gerado e salvo com sucesso em: " + nomeDoArquivoPng + ".png");
            }

        } catch (IOException ex) {
            System.err.println("Erro crítico na gravação da etiqueta: " + ex.getMessage());
            MensagemSistema.mostrarAvisoDark(this, "Erro de I/O ao salvar a imagem: " + ex.getMessage());
        }
    }
    public void salvaImagemBanco(){
        try {
            String caminho = pasta_loja;
            System.out.println(caminho);
            File outputFile = new File(caminho+campoCodigoItem.getText()+".png");
            ImageIO.write(imagem, "png", outputFile);
            MensagemSistema.mostrarAvisoDark(this, "Imagem enviada com sucesso!");
            System.out.println("Imagem do item enviada com sucesso!");
        } catch (HeadlessException | IOException ex) {
            ex.getMessage();
            System.err.println("Erro ao salvar imagem!");
        }
    }
    
    public void geraQRCode() throws IOException{       
        codigoDoItem = campoCodigoItem.getText();
        precoDoItem = campoPrecoSugerido.getText();
        if(!(codigoDoItem == null || codigoDoItem.isEmpty() && precoDoItem == null || precoDoItem.isEmpty())){
            GeradorQRCode gqrc = new GeradorQRCode();
            
            MensagemSistema.mostrarAvisoDark(this, "QR CODE GERADO COM SUCESSO!");
            System.out.println("QR Code gerado com sucesso!");
        }else{
            MensagemSistema.mostrarAvisoDark(this, "Verifique se o item tem código e está com preço!");
            System.err.println("QR Code nao foi gerado!");
        }
    }

    public void salvaImagemItem() throws MalformedURLException, IOException{
        BufferedImage image = null;
        URL url = new URL(pasta_loja+campoDescricaoItem.getText()+".png");
        image = ImageIO.read(url);
    }
    
    public ImageIcon selecionaImagemItem(){
        System.out.println(campoCodigoItem.getText());
        ImageIcon icon = new ImageIcon(new ImageIcon(pasta_loja+campoCodigoItem.getText()+".png").getImage().getScaledInstance(170, 250, Image.SCALE_DEFAULT));
        labelImagemItem.setIcon(icon);
        return icon;       
    }
    
    public void salvaRegistroItem(String tipo, String codigo, String nome, String data, String descricao, String codPeca, String marca, String tamanho, String valorPago, String precoSug, String lucroEst, String percLucro, String obs, String status){       
        String resposta = null;
        String registroItem = resposta;
        try{
            if(rButtonTipoDesapego.isSelected()){
                tipo = rButtonTipoDesapego.getText();
            }
            if(rButtonTipoOutlet.isSelected()){
                tipo = rButtonTipoDesapego.getText();
            }
            if(rButtonTipoConsignado.isSelected()){
                tipo = rButtonTipoDesapego.getText();
            }
            codigo = campoCodigoFornecedor.getText();
            nome = campoNomeFornecedor.getText();
            data = campoDataCadastroItem.getText();
            descricao = campoDescricaoItem.getText();
            codPeca = campoMarca.getText();
            marca = campoTamanho.getText();
            tamanho = campoCodigoItem.getText();
            valorPago = campoValorPago.getText();
            precoSug = campoPrecoSugerido.getText();
            lucroEst = campoLucroEstimado.getText();
            percLucro = campoPercentualLucro.getText();
            obs = campoObservacoes.getText();          
        }catch(Exception ex){
            System.err.println("Erro: "+ex.getMessage());
        }
   }
    
    public String tipoFornecedor(){
        String tipo = null;       
        try{
            if(rButtonTipoDesapego.isSelected()){
                tipo = rButtonTipoDesapego.getText();
            }
            if(rButtonTipoOutlet.isSelected()){
                tipo = rButtonTipoOutlet.getText();
            }
            if(rButtonTipoConsignado.isSelected()){
                tipo = rButtonTipoConsignado.getText();
            }
        }catch(Exception ex){
            System.err.println("Não selecionou tipo: "+ex.getMessage());
        }
        return tipo;
    }
    
    public void cleanFields(){  
        System.out.println("Iniciando limpeza dos campos da tela estoque...");       
        bg.add(rButtonTipoDesapego);
        bg.add(rButtonTipoOutlet);
        bg.add(rButtonTipoConsignado);
        if(rButtonTipoDesapego.isSelected() || rButtonTipoOutlet.isSelected() || rButtonTipoConsignado.isSelected()){
            bg.clearSelection();
        }
        campoCodigoFornecedor.setText("");
        campoNomeFornecedor.setText("");
        campoDataCadastroItem.setText("");
        campoDescricaoItem.setText("");
        campoLotePecas.setText("");
        campoMarca.setText("");
        campoTamanho.setText("");
        campoCodigoItem.setText("");
        campoValorPago.setText("");
        campoPrecoSugerido.setText("");
        campoLucroEstimado.setText("");
        campoPercentualLucro.setText("");
        campoObservacoes.setText("");
        labelImagemItem.setIcon(null);
        campoDataVenda.setText(""); 
        System.out.println("Registro limpo com sucesso!");
        System.out.println("---------------------------");
    }
    
    public  void salvarItemNaBase(){
        System.out.println("Iniciando registro do item na base...");
        try {
            pdao.saveItem(p);
            pdao.saveItemCloud(p);
//            geraQRCode();
            gerarQRCodeItemEstoque();
            MensagemSistema.mostrarAvisoDark(this, "ITEM SALVO NA BASE!");
        } catch (SQLException ex) {
            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro ocorrido: "+ex.getMessage());
            System.out.println("----------------------------------");
        } catch (Exception ex) {
            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro ocorrido: "+ex.getMessage());
            System.err.println("----------------------------------");
        }     
    }
    
    private void buscarItemNaBase() {
        System.out.println("Iniciando busca do item na base...");
        System.out.println("----------------------------------");
        try {
            pdao.selectItemCloud(s, p);
            codePeca = p.getCodpeca();
            if ("".equals(codePeca) || codePeca == null) {
               System.out.println(">>>>>>>>>ITEM NÃO ENCONTRADO NA BASE!<<<<<<<<<<<");
            }           
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro ocorrido: "+ex.getMessage());
            System.err.println("----------------------------------");
        }
    }
    
    private void apagarItemNaBase() {
        System.out.println("Iniciando exclusão do item na base...");
        System.out.println("-------------------------------------");
        try {
            pdao.deleteItem(p);
            pdao.deleteItemCloud(p);
            System.out.println("Tipo Item: "+p.getTipoitem());
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro ocorrido: "+ex.getMessage());
            System.err.println("----------------------------------");
        }
    }
    
    private void calcularLucroAutomatico() {
        try {
            // 1. Pega o texto e remove espaços em branco nas pontas
            String textoPreco = campoPrecoSugerido.getText().trim();
            String textoCusto = campoValorPago.getText().trim();

            if (textoPreco.isEmpty() || textoCusto.isEmpty()) {
                campoLucroEstimado.setText("0,00");
                campoPercentualLucro.setText("0%");
                return;
            }

            // 2. 🚀 O SEGREDO: Remove pontos de milhar e converte a vírgula decimal em ponto
            // Se o usuário digitar "1.250,50" -> vira "1250,50" -> vira "1250.50"
            // Se digitar apenas "50,00" -> vira "50.00"
            textoPreco = textoPreco.replace(".", "").replace(",", ".");
            textoCusto = textoCusto.replace(".", "").replace(",", ".");

            // 3. Remove qualquer outro caractere parasita que sobrou (ex: símbolos de moeda)
            textoPreco = textoPreco.replaceAll("[^\\d.]", "");
            textoCusto = textoCusto.replaceAll("[^\\d.]", "");

            // Se após a limpeza o campo ficou vazio, define como zero para não estourar erro
            double precoSugerido = textoPreco.isEmpty() ? 0.0 : Double.parseDouble(textoPreco);
            double valorPago = textoCusto.isEmpty() ? 0.0 : Double.parseDouble(textoCusto);

            // 4. Cálculos matemáticos
            double lucroEstimado = precoSugerido - valorPago;
            double margemLucroPercentual = 0.0;

            if (valorPago > 0) {
                margemLucroPercentual = (lucroEstimado / valorPago) * 100;
            } else if (precoSugerido > 0) {
                margemLucroPercentual = 100.0;
            }

            // 5. Formatação de saída no padrão brasileiro (pt-BR) sem gambiarras de .replace
            java.util.Locale ptBR = new java.util.Locale("pt", "BR");
            campoLucroEstimado.setText(String.format(ptBR, "%.2f", lucroEstimado));
            campoPercentualLucro.setText(String.format(ptBR, "%.1f", margemLucroPercentual) + "%");

        } catch (Exception ex) {
            // Captura qualquer erro genérico e impede o travamento da tela/sistema
            System.err.println("Erro ao calcular lucro: " + ex.getMessage());
            campoLucroEstimado.setText("0,00");
            campoPercentualLucro.setText("0%");
        }
    }
    
    private void atualizarItemNaBase() {
        System.out.println("Iniciando atualização do item na base...");
        try {
            pdao.updateItem(p);
            pdao.updateItemCloud(p);
            System.out.println("Dados atualizados com sucesso!");
            System.out.println("------------------------------");
            MensagemSistema.mostrarAvisoDark(this, "Dados atualizados com sucesso!");
        } catch (HeadlessException | ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro ocorrido: "+ex.getMessage());
            System.err.println("----------------------------------");
        }
    }
    
//    private void atualizarSiteBrecho() throws InterruptedException {
//        System.out.println("Iniciando atualizacao do Site....");
//        try {
//            pdao.gerarSiteEstoque();
//            System.out.println("Finalizando atualizacao do Site....");
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
//            System.err.println("Erro: "+ex);
//        } catch (SQLException ex) {
//            Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
//            System.err.println("Erro: "+ex);
//        }
//    }

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
                new TelaEstoque().setVisible(true);
                
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonBarcode;
    private javax.swing.JButton buttonEditar;
    private javax.swing.JButton buttonExcluir;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton buttonLimpar;
    private javax.swing.JButton buttonMenu;
    private javax.swing.JButton buttonPesquisar;
    private javax.swing.JButton buttonPesquisarFornecedor;
    private javax.swing.JButton buttonSalvar;
    private javax.swing.JButton buttonSelecionaImagem;
    private javax.swing.JButton buttonTirarFotos;
    private javax.swing.JTextField campoCodigoFornecedor;
    private javax.swing.JTextField campoCodigoItem;
    private javax.swing.JFormattedTextField campoDataCadastroItem;
    private javax.swing.JTextField campoDataVenda;
    private javax.swing.JTextField campoDescricaoItem;
    private javax.swing.JTextField campoLotePecas;
    private javax.swing.JTextField campoLucroEstimado;
    private javax.swing.JTextField campoMarca;
    private javax.swing.JTextField campoNomeFornecedor;
    private javax.swing.JTextPane campoObservacoes;
    private javax.swing.JTextField campoPercentualLucro;
    private javax.swing.JTextField campoPrecoSugerido;
    private javax.swing.JTextField campoTamanho;
    private javax.swing.JTextField campoValorPago;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JFrame jFrame2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel labelImagemItem;
    private javax.swing.JRadioButton rButtonTipoConsignado;
    private javax.swing.JRadioButton rButtonTipoDesapego;
    private javax.swing.JRadioButton rButtonTipoOutlet;
    // End of variables declaration//GEN-END:variables


}
