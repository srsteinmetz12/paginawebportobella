package views;

import dao.ClienteDAO;
import dao.ProdutoDAO;
import dao.TrocasDAO;
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
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import models.Cliente;
import models.Produto;
import models.Trocas;
import util.MensagemSistema;

public class TelaTrocas extends javax.swing.JFrame {

    int idTrocas;
    private String dataTroca;
    TrocasDAO tdao = new TrocasDAO();
    Trocas t = new Trocas();
    ClienteDAO cdao = new ClienteDAO();
    Cliente c = new Cliente();
    ProdutoDAO pdao = new ProdutoDAO();
    Produto p = new Produto();
    private JPopupMenu popupSugestoes;
    
    private JList<String> listaSugestoes;
    private final DefaultListModel<String> listModel;
    private List<String> listaCacheClientes = new ArrayList<>();
    public static  String nomeCliente;
    public static String codigoPeca;
    private Timer buscaTimer;
    private final boolean itemSelecionado = false;
    private Timer timerBusca;
    int anoAtual = java.time.Year.now().getValue();
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TelaTrocas() {
        this.setUndecorated(true);
        initComponents();
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
                // --- 1. DEFINIÇÃO DA PALETA LUXO/MODA PREMIUM (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // Separadores e Bordas

        // --- 2. 🔥 FORÇAR A PINTURA DO PAINEL DE FUNDO (PRESERVA O SEU ALINHAMENTO) ---
        jPanel1.setBackground(grafiteProfundo);
        jPanel1.setOpaque(true);
        this.getContentPane().setBackground(grafiteProfundo);

        // --- 3. ⚖️ HIERARQUIA DE FONTES OPERACIONAIS EM BRANCO PURO ---
        // Título Indicador de Tela Máximo (TROCAS - Topo Esquerdo) [links: 10]
        jLabel1.setText("TROCAS");
        jLabel1.setForeground(brancoPuro);
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));

        // Nomes Comuns de Campos (Rótulos em Negrito Suave) [links: 10]
        javax.swing.JLabel[] labelsCamposTroca = {
            jLabel2, jLabel3, jLabel4, jLabel5, labelCreditoCliente, jLabel6, jLabel7
        };
        for (javax.swing.JLabel lbl : labelsCamposTroca) {
            lbl.setForeground(brancoPuro);
            lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        }

        // --- 4. CAMPOS DE ENTRADA DARK (JTextFields e JTextArea) ---
        javax.swing.JTextField[] todosCamposTexto = {
            campoIdTroca, campoNomeCliente, campoDataTroca, 
            campoPecaTroca, campoValorPecaTroca, campoCreditoCliente
        };
        for (javax.swing.JTextField txt : todosCamposTexto) {
            txt.setBackground(grafiteClaro);
            txt.setForeground(brancoPuro);
            txt.setCaretColor(brancoPuro);
            // 🔥 Substitui o BevelBorder nativo do Windows por uma linha fina e elegante do FlatLaf [links: 10]
            txt.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
            txt.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        }
        
        // Área de Texto de Observações Dark Unificada [links: 10]
        campoTextoObs.setBackground(grafiteClaro);
        campoTextoObs.setForeground(brancoPuro);
        campoTextoObs.setCaretColor(brancoPuro);
        campoTextoObs.setBorder(null); // Limpa as bordas internas redundantes
        campoTextoObs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        
        // Remove a caixa branca fantasma do painel de rolagem que envolve a área de texto [links: 10]
        jScrollPane2.setBackground(grafiteClaro);
        jScrollPane2.getViewport().setBackground(grafiteClaro); // Engole o fundo branco [links: 10]
        jScrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));

        // --- 5. ESTILIZAÇÃO DOS SEPARADORES DE LINHA (JSeparator) ---
        javax.swing.JSeparator[] todosSeparadores = {
            jSeparator1, jSeparator2, jSeparator3, jSeparator4
        };
        for (javax.swing.JSeparator sep : todosSeparadores) {
            sep.setForeground(cinzaLinhas);
            sep.setBackground(cinzaLinhas);
        }

        // --- 6. VETOR INTEGRADO DE BOTÕES: DESIGN PLANO SEM BORDAS (FLAT STYLE) ---
        javax.swing.JButton[] todosBotoesAcao = {
            buttonMenu, buttonLimpar, buttonSalvar, buttonPesquisar, buttonAtualizar
        };

        for (javax.swing.JButton btn : todosBotoesAcao) {
            btn.setBackground(grafiteClaro);
            btn.setForeground(brancoPuro);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false); // 🔥 Destrói os contornos tridimensionais chanfrados antigos! [links: 10]
            btn.putClientProperty("JButton.buttonType", "square"); // Força o FlatLaf Plano [links: 10]
        }
                // 🔥 TOM DE BRONZE ACOBREADO: Destaque sofisticado e visível para o botão de voltar
        java.awt.Color bronzeAcobreado = new java.awt.Color(140, 120, 83); // #8C7853
        java.awt.Color textoBrancoPuro  = new java.awt.Color(255, 255, 255); // #FFFFFF
        
        // Ajuste o nome da variável para o botão MENU da tela atual (ex: buttonMenu, buttonMenuRetire)
        buttonMenu.setBackground(bronzeAcobreado);
        buttonMenu.setForeground(textoBrancoPuro); // Letras brancas dão leitura perfeita no bronze
        buttonMenu.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        buttonMenu.setFocusPainted(false);
        buttonMenu.setBorderPainted(false);
        buttonMenu.putClientProperty("JButton.buttonType", "square");

        // --- CONFIGURAÇÃO DA BARRA DE TÍTULO PREMIUM NA TELA DE TROCAS ---
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
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Gerenciamento de Trocas e Créditos");
        lblClienteBarra.setForeground(brancoPuroBarra);
        lblClienteBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        barraTituloPremium.add(lblClienteBarra, java.awt.BorderLayout.WEST);
        
        // 3. Lado Direito: Bloco Agrupador (Assinatura + Botão X)
        javax.swing.JPanel painelDireitoBarra = new javax.swing.JPanel();
        painelDireitoBarra.setBackground(grafiteClaroBarra);
        painelDireitoBarra.setOpaque(true);
        painelDireitoBarra.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 0));
        
        // Texto de Desenvolvimento de Grife [1]
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
        
        // 4. Posiciona e estica a barra no topo exato da tela de trocas
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

        // 6. Inserção Inteligente no topo do painel principal (Tratamento para jPanel1 da sua classe) [1]
        try {
            jPanel1.add(barraTituloPremium);
            jPanel1.revalidate();
            jPanel1.repaint();
        } catch(Exception e) {
            this.getContentPane().add(barraTituloPremium);
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
        }       
        this.setLocationRelativeTo(null);
        this.setLocationRelativeTo(null);
        this.setTitle(util.ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI LTDA");
        
        configurarCampoCliente();
        configurarFoco();
        configurarCampo();
        configurarBuscaAutomatica();    
        listModel = new DefaultListModel<>();
        listaSugestoes = new JList<>(listModel);
        listaSugestoes.setBackground(new Color(255, 255, 255));
        listaSugestoes.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        listaSugestoes.setFixedCellHeight(25);      
        listaSugestoes.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));        
        listaSugestoes.setSelectionBackground(new Color(0, 120, 215));
        listaSugestoes.setSelectionForeground(Color.WHITE);              
        popupSugestoes = new JPopupMenu();
        popupSugestoes.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(listaSugestoes);       
        // CORREÇÃO VISUAL DE TAMANHO: Garante largura mínima caso o componente inicie zerado
        int larguraFocal = campoNomeCliente.getWidth() > 0 ? campoNomeCliente.getWidth() : 280;
        scrollPane.setPreferredSize(new Dimension(larguraFocal, 150));
        popupSugestoes.add(scrollPane);        
        // PERFORMANCE: O Timer impede o travamento do teclado da loja
        timerBusca = new Timer(400, (ActionEvent e) -> {
            try {
                buscarSugestaoNome();
            } catch (ClassNotFoundException | SQLException ex) {
                System.err.println("Erro no ciclo de performance: " + ex);
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
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        campoNomeCliente = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        campoDataTroca = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        campoPecaTroca = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        campoValorPecaTroca = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        campoCreditoCliente = new javax.swing.JTextField();
        labelCreditoCliente = new javax.swing.JLabel();
        buttonMenu = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        buttonLimpar = new javax.swing.JButton();
        buttonSalvar = new javax.swing.JButton();
        buttonPesquisar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        campoTextoObs = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        buttonAtualizar = new javax.swing.JButton();
        campoIdTroca = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 36)); // NOI18N
        jLabel1.setText("TROCAS");

        campoNomeCliente.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoNomeCliente.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoNomeCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoNomeClienteMouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel2.setText("CLIENTE");

        campoDataTroca.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoDataTroca.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoDataTroca.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoDataTroca.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoDataTrocaMouseClicked(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel3.setText("DATA");

        campoPecaTroca.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoPecaTroca.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoPecaTroca.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoPecaTroca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoPecaTrocaActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel4.setText("PEÇA");

        campoValorPecaTroca.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoValorPecaTroca.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoValorPecaTroca.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel5.setText("VALOR");

        campoCreditoCliente.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCreditoCliente.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCreditoCliente.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        labelCreditoCliente.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        labelCreditoCliente.setText("CRÉDITO");

        buttonMenu.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenu.setText("MENU");
        buttonMenu.setPreferredSize(new java.awt.Dimension(105, 31));
        buttonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuActionPerformed(evt);
            }
        });

        buttonLimpar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonLimpar.setText("LIMPAR");
        buttonLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLimparActionPerformed(evt);
            }
        });

        buttonSalvar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonSalvar.setText("SALVAR");
        buttonSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSalvarActionPerformed(evt);
            }
        });

        buttonPesquisar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonPesquisar.setText("PESQUISAR");
        buttonPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPesquisarActionPerformed(evt);
            }
        });

        campoTextoObs.setColumns(20);
        campoTextoObs.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoTextoObs.setRows(5);
        campoTextoObs.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane2.setViewportView(campoTextoObs);

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel6.setText("OBSERVAÇÕES");

        buttonAtualizar.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonAtualizar.setText("ATUALIZAR");
        buttonAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAtualizarActionPerformed(evt);
            }
        });

        campoIdTroca.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoIdTroca.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoIdTroca.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoIdTroca.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoIdTrocaMouseClicked(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel7.setText("ID TROCAS");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(campoIdTroca, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(25, 25, 25)
                                        .addComponent(jLabel7)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(97, 97, 97)
                                        .addComponent(jLabel2))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(7, 7, 7)
                                        .addComponent(campoNomeCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel4)
                                        .addGap(43, 43, 43))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(campoDataTroca, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoPecaTroca, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(33, 33, 33)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(40, 40, 40)
                                        .addComponent(labelCreditoCliente))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(5, 5, 5)
                                        .addComponent(campoValorPecaTroca, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoCreditoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jScrollPane2)
                            .addComponent(jSeparator3)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(67, 67, 67))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonAtualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(71, 71, 71))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(labelCreditoCliente)
                    .addComponent(jLabel7))
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(campoDataTroca, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoPecaTroca, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoValorPecaTroca, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoNomeCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoCreditoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoIdTroca, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(73, 73, 73)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonAtualizar)
                    .addComponent(buttonLimpar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonPesquisar)
                    .addComponent(buttonSalvar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuActionPerformed
        new TelaMenu().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonMenuActionPerformed

    private void buttonLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimparActionPerformed
       limparCampos();
    }//GEN-LAST:event_buttonLimparActionPerformed

    private void campoDataTrocaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoDataTrocaMouseClicked
        LocalDateTime date = LocalDateTime.now();
        String data = date.format(DateTimeFormatter.ISO_DATE);
        String dia = data.substring(8,10);
        String mes = data.substring(5,7);
        String ano = data.substring(0,4);
        campoDataTroca.setText(""+dia+"/"+mes+"/"+ano+"");
    }//GEN-LAST:event_campoDataTrocaMouseClicked

    private void buttonSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSalvarActionPerformed
        if (isCampoObrigatorioVazio()) {
            MensagemSistema.mostrarAvisoDark(this, "Preencha todos os campos obrigatórios!");
            return;
        }
        try {
            System.out.println("Iniciando persistência e processamento da troca...");
            System.out.println("------------------------------------------------");

            // 2. ALIMENTAÇÃO DO MODELO DE TROCAS (Tipagem Segura)
            t.setId(Integer.valueOf(campoIdTroca.getText().trim()));
            t.setNomeCliente(campoNomeCliente.getText().trim());
            t.setPecaTroca(campoPecaTroca.getText().trim());

            // Trata a conversão de ponto/vírgula decimal para evitar NumberFormatException
            t.setPecaValor(Double.valueOf(campoValorPecaTroca.getText().trim().replace(",", ".")));
            t.setCreditoCliente(Double.valueOf(campoCreditoCliente.getText().trim().replace(",", ".")));

            // Processa e valida a string de data no formato nacional
            if (!converterEConfigurarDataTroca()) {
                return; // Aborta a gravação se a data estiver incorreta
            }

            // Configuração de campos opcionais e estado padrão do modelo
            t.setObs(!campoTextoObs.getText().trim().isEmpty() ? campoTextoObs.getText().trim() : "Sem observações.");
            t.setStatus("ATIVO");

            // 3. PERSISTÊNCIA NA BASE DE DADOS
            salvarCreditoCliente(); 

            // Executa a devolução da peça diretamente na Cloud enviando a String do código por parâmetro
            p.setCodpeca(t.getPecaTroca());
            try {
                tdao.atualizaItemStatusDisponivelCloud(p);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TelaTrocas.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                System.out.println("Disparando Engine de Impressão de Cupom do Brechó...");
                util.GeradorComprovanteTrocas gerador = new util.GeradorComprovanteTrocas();
                gerador.emitirCupomPDF(t); // Envia o objeto 't' carregado para o método gerar o cupom
            } catch (Exception pdfEx) {
                System.err.println("Falha ao abrir visualizador de cupom: " + pdfEx.getMessage());
            }

            MensagemSistema.mostrarAvisoDark(this, "Troca registrada com sucesso!\nCrédito gerado e peça devolvida ao estoque.");
            limparCampos();

        } catch (NumberFormatException ex) {
            System.err.println("Erro de formatação numérica no formulário de trocas: " + ex.getMessage());
            MensagemSistema.mostrarAvisoDark(this, "Verifique se os campos de VALOR possuem apenas números!");
            System.err.println("Erro crítico: " + ex);
        } catch (HeadlessException | SQLException ex) {
            System.err.println("Erro crítico na infraestrutura durante a gravação da troca: " + ex);
            MensagemSistema.mostrarAvisoDark(this, "Falha operacional ao conectar com o banco Cloud.");
            System.err.println("Erro crítico: " + ex);
        }
    }//GEN-LAST:event_buttonSalvarActionPerformed
    
    private boolean isCampoObrigatorioVazio() {
        return campoIdTroca.getText().trim().isEmpty() 
            || campoNomeCliente.getText().trim().isEmpty() 
            || campoDataTroca.getText().trim().isEmpty() 
            || campoPecaTroca.getText().trim().isEmpty() 
            || campoValorPecaTroca.getText().trim().isEmpty() 
            || labelCreditoCliente.getText().trim().isEmpty();
    }
    
    private boolean converterEConfigurarDataTroca() {
    SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
    formatador.setLenient(false); // Impede que o Java aceite datas impossíveis como 31/02
    
    try {
        t.setDataTroca(formatador.parse(campoDataTroca.getText().trim()));
        dataTroca = campoDataTroca.getText().trim();
        System.out.println("Data de troca validada: " + dataTroca);
        return true;
    } catch (ParseException ex) {
        System.err.println("Erro: Data informada fora do padrão cronológico esperado.");
        MensagemSistema.mostrarAvisoDark(this, "Formato de data inválido! Use o padrão DD/MM/AAAA.");
        System.err.println("Erro crítico: " + ex);
        campoDataTroca.requestFocus();
        return false;
    }
}
    private void campoIdTrocaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoIdTrocaMouseClicked
        try {
            trocaId();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaTrocas.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
    }//GEN-LAST:event_campoIdTrocaMouseClicked

    private void buttonAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAtualizarActionPerformed
        try {
            tdao.atualizaTabelaCreditoLojaCloud();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaTrocas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaTrocas.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Erro: "+ex);
        }
    }//GEN-LAST:event_buttonAtualizarActionPerformed

    private void campoNomeClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoNomeClienteMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_campoNomeClienteMouseClicked

    private void campoPecaTrocaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoPecaTrocaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoPecaTrocaActionPerformed

    private void buttonPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPesquisarActionPerformed
        try {
            // 🎨 Paleta de Cores Alinhada ao Padrão Dark Premium do Brechó
            java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);
            java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);
            java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);
            java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255);
            java.awt.Color cinzaLinhas     = new java.awt.Color(70, 70, 70);
            java.awt.Color pretoCabecalho  = new java.awt.Color(20, 20, 20);

            // 1. Cria a Janela de Diálogo Customizada Modal (Trava a tela de trás)
            final javax.swing.JDialog caixaDialogo = new javax.swing.JDialog(this, "Busca de Histórico", true);
            caixaDialogo.setUndecorated(true); // Remove a barra branca nativa do Windows 🛠️
            caixaDialogo.setSize(400, 150);
            caixaDialogo.setLocationRelativeTo(this); // Centraliza milimetricamente sobre a Tela de Trocas

            // Painel Principal com Borda Integrada
            javax.swing.JPanel painelPrincipal = new javax.swing.JPanel(null);
            painelPrincipal.setBackground(grafiteProfundo);
            painelPrincipal.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
            caixaDialogo.add(painelPrincipal);

            // Barra Superior do Diálogo com o Título
            javax.swing.JPanel barraTopo = new javax.swing.JPanel(new java.awt.BorderLayout());
            barraTopo.setBackground(pretoCabecalho);
            barraTopo.setBounds(0, 0, 400, 30);
            barraTopo.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

            javax.swing.JLabel lblTitulo = new javax.swing.JLabel("  BUSCA HISTÓRICO DE CRÉDITOS");
            lblTitulo.setForeground(douradoOuro);
            lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
            barraTopo.add(lblTitulo, java.awt.BorderLayout.WEST);
            painelPrincipal.add(barraTopo);

            // Label Instrutiva Interna
            javax.swing.JLabel lblInstrucao = new javax.swing.JLabel("Digite o nome completo do cliente:");
            lblInstrucao.setForeground(brancoPuro);
            lblInstrucao.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            lblInstrucao.setBounds(20, 45, 360, 20);
            painelPrincipal.add(lblInstrucao);

            // Campo de Entrada de Texto Dark Style
            final javax.swing.JTextField txtNomeBusca = new javax.swing.JTextField();
            txtNomeBusca.setBackground(grafiteClaro);
            txtNomeBusca.setForeground(brancoPuro);
            txtNomeBusca.setCaretColor(brancoPuro); // Cursor piscando em branco
            txtNomeBusca.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            txtNomeBusca.setBounds(20, 70, 360, 28);
            txtNomeBusca.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
            painelPrincipal.add(txtNomeBusca);

            // Botão OK de Confirmação (Dourado)
            javax.swing.JButton btnConfirmar = new javax.swing.JButton("PESQUISAR");
            btnConfirmar.setBackground(douradoOuro);
            btnConfirmar.setForeground(pretoCabecalho);
            btnConfirmar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
            btnConfirmar.setFocusPainted(false);
            btnConfirmar.setBorderPainted(false);
            btnConfirmar.setBounds(135, 110, 130, 26);
            btnConfirmar.putClientProperty("JButton.buttonType", "square");
            painelPrincipal.add(btnConfirmar);

            // Botão Cancelar (Discreto ao lado se necessário, ou fechamento automático)
            javax.swing.JButton btnSair = new javax.swing.JButton("X");
            btnSair.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            btnSair.setForeground(brancoPuro);
            btnSair.setBackground(pretoCabecalho);
            btnSair.setFocusPainted(false);
            btnSair.setBorderPainted(false);
            btnSair.setPreferredSize(new java.awt.Dimension(40, 30));
            btnSair.addActionListener(e -> caixaDialogo.dispose());
            barraTopo.add(btnSair, java.awt.BorderLayout.EAST);

            // Array final para resgatar a String digitada de dentro das Threads/Listeners
            final String[] nomeRetornado = {null};

            // Lógica de Execução da Busca ao confirmar
            java.awt.event.ActionListener acaoConfirmar = e -> {
                String texto = txtNomeBusca.getText().trim();
                if (!texto.isEmpty()) {
                    nomeRetornado[0] = texto;
                    caixaDialogo.dispose(); // Fecha o diálogo se estiver preenchido
                } else {
                    util.MensagemSistema.mostrarAvisoDark(caixaDialogo, "O nome não pode ficar vazio!");
                    txtNomeBusca.requestFocus();
                }
            };

            // Vincula a ação tanto ao clique do Botão quanto ao apertar ENTER no teclado 🚀
            btnConfirmar.addActionListener(acaoConfirmar);
            txtNomeBusca.addActionListener(acaoConfirmar);

            // Força o cursor a focar na caixa de texto assim que ela abrir
            java.awt.EventQueue.invokeLater(() -> txtNomeBusca.requestFocusInWindow());

            // Abre a caixa de diálogo de forma síncrona (A Thread para aqui até o operador dar OK ou fechar)
            caixaDialogo.setVisible(true);

            // 🚀 DISPARO DO MÓDULO CLOUD: Se o operador digitou um nome e confirmou
            if (nomeRetornado[0] != null) {
                System.out.println("Disparando histórico Dark Cloud para: " + nomeRetornado[0]);

                // Invoca o método corrigido da classe DAO passando o nome coletado
                tdao.buscaHistoricoClienteTrocasCloud(nomeRetornado[0]);
            }

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Erro na execução da caixa de pesquisa Dark: " + ex.getMessage());
        }
    }//GEN-LAST:event_buttonPesquisarActionPerformed

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaTrocas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaTrocas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaTrocas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaTrocas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaTrocas().setVisible(true);
            }
        });
    }
    
    public void atualizarItemDisponivelEstoque(){
        System.out.println("Iniciando devolução do item no estoque");
        codigoPeca = campoPecaTroca.getText();
        try {
            tdao.atualizaItemStatusDisponivelCloud(p);
            tdao.atualizaItemStatusDisponivel(p);
            System.out.println("Finalizando devolução do item no estoque");
            System.out.println("----------------------------------");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaTrocas.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
    }
    
    public void salvarCreditoCliente() throws SQLException{
        System.out.println("Iniciando registro de credito do clientes...");
        try {
            tdao.saveCreditoCloud(t);
            tdao.saveCredito(t);
            atualizarItemDisponivelEstoque();
            System.out.println("Finalizando registro de credito do clientes...");
            System.out.println("----------------------------------");
        } catch (Exception ex) {
            Logger.getLogger(TelaTrocas.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
    }
    
    public void carregarCacheBuscaClientes() {
        try {
            System.out.println("Iniciando busca por nomes na base de clientes...");
            System.out.println("----------------------------------");
            
            // CORREÇÃO 1: Usa a instância cdao para preencher o modelo do cliente c
            cdao.buscaNomeClienteCloud(c); 
            String nomesStr = c.getNomeCli(); // CORREÇÃO 2: Coleta a string correta do cliente preenchido
            
            if (nomesStr != null && !nomesStr.isEmpty()) {
                listaCacheClientes = Arrays.asList(nomesStr.split(";"));
                System.out.println("Lista Carregada da Base: " + listaCacheClientes.size() + " registros.");
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
        listModel.clear();
        
        if (nomeCliente.length() < 2) {
            if (popupSugestoes != null) {
                popupSugestoes.setVisible(false);
            }
            return;
        }
        
        carregarCacheBuscaClientes();
        
        // FILTRAGEM DE ALTA PERFORMANCE EM MEMÓRIA (Evita travar o Swing)
        String termoUpper = nomeCliente.toUpperCase();
        for (String clienteItem : listaCacheClientes) {
            if (clienteItem.toUpperCase().contains(termoUpper)) {
                listModel.addElement(clienteItem.trim());
            }
        }
        
        // EXIBIÇÃO DO COMPONENTE POPUP
        if (listModel.getSize() > 0 && popupSugestoes != null) {
            if (!popupSugestoes.isVisible()) {
                SwingUtilities.invokeLater(() -> {
                    // Força a redensificação do tamanho antes de mostrar na tela
                    int larguraAtual = campoNomeCliente.getWidth() > 0 ? campoNomeCliente.getWidth() : 280;
                    popupSugestoes.getComponent(0).setPreferredSize(new Dimension(larguraAtual, 150));
                    popupSugestoes.pack();
                    
                    popupSugestoes.show(campoNomeCliente, 0, campoNomeCliente.getHeight());
                    campoNomeCliente.requestFocusInWindow();
                    System.out.println("Popup de sugestões expandido com sucesso.");
                });
            }
        } else {
            if (popupSugestoes != null) {
                popupSugestoes.setVisible(false);
            }
        }
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
                    }
                });
                buscaTimer.setRepeats(false);
                buscaTimer.start();
            }
        });
    }
    
    public void trocaId() throws ClassNotFoundException, SQLException{
        int x = 0;
        int trocasId;
        try {
            tdao.selectIdTrocasCloud(t);
            int codigoId = t.getId();
            if(codigoId != 0){
                x = codigoId;
                trocasId = (x+1);
                campoIdTroca.setText(Integer.toString(x+1));
                System.out.println(trocasId);
            }else{
                if(x == 0){                  
                   campoIdTroca.setText("1");
                }else{
                    System.out.println("ID: " + codigoId + " --> incorreto!");
                }
            }           
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Erro: "+ex);
        }            
    }
    
    public void limparCampos(){       
        campoIdTroca.setText("");
        campoNomeCliente.setText("");
        campoCreditoCliente.setText("");
        campoDataTroca.setText("");
        campoPecaTroca.setText("");
        campoValorPecaTroca.setText("");
        campoTextoObs.setText("");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAtualizar;
    private javax.swing.JButton buttonLimpar;
    private javax.swing.JButton buttonMenu;
    private javax.swing.JButton buttonPesquisar;
    private javax.swing.JButton buttonSalvar;
    private javax.swing.JTextField campoCreditoCliente;
    private javax.swing.JTextField campoDataTroca;
    private javax.swing.JTextField campoIdTroca;
    private javax.swing.JTextField campoNomeCliente;
    public javax.swing.JTextField campoPecaTroca;
    private javax.swing.JTextArea campoTextoObs;
    private javax.swing.JTextField campoValorPecaTroca;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel labelCreditoCliente;
    // End of variables declaration//GEN-END:variables

}
