package views;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.YES_OPTION;
import dao.FornecedorDAO;
import java.awt.HeadlessException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import models.Fornecedor;
import util.ConfigLoader;
import util.MensagemSistema;

public class TelaFornecedor extends javax.swing.JFrame {

    public static String s;
    public String tipo;
    FornecedorDAO fdao = new FornecedorDAO();       
    Fornecedor f = new Fornecedor();
    ButtonGroup bg = new ButtonGroup();
    public String precoMedio;
    public String lote;
    String tipo_fornecedor;
    int anoAtual = java.time.Year.now().getValue();
    String favicon = ConfigLoader.get("sistema.favicon");
    
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TelaFornecedor() {
        this.setUndecorated(true);
        initComponents();
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
                // --- 1. PALETA LUXO/MODA PREMIUM CONTÍNUA (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C

        // --- 2. 🔥 REVESTIMENTO DO PAINEL DE FUNDO (MANTÉM O ALINHAMENTO ORIGINAL) ---
        // Altere 'jPanel1' para o nome real do seu painel principal se necessário
        try {
            jPanel1.setBackground(grafiteProfundo);
            jPanel1.setOpaque(true);
        } catch(Exception e) {}
        this.getContentPane().setBackground(grafiteProfundo);

        // --- 3. ⚖️ HIERARQUIA DE FONTES OPERACIONAIS ---
        // Nível 1: Título Indicador da Tela (CADASTRO DE FORNECEDOR)
        jLabel1.setForeground(brancoPuro);
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));

        // Nível 2: Subtítulos de Seções (TIPO / DADOS DE CADASTRO)
        // Substitua pelos nomes das labels reais se houver divergência no NetBeans
        try {
            jLabel2.setForeground(brancoPuro); // TIPO
            jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
            
            jLabel3.setForeground(brancoPuro); // DADOS DE CADASTRO
            jLabel3.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        } catch(Exception e) {}

        // Nível 3: Rótulos Menores de Campos Comuns (NOME, CEP, ENDEREÇO, OBSERVAÇÕES...)
        // Junta todas as outras labels num vetor para pintar em branco de uma vez só
        javax.swing.JLabel[] labelsCamposFornecedor = {
            jLabel4, jLabel5, jLabel6, jLabel7, jLabel8, jLabel9, jLabel10, jLabel11, 
            jLabel12, jLabel13, jLabel14, jLabel15, jLabel16, jLabel17, jLabel18, labelUF
        };
        for (javax.swing.JLabel lbl : labelsCamposFornecedor) {
            try {
                lbl.setForeground(brancoPuro);
                lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
            } catch(Exception e) {}
        }

        // 🔥 Botões de Seleção Circular (RadioButtons) sem fundo fosco e em Dourado Ouro [links: 10]
        javax.swing.JRadioButton[] todosRadios = {
            rButtonDesapego, rButtonOutlet, rButtonCPF, rButtonCNPJ
        };
        for (javax.swing.JRadioButton rad : todosRadios) {
            try {
                rad.setForeground(douradoOuro);
                rad.setOpaque(false);
                rad.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
            } catch(Exception e) {}
        }

        // --- 4. CAMPOS DE ENTRADA DARK SLIM (JTextFields e JTextArea) ---
        javax.swing.JTextField[] todosCamposFornecedor = {
            campoCodigo, campoData, campoLote, campoPrecoMedio, campoNome, campoCEP, 
            campoCidade, campoUF, campoEndereco, campoNumero, campoComplemento, 
            campoBairro, campoEmail, campoTelefone, campoSiteRede, campoCpfCnpj
        };
        for (javax.swing.JTextField txt : todosCamposFornecedor) {
            try {
                txt.setBackground(grafiteClaro);
                txt.setForeground(brancoPuro);
                txt.setCaretColor(brancoPuro);
                txt.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); // Borda fina moderna
                txt.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            } catch(Exception e) {}
        }
        
        // Pinta a grande caixa branca das Observações para o modo Dark [links: 10]
        try {
            campoObservacao.setBackground(grafiteClaro);
            campoObservacao.setForeground(brancoPuro);
            campoObservacao.setCaretColor(brancoPuro);
            campoObservacao.setBorder(null);
            campoObservacao.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            
            // Remove a barra branca fantasma do painel de rolagem [links: 10]
            jScrollPane1.setBackground(grafiteClaro);
            jScrollPane1.getViewport().setBackground(grafiteClaro);
            jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
        } catch(Exception e) {}

        // --- 5. ESTILIZAÇÃO DOS SEPARADORES DE LINHA (JSeparator) ---
        javax.swing.JSeparator[] todosSeparadores = {
            jSeparator1, jSeparator2, jSeparator3
        };
        for (javax.swing.JSeparator sep : todosSeparadores) {
            try {
                sep.setForeground(cinzaLinhas);
                sep.setBackground(cinzaLinhas);
            } catch(Exception e) {}
        }

        // --- 6. VETOR DE BOTÕES: DESIGN PLANO EMBUTIDO (FLAT STYLE) ---
        javax.swing.JButton[] botoesBaseFornecedor = {
            buttonMenu, buttonPesquisar, buttonLimpar, buttonExcluir, buttonEditar, buttonSalvar
        };

        for (javax.swing.JButton btn : botoesBaseFornecedor) {
            try {
                btn.setBackground(grafiteClaro);
                btn.setForeground(brancoPuro);
                btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false); // 🔥 Arranca as molduras tridimensionais arredondadas do Windows! [links: 10]
                btn.putClientProperty("JButton.buttonType", "square"); // Força cantos retos modernos [links: 10]
            } catch(Exception e) {}
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
                // --- CONFIGURAÇÃO DA BARRA DE TÍTULO PREMIUM NA TELA DE FORNECECEDORES ---
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
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Cadastro de Fornecedores");
        lblClienteBarra.setForeground(brancoPuroBarra);
        lblClienteBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        // Painel Esquerdo: Logo + Título
        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        painelEsquerdo.setOpaque(false);
        barraTituloPremium.add(lblClienteBarra, java.awt.BorderLayout.WEST);
        javax.swing.JLabel lblLogo = new javax.swing.JLabel();
        try {
            java.net.URL urlLogo = getClass().getResource(favicon); 
            if (urlLogo != null) {
                javax.swing.ImageIcon iconeOriginal = new javax.swing.ImageIcon(urlLogo);
                java.awt.Image imgRedimensionada = iconeOriginal.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
                lblLogo.setIcon(new javax.swing.ImageIcon(imgRedimensionada));
            }
        } catch (Exception ex) {
            System.err.println("Não foi possível carregar o logo na barra: " + ex.getMessage());
        }
        painelEsquerdo.add(lblLogo);
        
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
        
        // 4. Posiciona e estica a barra no topo exato da tela de cadastro [links: 1, 3]
        barraTituloPremium.setBounds(0, 0, this.getWidth(), 30);

        // 5. Motor de Movimentação utilizando a referência de janelaAtual [links: 1, 3]
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

        // 6. Inserção Inteligente no topo do painel principal [links: 10]
        try {
            jPanel1.add(barraTituloPremium);
            jPanel1.revalidate();
            jPanel1.repaint();
        } catch(Exception e) {
            this.getContentPane().add(barraTituloPremium);
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
        }       
        this.setLocationRelativeTo(null); // Mantém o cadastro centralizado [links: 10]
//        this.setIconImage(new ImageIcon(getClass().getResource("/images/favicon.png")).getImage());
        this.setTitle(util.ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI LTDA");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rButtonDesapego = new javax.swing.JRadioButton();
        rButtonOutlet = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        campoNome = new javax.swing.JTextField();
        rButtonCPF = new javax.swing.JRadioButton();
        rButtonCNPJ = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        campoEndereco = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        campoNumero = new javax.swing.JTextField();
        campoComplemento = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        campoEmail = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        campoCodigo = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        buttonLimpar = new javax.swing.JButton();
        buttonEditar = new javax.swing.JButton();
        buttonExcluir = new javax.swing.JButton();
        campoCpfCnpj = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        campoSiteRede = new javax.swing.JTextField();
        buttonPesquisar = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        campoObservacao = new javax.swing.JTextArea();
        jLabel14 = new javax.swing.JLabel();
        campoCEP = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        campoBairro = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        campoCidade = new javax.swing.JTextField();
        buttonSalvar = new javax.swing.JButton();
        buttonMenu = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        campoData = new javax.swing.JFormattedTextField();
        campoTelefone = new javax.swing.JTextField();
        campoLote = new javax.swing.JTextField();
        campoPrecoMedio = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        campoUF = new javax.swing.JTextField();
        labelUF = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setPreferredSize(new java.awt.Dimension(1000, 700));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 30)); // NOI18N
        jLabel1.setText("CADASTRO DE FORNECEDOR");

        rButtonDesapego.setBackground(new java.awt.Color(204, 204, 255));
        rButtonDesapego.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        rButtonDesapego.setText("DESAPEGO");
        rButtonDesapego.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rButtonDesapego.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rButtonDesapegoActionPerformed(evt);
            }
        });

        rButtonOutlet.setBackground(new java.awt.Color(204, 204, 255));
        rButtonOutlet.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        rButtonOutlet.setText("OUTLET");
        rButtonOutlet.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rButtonOutlet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rButtonOutletActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel3.setText("NOME");

        campoNome.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoNome.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        rButtonCPF.setBackground(new java.awt.Color(204, 204, 255));
        rButtonCPF.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        rButtonCPF.setText("CPF");
        rButtonCPF.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        rButtonCPF.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rButtonCPF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rButtonCPFActionPerformed(evt);
            }
        });

        rButtonCNPJ.setBackground(new java.awt.Color(204, 204, 255));
        rButtonCNPJ.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        rButtonCNPJ.setText("CNPJ");
        rButtonCNPJ.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        rButtonCNPJ.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rButtonCNPJ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rButtonCNPJActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel2.setText("ENDEREÇO");

        campoEndereco.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoEndereco.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel4.setText("NÚMERO");

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel5.setText("COMPLEMENTO");

        campoNumero.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoNumero.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoNumero.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        campoComplemento.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoComplemento.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoComplemento.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel6.setText("EMAIL");

        campoEmail.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoEmail.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoEmailFocusLost(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel7.setText("TELEFONE");

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel8.setText(" CÓDIGO");

        campoCodigo.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCodigo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCodigo.setToolTipText("Max. 10 números");
        campoCodigo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoCodigo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoCodigoMouseClicked(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel9.setText("           DATA");

        buttonLimpar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonLimpar.setText("LIMPAR");
        buttonLimpar.setBorder(null);
        buttonLimpar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLimparActionPerformed(evt);
            }
        });

        buttonEditar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonEditar.setText("EDITAR");
        buttonEditar.setBorder(null);
        buttonEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditarActionPerformed(evt);
            }
        });

        buttonExcluir.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonExcluir.setText("EXCLUIR");
        buttonExcluir.setBorder(null);
        buttonExcluir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExcluirActionPerformed(evt);
            }
        });

        campoCpfCnpj.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCpfCnpj.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCpfCnpj.setToolTipText("Somente númenos");
        campoCpfCnpj.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoCpfCnpj.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoCpfCnpjFocusLost(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Times New Roman", 1, 13)); // NOI18N
        jLabel10.setText("SITE / REDE SOCIAL");

        campoSiteRede.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoSiteRede.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        buttonPesquisar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonPesquisar.setText("PESQUISAR");
        buttonPesquisar.setBorder(null);
        buttonPesquisar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPesquisarActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        jLabel11.setText("TIPO");

        jLabel12.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        jLabel12.setText("DADOS DE CADASTRO");

        jLabel13.setFont(new java.awt.Font("Times New Roman", 1, 13)); // NOI18N
        jLabel13.setText("OBSERVAÇÕES");

        campoObservacao.setColumns(20);
        campoObservacao.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoObservacao.setRows(5);
        campoObservacao.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane1.setViewportView(campoObservacao);

        jLabel14.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel14.setText("CEP");

        campoCEP.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCEP.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCEP.setToolTipText("Somente números");
        campoCEP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoCEP.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoCEPFocusLost(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel15.setText("BAIRRO");

        campoBairro.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoBairro.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel16.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel16.setText("CIDADE");

        campoCidade.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCidade.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        buttonSalvar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonSalvar.setText("SALVAR");
        buttonSalvar.setBorder(null);
        buttonSalvar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSalvarActionPerformed(evt);
            }
        });

        buttonMenu.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenu.setText("MENU");
        buttonMenu.setBorder(null);
        buttonMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuActionPerformed(evt);
            }
        });

        campoData.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoData.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter()));
        campoData.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoData.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoDataMouseClicked(evt);
            }
        });

        campoTelefone.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoTelefone.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoTelefone.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoTelefone.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                campoTelefoneFocusLost(evt);
            }
        });

        campoLote.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoLote.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoLote.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoLote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoLoteActionPerformed(evt);
            }
        });

        campoPrecoMedio.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoPrecoMedio.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoPrecoMedio.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoPrecoMedio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoPrecoMedioActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel17.setText("LOTE");

        jLabel18.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jLabel18.setText("PREÇO MÉDIO");

        campoUF.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        campoUF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoUF.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoUF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoUFActionPerformed(evt);
            }
        });

        labelUF.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        labelUF.setText("     UF");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(83, 83, 83)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 818, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 818, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 814, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 818, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(57, 57, 57)
                                    .addComponent(buttonPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(buttonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                    .addComponent(campoCodigo))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoData, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoLote, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(campoPrecoMedio, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(campoEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(rButtonCPF)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(4, 4, 4)
                                                .addComponent(jLabel4)
                                                .addGap(16, 16, 16))
                                            .addComponent(campoNumero, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(21, 21, 21)
                                                .addComponent(jLabel5))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(campoComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(campoBairro, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(25, 25, 25)
                                        .addComponent(rButtonCNPJ)
                                        .addGap(50, 50, 50)
                                        .addComponent(jLabel14)
                                        .addGap(76, 76, 76)
                                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(55, 55, 55)
                                        .addComponent(labelUF, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(campoNome, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoCpfCnpj, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoCEP, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoCidade, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoUF)))
                        .addGap(131, 131, 131))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(rButtonDesapego)
                        .addGap(27, 27, 27)
                        .addComponent(rButtonOutlet)
                        .addGap(293, 293, 293)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(262, 262, 262))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel1)
                            .addComponent(jLabel13)
                            .addComponent(jLabel12)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(344, 344, 344))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(campoEmail)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(campoTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addGap(46, 46, 46)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(148, 148, 148)
                                        .addComponent(jLabel15))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(62, 62, 62)
                                        .addComponent(jLabel10))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(campoSiteRede, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel18))
                                .addGap(4, 4, 4)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(campoPrecoMedio, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(campoData, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(campoCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(campoLote, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(rButtonOutlet)
                                    .addComponent(rButtonDesapego))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)))
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(rButtonCNPJ)
                    .addComponent(rButtonCPF)
                    .addComponent(jLabel14)
                    .addComponent(labelUF)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(campoUF, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(campoNome, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(campoCpfCnpj, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(campoCEP, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(campoCidade, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoBairro, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(campoSiteRede, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(campoTelefone, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(campoEmail, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonPesquisar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(68, 68, 68))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

////// Botao Menu //////
    private void buttonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuActionPerformed
        new TelaMenu().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonMenuActionPerformed
////// RB CNPJ //////
    private void rButtonCNPJActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rButtonCNPJActionPerformed
        if(rButtonCNPJ.isSelected()){
            System.out.println("Selecionou CNPJ");
        }
    }//GEN-LAST:event_rButtonCNPJActionPerformed
////// RB CPF //////
    private void rButtonCPFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rButtonCPFActionPerformed
        bg.add(rButtonCPF);
        bg.add(rButtonCNPJ);
        if(rButtonCPF.isSelected()){
            System.out.println("Selecionou CPF");
        }
    }//GEN-LAST:event_rButtonCPFActionPerformed
////// RB Outlet //////
    private void rButtonOutletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rButtonOutletActionPerformed
        if(rButtonOutlet.isSelected()){
            System.out.println("Selecionou Outlet");
        }
    }//GEN-LAST:event_rButtonOutletActionPerformed
////// RB Desapego //////
    private void rButtonDesapegoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rButtonDesapegoActionPerformed
        bg.add(rButtonDesapego);
        bg.add(rButtonOutlet);
        if(rButtonDesapego.isSelected()){
            System.out.println("Selecionou Desapego");
        }
    }//GEN-LAST:event_rButtonDesapegoActionPerformed
////// Botao Salvar //////
    private void buttonSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSalvarActionPerformed
        if(rButtonDesapego.isSelected() || rButtonOutlet.isSelected() || campoCodigo.getText().isEmpty() || campoData.getText().isEmpty() || campoNome.getText().isEmpty() || campoCpfCnpj.getText().isEmpty()){
            try{
                f.setTipoforn(rButtonDesapego.getText());
                if(rButtonDesapego.getText() == null){
                    f.setTipoforn(rButtonOutlet.getText());
                }
                f.setCodforn(campoCodigo.getText());
                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");      
                try {
                    f.setDataCadastramento(fmt.parse(campoData.getText()));
                } catch (ParseException ex) {
                    Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
                }
                f.setNomeforn(campoNome.getText());
                f.setCpfCnpj(campoCpfCnpj.getText());
                f.setCep(campoCEP.getText());
                f.setCidade(campoCidade.getText());
                f.setUF(campoUF.getText());
                f.setEndereco(campoEndereco.getText());
                f.setNumero(campoNumero.getText());
                f.setComplemento(campoComplemento.getText());
                f.setBairro(campoBairro.getText());
                f.setEmail(campoEmail.getText());
                f.setTelefone(campoTelefone.getText());
                f.setSiteRede(campoSiteRede.getText());
                f.setObservacao(campoObservacao.getText());
                try {
                    if((rButtonDesapego.getText().isEmpty()) || (rButtonOutlet.getText().isEmpty()) || (campoCodigo.getText().isEmpty()) || (campoData.getText().isEmpty()) || (campoNome.getText().isEmpty())){
                        MensagemSistema.mostrarAvisoDark(this, "Preencher Tipo, Código, Data e nome!");
                    }else{
                        salvarCadastroFornecedor();
                        MensagemSistema.mostrarAvisoDark(this, "Registro SALVO na base!");
                        System.out.println("Registro efetuado com sucesso!");
                        System.out.println("-----------------------------------");
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro: "+ex.getMessage());
                }
                limparCamposCadastroFornecedor();
            }catch(HeadlessException ex){
                System.out.println("Erro: "+ex.getMessage());
                MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex.getMessage());           
            }
        }else{
            MensagemSistema.mostrarAvisoDark(this, "Campos tipo Desapego/outlet, Código, Data, Nome e CPF/CNPJ devem ser preenchidos!");
            System.out.println("Não preencheu campos obrigatórios!");
            System.out.println("----------------------------------");        
        }
    }//GEN-LAST:event_buttonSalvarActionPerformed
////// Botao Limpar  ///////
    private void buttonLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimparActionPerformed
        limparCamposCadastroFornecedor();                         
        buttonPesquisar.setEnabled(true);
        buttonSalvar.setEnabled(true); 
    }//GEN-LAST:event_buttonLimparActionPerformed
////// Botao Pesquisar //////
    @SuppressWarnings("null")
    private void buttonPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPesquisarActionPerformed
        s = MensagemSistema.mostrarInputDark(this, "Entre com Código do fornecedor");             
        try{
            pesquisarCadastroFornecedor();
            System.out.println("Tipo fornecedor: "+f.getTipoforn());
            tipo_fornecedor = f.getTipoforn();
            if(tipo_fornecedor.equals("DESAPEGO")){
                rButtonDesapego.doClick();
            }else{
                rButtonOutlet.doClick();
            }
            campoCodigo.setText(f.getCodforn());
            campoData.setText(f.getDataCadastramento().toString().replaceAll("-", "/"));
            String data = campoData.getText();
            System.out.println(data);
            String dia = data.substring(8, 10);
            System.out.println(dia);
            String mes = data.substring(5, 7);
            System.out.println(mes);
            String ano = data.substring(0, 4);
            System.out.println(ano);
            campoData.setText(""+dia+"/"+mes+"/"+ano+"");
            System.out.println(campoData.getText());
            campoLote.setText(f.getUltimoLote());
            campoPrecoMedio.setText(String.valueOf(f.getPrecoMedio()));
            campoNome.setText(f.getNomeforn());
            campoCpfCnpj.setText(f.getCpfCnpj());
            campoCEP.setText(f.getCep());
            campoCidade.setText(f.getCidade());
            campoEndereco.setText(f.getEndereco());
            campoNumero.setText(f.getNumero());
            campoComplemento.setText(f.getComplemento());
            campoBairro.setText(f.getBairro());
            campoEmail.setText(f.getEmail());
            campoTelefone.setText(f.getTelefone());
            campoSiteRede.setText(f.getSiteRede());
            campoObservacao.setText(f.getObservacao());       
        } catch(IOException ex){
            System.out.println("Erro: "+ ex.getMessage());
            System.out.println("----------------------");
        }       
        buttonPesquisar.setEnabled(false);
        buttonSalvar.setEnabled(false);
    }//GEN-LAST:event_buttonPesquisarActionPerformed
////// Botao Editar //////
    private void buttonEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditarActionPerformed
        boolean resposta = MensagemSistema.mostrarDecisaoDark(this, "Deseja alterar esse registro?");
        if(resposta == true){
            try{
                f.setTipoforn(rButtonDesapego.getText());
                if(rButtonDesapego.getText() == null){
                    f.setTipoforn(rButtonOutlet.getText());
                }
                f.setCodforn(campoCodigo.getText());
                System.out.println(campoCodigo.getText());
                f.setUltimoLote(lote);
                f.setPrecoMedio(Double.valueOf(campoPrecoMedio.getText()));
                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");      
                try {
                    f.setDataCadastramento(fmt.parse(campoData.getText()));
                    System.out.println(campoData.getText());
                    System.out.println("----------------------");

                } catch (ParseException ex) {
                    Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro na data: "+ex.getMessage());
                    System.out.println("-----------------------------------");
                }
                f.setNomeforn(campoNome.getText());
                f.setCpfCnpj(campoCpfCnpj.getText());
                f.setCep(campoCEP.getText());
                f.setCidade(campoCidade.getText());
                f.setEndereco(campoEndereco.getText());
                f.setNumero(campoNumero.getText());
                f.setComplemento(campoComplemento.getText());
                f.setBairro(campoBairro.getText());
                f.setEmail(campoEmail.getText());
                f.setTelefone(campoTelefone.getText());
                f.setSiteRede(campoSiteRede.getText());
                f.setObservacao(campoObservacao.getText());
                if((campoCodigo.getText().isEmpty()) || (campoData.getText().isEmpty()) || (campoNome.getText().isEmpty())){
                    MensagemSistema.mostrarAvisoDark(this, "Campos mandatórios devem estar preenchidos: Tipo, Código, Data, Nome!");
                } else{
                    atualizarCadastroFornecedor();
                    MensagemSistema.mostrarAvisoDark(this, "Dados atualizados!");
                    limparCamposCadastroFornecedor();
                    System.out.println("Atualização realizada com sucesso!");
                    System.out.println("----------------------");               
                    buttonPesquisar.setEnabled(true);
                    buttonSalvar.setEnabled(true);                                     
                }                        
            } catch (ParseException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Erro: "+ex.getMessage());
                System.out.println("----------------------");
            }
        }else{
            System.out.println("Cancelou a alteração");
            System.out.println("-----------------------");
            buttonPesquisar.setEnabled(false);
            buttonSalvar.setEnabled(false);
        }       
    }//GEN-LAST:event_buttonEditarActionPerformed
    ////// Botao Excluir //////
    private void buttonExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExcluirActionPerformed
        int resposta = JOptionPane.showConfirmDialog(this, "Deseja excluir o fornecedor?", "Aviso", JOptionPane.YES_NO_OPTION);
        if(resposta == YES_OPTION){
            f.setTipoforn(s);
            f.setCodforn(campoCodigo.getText());
            System.out.println("Codigo do fornecedor: "+campoCodigo.getText());
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");      
            try {
                f.setDataCadastramento(fmt.parse(campoData.getText()));
                System.out.println(campoData.getText());               
            } catch (ParseException ex) {
                Logger.getLogger(TelaEstoque.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Erro na data: "+ex.getMessage());
            }
            f.setUltimoLote(campoLote.getText());
            f.setPrecoMedio(Double.valueOf(campoPrecoMedio.getText()));
            f.setNomeforn(campoNome.getText());
            f.setCpfCnpj(campoCpfCnpj.getText());
            f.setCep(campoCEP.getText());
            f.setCidade(campoCidade.getText());
            f.setEndereco(campoEndereco.getText());
            f.setNumero(campoNumero.getText());
            f.setComplemento(campoComplemento.getText());
            f.setBairro(campoBairro.getText());
            f.setEmail(campoEmail.getText());
            f.setTelefone(campoTelefone.getText());
            f.setSiteRede(campoSiteRede.getText());
            f.setObservacao(campoObservacao.getText());           
            if((rButtonDesapego.getText().isEmpty()) || (rButtonOutlet.getText().isEmpty()) || (campoCodigo.getText().isEmpty()) || (campoData.getText().isEmpty()) || (campoNome.getText().isEmpty())){
                MensagemSistema.mostrarAvisoDark(this, "Não existem dados para exclusão!");
            }else{
                try {
                    excluirCadastroFornecedor();
                } catch (IOException ex) {
                    Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                }
                MensagemSistema.mostrarAvisoDark(this, "FORNECEDOR removido com sucesso!");
            }
            limparCamposCadastroFornecedor();
        }else{
            System.out.println("Cancelou a exclusão");
            System.out.println("----------------------");
        }      
        buttonPesquisar.setEnabled(true);
        buttonSalvar.setEnabled(true);
    }//GEN-LAST:event_buttonExcluirActionPerformed

    private void campoDataMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoDataMouseClicked
        LocalDateTime date = LocalDateTime.now();
        String data = date.format(DateTimeFormatter.ISO_DATE);
        String dia = data.substring(8, 10);
        String mes = data.substring(5, 7);
        String ano = data.substring(0, 4);
        campoData.setText(""+dia+"/"+mes+"/"+ano+"");
    }//GEN-LAST:event_campoDataMouseClicked

    private void campoCodigoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoCodigoMouseClicked
        int x = 0;
        try {
            fdao.selectCodFornecedor(f);
            String codigo = f.getCodforn();
            if(codigo != null){
                x = Integer.parseInt(codigo);
                String CodFornecedor = String.valueOf(x+1);
                campoCodigo.setText(CodFornecedor);
            }else{
                campoCodigo.setText("1");
            }           
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex.getMessage());
        }
    }//GEN-LAST:event_campoCodigoMouseClicked

    private void campoEmailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoEmailFocusLost
        String email = campoEmail.getText();
        if(!email.isEmpty()){
            if(email.length() > 0 && email.indexOf("@") > 0){
                System.out.println("Confirmou email: "+email);
            }else{
                MensagemSistema.mostrarAvisoDark(this, "Email fora do padrão esperado!");
            }
        }else{
            System.out.println("Campo email vazio!");
        }
    }//GEN-LAST:event_campoEmailFocusLost

    private void campoCpfCnpjFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoCpfCnpjFocusLost
        if(campoCpfCnpj.getText().isEmpty()){
            System.out.println("Campo CPF/CNPJ vazio!");
            MensagemSistema.mostrarAvisoDark(this, "Campo CPF/CNPJ deve ser preenchido!");
        }else{
            String cpf = campoCpfCnpj.getText();
            if(cpf.length() == 11 && rButtonCPF.isSelected()){                
                String seq1 = cpf.substring(0,3);
                String seq2 = cpf.substring(3,6);
                String seq3 = cpf.substring(6,9);
                String seq4 = cpf.substring(9,11);
                campoCpfCnpj.setText(seq1+"."+seq2+"."+seq3+"-"+seq4);
                System.out.println(seq1+"."+seq2+"."+seq3+"-"+seq4);
            }else{
                if(rButtonCNPJ.isSelected()){
                    String cnpj = campoCpfCnpj.getText();
                    String seq1 = cnpj.substring(0,2);
                    String seq2 = cnpj.substring(2,5);
                    String seq3 = cnpj.substring(5,8);
                    String seq4 = cnpj.substring(8,12);
                    String seq5 = cnpj.substring(12,14);
                    campoCpfCnpj.setText(seq1+"."+seq2+"."+seq3+"/"+seq4+"-"+seq5);
                    System.out.println(seq1+"."+seq2+"."+seq3+"/"+seq4+"-"+seq5);
                }else{
                    if(!rButtonCNPJ.isSelected() && !rButtonCPF.isSelected()){                        
                        MensagemSistema.mostrarAvisoDark(this, "Favor selecionar opção correta CPF ou CNPJ.");
                        campoCpfCnpj.setText("");
                    }else{
                        MensagemSistema.mostrarAvisoDark(this, "CPF pu CNPJ fora do pradrão esperado!");
                        campoCpfCnpj.setText("");
                    }
                }
            }                     
        }
    }//GEN-LAST:event_campoCpfCnpjFocusLost

    private void campoTelefoneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoTelefoneFocusLost
        String fone = campoTelefone.getText();
        if(fone.isEmpty()){
            System.out.println("Campo telefone vazio!");
        }else{
            if(fone.length() == 11){
                String ddd = fone.substring(0,2);
                System.out.println(ddd);
                String prefixo = fone.substring(2,7);
                String sufixo = fone.substring(7,11);
                campoTelefone.setText("("+ddd+") "+prefixo+"-"+sufixo);
                System.out.println("Confirmou número: ("+ddd+") "+prefixo+"-"+sufixo);
            }
            if(fone.length() == 10){    
                String ddd = fone.substring(0,2);
                System.out.println(ddd);
                String prefixo = fone.substring(2,6);
                String sufixo = fone.substring(6,10);
                campoTelefone.setText("("+ddd+") "+prefixo+"-"+sufixo);
                System.out.println("Confirmou número: ("+ddd+") "+prefixo+"-"+sufixo);
            }
            if(fone.length() < 10 || fone.length() > 11){
                MensagemSistema.mostrarAvisoDark(this, "Telefone fora do pradrão esperado!");
                campoTelefone.setText("");
            }
        }      
    }//GEN-LAST:event_campoTelefoneFocusLost

    private void campoCEPFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_campoCEPFocusLost
        if(campoCEP.getText().isEmpty()){
            System.out.println("Campo CEP vazio!");          
        }else{
            String cep = campoCEP.getText();
            if(cep.length() == 8){
                String seq1 = cep.substring(0,5);
                    String seq2 = cep.substring(5,8);
                campoCEP.setText(seq1+"-"+seq2);
                System.out.println("Entrou com CEP: "+seq1+"-"+seq2);
            }else{
                MensagemSistema.mostrarAvisoDark(this, "CEP fora do padrão esperado!");
                campoCEP.setText("");
            }
        }
    }//GEN-LAST:event_campoCEPFocusLost

    private void campoLoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoLoteActionPerformed
        lote = campoLote.getText();
    }//GEN-LAST:event_campoLoteActionPerformed

    private void campoPrecoMedioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoPrecoMedioActionPerformed
        precoMedio = String.valueOf(campoPrecoMedio.getText());
    }//GEN-LAST:event_campoPrecoMedioActionPerformed

    private void campoUFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoUFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoUFActionPerformed

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
                new TelaFornecedor().setVisible(true);
            }
        });
    }
    /////////////  Metodos da classe  ///////////////
    
    public void salvarCadastroFornecedor() throws ParseException{
        System.out.println("Iniciando inclusão de cadastro de Fornecedor...");
        try {
            fdao.saveFornecedor(f);
            fdao.saveFornecedorCloud(f);
        } catch (ClassNotFoundException | SQLException ex) {
           Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
           System.out.println("Erro: "+ex.getMessage());
        }
    }
    
    public void atualizarCadastroFornecedor() throws ParseException{
        System.out.println("Iniciando atualização de cadastro de Fornecedor...");
        try {
            fdao.updateFornecedor(f);
            fdao.updateFornecedorCloud(f);
        } catch (ClassNotFoundException | SQLException ex) {
           Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
           System.out.println("Erro: "+ex.getMessage());
        }
    }
    
    public void pesquisarCadastroFornecedor() throws IOException{
        System.out.println("Iniciando pesquisa de cadastro de Fornecedor...");
        try {
            fdao.selectFornecedorCloud(s, f);
        } catch (ClassNotFoundException | SQLException ex) {
           Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
           System.out.println("Erro: "+ex.getMessage());
        }
    }
    
    public void excluirCadastroFornecedor() throws IOException{
        System.out.println("Iniciando pesquisa de cadastro de Fornecedor...");
        try {
            fdao.deleteFornecedor(f);
            fdao.deleteFornecedorCloud(f);
        } catch (ClassNotFoundException | SQLException ex) {
           Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
           System.out.println("Erro: "+ex.getMessage());
        }
    }
    
    public void limparCamposCadastroFornecedor(){
        System.out.println("------------------------------------------------------");
        System.out.println("Iniciando limpeza dos campos de cadastro de Fornecedor...");
        bg.add(rButtonDesapego);
        bg.add(rButtonOutlet);
        bg.add(rButtonCPF);
        bg.add(rButtonCNPJ);
        if(rButtonDesapego.isSelected() || rButtonOutlet.isSelected() || rButtonCPF.isSelected() || rButtonCNPJ.isSelected()){
            bg.clearSelection();
        }
        campoCodigo.setText("");
        campoData.setText("");
        campoLote.setText("");
        campoPrecoMedio.setText("");
        campoNome.setText("");
        campoCpfCnpj.setText("");
        campoCEP.setText("");
        campoCidade.setText("");
        campoEndereco.setText("");
        campoNumero.setText("");
        campoComplemento.setText("");
        campoBairro.setText("");
        campoEmail.setText("");
        campoTelefone.setText("");
        campoSiteRede.setText("");
        campoObservacao.setText(""); 
        System.out.println("Limpeza dos campos de cadastro de Fornecedor concluida");
        System.out.println("------------------------------------------------------");
    }
    
    private String tipoFornecedor() {
        try{
            if(rButtonDesapego.equals("Desapego")){
                rButtonDesapego.doClick();
                System.out.println("Desapego");
                System.out.println("------------------------------------");
            }else{
                rButtonOutlet.doClick();
                System.out.println("Outlet");
                System.out.println("------------------------------------");
            }
        }catch(Exception e){
            System.out.println("Não selecionou tipo: "+e.getMessage());
            System.out.println("------------------------------------");
        }
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonEditar;
    private javax.swing.JButton buttonExcluir;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton buttonLimpar;
    private javax.swing.JButton buttonMenu;
    private javax.swing.JButton buttonPesquisar;
    private javax.swing.JButton buttonSalvar;
    private javax.swing.JTextField campoBairro;
    private javax.swing.JTextField campoCEP;
    private javax.swing.JTextField campoCidade;
    private javax.swing.JTextField campoCodigo;
    private javax.swing.JTextField campoComplemento;
    private javax.swing.JTextField campoCpfCnpj;
    private javax.swing.JFormattedTextField campoData;
    private javax.swing.JTextField campoEmail;
    private javax.swing.JTextField campoEndereco;
    private javax.swing.JTextField campoLote;
    private javax.swing.JTextField campoNome;
    private javax.swing.JTextField campoNumero;
    private javax.swing.JTextArea campoObservacao;
    private javax.swing.JTextField campoPrecoMedio;
    private javax.swing.JTextField campoSiteRede;
    private javax.swing.JTextField campoTelefone;
    private javax.swing.JTextField campoUF;
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labelUF;
    private javax.swing.JRadioButton rButtonCNPJ;
    private javax.swing.JRadioButton rButtonCPF;
    private javax.swing.JRadioButton rButtonDesapego;
    private javax.swing.JRadioButton rButtonOutlet;
    // End of variables declaration//GEN-END:variables

    
}
