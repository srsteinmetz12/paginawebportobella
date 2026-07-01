package views;

import dao.ClienteDAO;
import java.awt.HeadlessException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Cliente;
import util.MensagemSistema;

public class TelaCliente extends javax.swing.JFrame {

    public static String cc;
    ClienteDAO cdao = new ClienteDAO();       
    Cliente c = new Cliente();
    int anoAtual = java.time.Year.now().getValue();
    
    @SuppressWarnings("LeakingThisInConstructor")
    public TelaCliente() {
        this.setUndecorated(true);
        initComponents();
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
                // --- 1. PALETA LUXO/MODA PREMIUM CONTÍNUA (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C

        // --- 2. REVESTIMENTO DO PAINEL DE FUNDO (MANTÉM O ALINHAMENTO ORIGINAL) ---
        // Altere 'jPanel1' para o nome real do seu painel se necessário
        try {
            jPanel1.setBackground(grafiteProfundo);
            jPanel1.setOpaque(true);
        } catch(Exception e) {}
        this.getContentPane().setBackground(grafiteProfundo);

        // --- 3. ⚖️ HIERARQUIA DE FONTES OPERACIONAIS CONTRASTANTES ---
        // Nível 1: Título Indicador da Tela (CADASTRO DE CLIENTES)
        jLabel1.setForeground(brancoPuro);
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));

        // Nível 2: Subtítulo de Seção (DADOS DE CADASTRO)
        try {
            jLabel2.setForeground(brancoPuro); 
            jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        } catch(Exception e) {}

        // Nível 3: Rótulos Menores de Campos Comuns (CÓDIGO, NOME, CEP, TAMANHO, OBSERVAÇÕES...)
        javax.swing.JLabel[] labelsCamposClientes = {
            jLabel3, jLabel4, jLabel5, jLabel6, jLabel7, jLabel8, jLabel9, jLabel10, 
            jLabel11, jLabel12, jLabel13, jLabel14, jLabel15, jLabel16, jLabel17
        };
        for (javax.swing.JLabel lbl : labelsCamposClientes) {
            try {
                lbl.setForeground(brancoPuro);
                lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
            } catch(Exception e) {}
        }

        // --- 4. CAMPOS DE ENTRADA DARK SLIM (JTextFields e JTextArea) ---
        javax.swing.JTextField[] todosCamposClientes = {
            campoCodigoCliente, campoDataCadastroCli, campoNomeCliente, campoCepCliente, campoCidadeCli, campoUFCli, 
            campoEndCli, campoNumeroCli, campoComplCli, campoBairroCli, campoTamanhoCli,
            campoEmailCli, campoTelCli, campoRedeCli
        };
        for (javax.swing.JTextField txt : todosCamposClientes) {
            try {
                txt.setBackground(grafiteClaro);
                txt.setForeground(brancoPuro);
                txt.setCaretColor(brancoPuro);
                txt.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); // Linha fina reta
                txt.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            } catch(Exception e) {}
        }
        
        // Pinta a grande caixa branca das Observações de Clientes [10]
        try {
            campoObsCli.setBackground(grafiteClaro);
            campoObsCli.setForeground(brancoPuro);
            campoObsCli.setCaretColor(brancoPuro);
            campoObsCli.setBorder(null);
            campoObsCli.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            
            // Elimina o fundo branco residual do painel de rolagem [10]
            jScrollPane1.setBackground(grafiteClaro);
            jScrollPane1.getViewport().setBackground(grafiteClaro);
            jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
        } catch(Exception e) {}

        // --- 5. ESTILIZAÇÃO DOS SEPARADORES DE LINHA (JSeparator) ---
        javax.swing.JSeparator[] todosSeparadores = {
            jSeparator1, jSeparator2
        };
        for (javax.swing.JSeparator sep : todosSeparadores) {
            try {
                sep.setForeground(cinzaLinhas);
                sep.setBackground(cinzaLinhas);
            } catch(Exception e) {}
        }

        // --- 6. VETOR DE BOTÕES: DESIGN PLANO EMBUTIDO (FLAT STYLE) ---
        javax.swing.JButton[] botoesBaseClientes = {
            buttonMenu, buttonPesquisar, buttonLimpar, buttonExcluir, buttonEditar, buttonSalvar
        };

        for (javax.swing.JButton btn : botoesBaseClientes) {
            try {
                btn.setBackground(grafiteClaro);
                btn.setForeground(brancoPuro);
                btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false); // 🔥 Remove o contorno tridimensional chanfrado do Windows antigo! [10]
                btn.putClientProperty("JButton.buttonType", "square"); // Força o FlatLaf Plano [10]
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
                // --- CONFIGURAÇÃO DA BARRA DE TÍTULO PREMIUM NA TELA DE CLIENTES ---
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
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Cadastro de Clientes");
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
        
        // 4. Posiciona e estica a barra no topo exato da tela de cadastro
        barraTituloPremium.setBounds(0, 0, this.getWidth(), 30);

        // 5. Motor de Movimentação utilizando a referência de janelaAtual
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
        
        this.setLocationRelativeTo(null);
//        this.setIconImage(new ImageIcon(getClass().getResource("/images/favicon.png")).getImage());
        this.setTitle(util.ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI LTDA");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        campoCodigoCliente = new javax.swing.JTextField();
        campoDataCadastroCli = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        campoNomeCliente = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        campoCepCliente = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        campoCidadeCli = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        campoEndCli = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        campoNumeroCli = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        campoComplCli = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        campoBairroCli = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        campoEmailCli = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        campoTelCli = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        campoRedeCli = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        campoObsCli = new javax.swing.JTextArea();
        jSeparator2 = new javax.swing.JSeparator();
        buttonMenu = new javax.swing.JButton();
        buttonPesquisar = new javax.swing.JButton();
        buttonLimpar = new javax.swing.JButton();
        buttonSalvar = new javax.swing.JButton();
        buttonEditar = new javax.swing.JButton();
        buttonExcluir = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        campoTamanhoCli = new javax.swing.JTextField();
        campoUFCli = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 30)); // NOI18N
        jLabel1.setText("CADASTRO DE CLIENTES");

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel2.setText("DADOS DE CADASTRO");

        campoCodigoCliente.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCodigoCliente.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCodigoCliente.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoCodigoCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        campoCodigoCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoCodigoClienteMouseClicked(evt);
            }
        });
        campoCodigoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoCodigoClienteActionPerformed(evt);
            }
        });

        campoDataCadastroCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoDataCadastroCli.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoDataCadastroCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoDataCadastroCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        campoDataCadastroCli.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                campoDataCadastroCliMouseClicked(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel3.setText("CÓDIGO");

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel4.setText("DATA");

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel5.setText("NOME");

        campoNomeCliente.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoNomeCliente.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoNomeCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel6.setText("CEP");

        campoCepCliente.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCepCliente.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoCepCliente.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoCepCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel7.setText("CIDADE");

        campoCidadeCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoCidadeCli.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoCidadeCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoCidadeCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel8.setText("ENDEREÇO");

        campoEndCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoEndCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoEndCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel9.setText(" NÚMERO");

        campoNumeroCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoNumeroCli.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoNumeroCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoNumeroCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel10.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel10.setText("COMPLEMENTO");

        campoComplCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoComplCli.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoComplCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoComplCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel11.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel11.setText("BAIRRO");

        campoBairroCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoBairroCli.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoBairroCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoBairroCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel12.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel12.setText("EMAIL");

        campoEmailCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoEmailCli.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        campoEmailCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoEmailCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel13.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel13.setText("TELEFONE");

        campoTelCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoTelCli.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoTelCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoTelCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel14.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel14.setText("REDE SOCIAL");

        campoRedeCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoRedeCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoRedeCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel15.setFont(new java.awt.Font("Times New Roman", 1, 13)); // NOI18N
        jLabel15.setText("OBSERVAÇÕES");

        campoObsCli.setColumns(20);
        campoObsCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoObsCli.setRows(5);
        campoObsCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoObsCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(campoObsCli);

        buttonMenu.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenu.setText("MENU");
        buttonMenu.setBorder(null);
        buttonMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuActionPerformed(evt);
            }
        });

        buttonPesquisar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonPesquisar.setText("PESQUISAR");
        buttonPesquisar.setBorder(null);
        buttonPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPesquisarActionPerformed(evt);
            }
        });

        buttonLimpar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonLimpar.setText("LIMPAR");
        buttonLimpar.setBorder(null);
        buttonLimpar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLimparActionPerformed(evt);
            }
        });

        buttonSalvar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonSalvar.setText("SALVAR");
        buttonSalvar.setBorder(null);
        buttonSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSalvarActionPerformed(evt);
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

        jLabel16.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel16.setText("TAMANHO");

        campoTamanhoCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoTamanhoCli.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoTamanhoCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoTamanhoCli.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        campoUFCli.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoUFCli.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        campoUFCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoUFCli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoUFCliActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel17.setText("UF");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(84, 84, 84)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel1))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(124, 124, 124)
                        .addComponent(jLabel14)
                        .addGap(239, 239, 239))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(campoEmailCli, javax.swing.GroupLayout.PREFERRED_SIZE, 367, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoTelCli, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoRedeCli))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addComponent(campoCodigoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(46, 46, 46)
                                                .addComponent(jLabel4))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(campoDataCadastroCli, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(campoNomeCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(campoCepCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(campoEndCli)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel8)
                                                        .addGap(218, 218, 218)))
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(campoNumeroCli, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(campoComplCli, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(jLabel10)))))
                                        .addGap(3, 3, 3))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel6)
                                        .addGap(52, 52, 52)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(campoCidadeCli, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(campoUFCli, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                        .addGap(45, 45, 45)
                                                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(36, 36, 36))
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addGap(3, 3, 3)
                                                        .addComponent(campoBairroCli, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(campoTamanhoCli)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel16)
                                                        .addGap(0, 0, Short.MAX_VALUE)))))
                                        .addGap(3, 3, 3))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel7)
                                        .addGap(103, 103, 103)
                                        .addComponent(jLabel17)
                                        .addGap(25, 25, 25))))
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(106, 106, 106))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoDataCadastroCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoCodigoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoNomeCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoCepCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoCidadeCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoUFCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoEndCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoNumeroCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoComplCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoBairroCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoTamanhoCli, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel14)
                        .addComponent(jLabel13)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoEmailCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoTelCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campoRedeCli, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(91, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1002, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuActionPerformed
        new TelaMenu().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonMenuActionPerformed

    private void buttonSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSalvarActionPerformed
        // ==========================================
        // VALIDAÇÃO DOS CAMPOS OBRIGATÓRIOS
        // ==========================================
        if (campoDataCadastroCli.getText().isEmpty() || 
            campoNomeCliente.getText().isEmpty() || 
            campoCodigoCliente.getText().isEmpty()) {

            MensagemSistema.mostrarAvisoDark(this, "Campos Código, Data e Nome devem ser preenchidos!");
            System.out.println("❌ Campos obrigatórios não preenchidos!");
            return;
        }

        try {
            // ==========================================
            // PREENCHER OBJETO CLIENTE
            // ==========================================
            c.setCodCli(campoCodigoCliente.getText());

            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            try {
                c.setDataCadastro(fmt.parse(campoDataCadastroCli.getText()));
            } catch (ParseException ex) {
                MensagemSistema.mostrarAvisoDark(this, "Data inválida! Use o formato dd/MM/yyyy");
                System.err.println("❌ Erro ao converter data: " + ex.getMessage());
                return;
            }

            c.setNomeCli(campoNomeCliente.getText());
            c.setCepCli(campoCepCliente.getText());
            c.setCidadeCli(campoCidadeCli.getText());
            c.setUFCli(campoUFCli.getText());
            c.setEnderecoCli(campoEndCli.getText());
            c.setNumeroCli(campoNumeroCli.getText());
            c.setComplementoCli(campoComplCli.getText());
            c.setBairroCli(campoBairroCli.getText());
            c.setTamanhoCli(campoTamanhoCli.getText());
            c.setEmailCli(campoEmailCli.getText());
            c.setTelefoneCli(campoTelCli.getText());
            c.setRedeCli(campoRedeCli.getText());
            c.setObsCli(campoObsCli.getText());

            // ==========================================
            // 🔥 SALVAR E VERIFICAR SE FOI BEM SUCEDIDO
            // ==========================================
            boolean salvou = salvaCadastroCliente();

            if (salvou) {
                MensagemSistema.mostrarAvisoDark(this, "Registro SALVO na base com sucesso!");
                System.out.println("✅ Registro efetuado com sucesso!");
                System.out.println("-----------------------------------");
                limpaCamposCadastroClientes();
            } else {
                MensagemSistema.mostrarAvisoDark(this, "❌ Erro ao salvar registro! Tente novamente.");
                System.err.println("❌ Falha ao salvar cliente!");
            }

        } catch (HeadlessException ex) {
            System.err.println("❌ Erro: " + ex.getMessage());
            MensagemSistema.mostrarAvisoDark(this, "❌ Erro ao salvar: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("❌ Erro inesperado: " + ex.getMessage());
            ex.printStackTrace();
            MensagemSistema.mostrarAvisoDark(this, "❌ Erro inesperado: " + ex.getMessage());
        }
    }//GEN-LAST:event_buttonSalvarActionPerformed

    private void campoDataCadastroCliMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoDataCadastroCliMouseClicked
        LocalDateTime date = LocalDateTime.now();
        String data = date.format(DateTimeFormatter.ISO_DATE);
        String dia = data.substring(8, 10);
        String mes = data.substring(5, 7);
        String ano = data.substring(0, 4);
        campoDataCadastroCli.setText(""+dia+"/"+mes+"/"+ano+"");
    }//GEN-LAST:event_campoDataCadastroCliMouseClicked

    private void campoCodigoClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_campoCodigoClienteMouseClicked
        int x = 0;
        try {
            cdao.selectCodClienteCloud(c);       
            String codigo = c.getCodCli();
            if(codigo != null){
                x = Integer.parseInt(codigo);
                String CodCliente = String.valueOf(x+1);
                campoCodigoCliente.setText(CodCliente);
            }else{
                campoCodigoCliente.setText("1");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaCliente.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
    }//GEN-LAST:event_campoCodigoClienteMouseClicked

    private void buttonPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPesquisarActionPerformed
        cc = MensagemSistema.mostrarInputDark(this, "Entre com Código do cliente");             
        pesquisaClienteCadastro();
        campoCodigoCliente.setText(c.getCodCli());
        campoDataCadastroCli.setText(c.getDataCadastro().toString().replaceAll("-", "/"));
        String data = campoDataCadastroCli.getText();
        System.out.println(data);
        String dia = data.substring(8, 10);
        System.out.println(dia);
        String mes = data.substring(5, 7);
        System.out.println(mes);
        String ano = data.substring(0, 4);
        System.out.println(ano);
        campoDataCadastroCli.setText(""+dia+"/"+mes+"/"+ano+"");
        System.out.println(campoDataCadastroCli.getText());
        campoNomeCliente.setText(c.getNomeCli());
        campoCepCliente.setText(c.getCepCli());
        campoCidadeCli.setText(c.getCidadeCli());
        campoUFCli.setText(c.getUFCli());
        campoEndCli.setText(c.getEnderecoCli());
        campoNumeroCli.setText(c.getNumeroCli());
        campoComplCli.setText(c.getComplementoCli());
        campoBairroCli.setText(c.getBairroCli());
        campoTamanhoCli.setText(c.getTamanhoCli());
        campoEmailCli.setText(c.getEmailCli());
        campoTelCli.setText(c.getTelefoneCli());
        campoRedeCli.setText(c.getRedeCli());
        campoObsCli.setText(c.getObsCli());      
        buttonPesquisar.setEnabled(false);
        buttonSalvar.setEnabled(false);
    }//GEN-LAST:event_buttonPesquisarActionPerformed

    private void buttonLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimparActionPerformed
        limpaCamposCadastroClientes();
        System.out.println("Registro de tela limpo com sucesso!");
        System.out.println("-----------------------------------");      
        buttonPesquisar.setEnabled(true);
        buttonSalvar.setEnabled(true);       
    }//GEN-LAST:event_buttonLimparActionPerformed

    private void buttonEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditarActionPerformed
        boolean resposta = MensagemSistema.mostrarDecisaoDark(this, "Deseja alterar esse registro?");
        if(resposta == true){
            c.setCodCli(campoCodigoCliente.getText());
            System.out.println(campoCodigoCliente.getText());
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            try {
                c.setDataCadastro(fmt.parse(campoDataCadastroCli.getText()));
                System.out.println(campoDataCadastroCli.getText());
                System.out.println("-----------------------------------");
                
            } catch (ParseException ex) {
                Logger.getLogger(TelaCliente.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Erro na data: "+ex.getMessage());
                System.out.println("-----------------------------------");
            }
            c.setNomeCli(campoNomeCliente.getText());
            c.setCepCli(campoCepCliente.getText());
            c.setCidadeCli(campoCidadeCli.getText());
            c.setUFCli(campoUFCli.getText());
            c.setEnderecoCli(campoEndCli.getText());
            c.setNumeroCli(campoNumeroCli.getText());
            c.setComplementoCli(campoComplCli.getText());
            c.setBairroCli(campoBairroCli.getText());
            c.setTamanhoCli(campoTamanhoCli.getText());
            c.setEmailCli(campoEmailCli.getText());
            c.setTelefoneCli(campoTelCli.getText());
            c.setRedeCli(campoRedeCli.getText());
            System.out.println(campoRedeCli.getText());
            c.setObsCli(campoObsCli.getText());
            if((campoCodigoCliente.getText().isEmpty()) || (campoDataCadastroCli.getText().isEmpty()) || (campoNomeCliente.getText().isEmpty())){
                MensagemSistema.mostrarAvisoDark(this, "Campos mandatórios devem estar preenchidos: Código, Data e Nome!");
            } else{
                atualizaCadastroCliente();
                MensagemSistema.mostrarAvisoDark(this, "Dados atualizados!");
                limpaCamposCadastroClientes();
                System.out.println("Atualização realizada com sucesso!");
                System.out.println("-----------------------------------");
                
                buttonPesquisar.setEnabled(true);
                buttonSalvar.setEnabled(true);
                
            }
        }else{
            System.out.println("Cancelou a alteração");
            System.out.println("-----------------------------------");
            buttonPesquisar.setEnabled(false);
            buttonSalvar.setEnabled(false);
        }
    }//GEN-LAST:event_buttonEditarActionPerformed

    private void buttonExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExcluirActionPerformed
        boolean resposta = MensagemSistema.mostrarDecisaoDark(this, "Deseja excluir o Cliente?");
        if(resposta == true){           
            c.setCodCli(campoCodigoCliente.getText());
            System.out.println("Codigo do Cliente: "+campoCodigoCliente.getText());
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");      
            try {
                c.setDataCadastro(fmt.parse(campoDataCadastroCli.getText()));
                System.out.println(campoDataCadastroCli.getText());
                
            } catch (ParseException ex) {
                Logger.getLogger(TelaCliente.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Erro na data: "+ex.getMessage());
            }
            c.setNomeCli(campoNomeCliente.getText());
            c.setCepCli(campoCepCliente.getText());
            c.setCidadeCli(campoCidadeCli.getText());
            c.setUFCli(campoUFCli.getText());
            c.setEnderecoCli(campoEndCli.getText());
            c.setNumeroCli(campoNumeroCli.getText());
            c.setComplementoCli(campoComplCli.getText());
            c.setBairroCli(campoBairroCli.getText());
            c.setTamanhoCli(campoTamanhoCli.getText());
            c.setEmailCli(campoEmailCli.getText());
            c.setTelefoneCli(campoTelCli.getText());
            c.setRedeCli(campoRedeCli.getText());
            c.setObsCli(campoObsCli.getText());           
            if((campoCodigoCliente.getText().isEmpty()) || (campoDataCadastroCli.getText().isEmpty()) || (campoNomeCliente.getText().isEmpty())){
                MensagemSistema.mostrarAvisoDark(this, "Não existem dados para exclusão!");
            }else{
                excluirClienteCadastro();
                MensagemSistema.mostrarAvisoDark(this, "Cliente removido com sucesso!");
            }
            limpaCamposCadastroClientes();
        }else{
            System.out.println("Cancelou a exclusão");
            System.out.println("----------------------");
        }       
        buttonPesquisar.setEnabled(true);
        buttonSalvar.setEnabled(true);
    }//GEN-LAST:event_buttonExcluirActionPerformed

    private void campoCodigoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoCodigoClienteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoCodigoClienteActionPerformed

    private void campoUFCliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoUFCliActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoUFCliActionPerformed

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
            java.util.logging.Logger.getLogger(TelaCliente.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaCliente().setVisible(true);
            }
        });
    }
    
    //////////////// METODOS DA CLASSE ////////////////////////
    
    private boolean salvaCadastroCliente() {
        try {
            // ==========================================
            // CHAMAR O DAO PARA SALVAR
            // ==========================================
            cdao.saveClienteCloud(c);

            // ==========================================
            // VERIFICAR SE O CLIENTE FOI INSERIDO
            // ==========================================
            // Opção 1: Se o DAO retornar boolean
            // return cdao.inserirClienteCloud(c);

            // Opção 2: Se o DAO lançar exceção em caso de erro
            // Se chegou aqui, salvou com sucesso
            return true;

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Erro inesperado: " + e.getMessage());
            return false;
        }
    }
    
//    public void salvaCadastroCliente(){
//        System.out.println("Inserindo Cliente na base dde dados...");
//        try {
//            cdao.saveCliente(c);
//            cdao.saveClienteCloud(c);
//            System.out.println("Cliente inserido com sucesso!");
//        } catch (ClassNotFoundException | SQLException ex) {
//            Logger.getLogger(TelaCliente.class.getName()).log(Level.SEVERE, null, ex);
//            System.out.println("Erro: "+ ex.getMessage());
//        }   
//    }
    
    public void atualizaCadastroCliente(){
        System.out.println("Iniciando atualização do cadastro do cliente...");
        try {
            cdao.updateCliente(c);
            cdao.updateClienteCloud(c);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaCliente.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ ex.getMessage());
        }
    }
    
    public void pesquisaClienteCadastro(){
        System.out.println("Iniciando pesquisa do cliente...");;
        try {
            cdao.selectClienteCloud(cc, c);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaCliente.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ ex.getMessage());
        }
    }
    
    public void excluirClienteCadastro(){
        System.out.println("Iniciando exclusão de cadastro de cliente...");
        try {
            cdao.deleteCliente(c);
            cdao.deleteClienteCloud(c);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TelaCliente.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro: "+ ex.getMessage());
        }
    }
    
    public void limpaCamposCadastroClientes(){
        campoCodigoCliente.setText("");
        campoDataCadastroCli.setText("");
        campoNomeCliente.setText("");
        campoCepCliente.setText("");
        campoCidadeCli.setText("");
        campoUFCli.setText("");
        campoEndCli.setText("");
        campoNumeroCli.setText("");
        campoComplCli.setText("");
        campoBairroCli.setText("");
        campoTamanhoCli.setText("");
        campoEmailCli.setText("");
        campoTelCli.setText("");
        campoRedeCli.setText("");
        campoObsCli.setText("");
        System.out.println("Campos limpos com sucesso!");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonEditar;
    private javax.swing.JButton buttonExcluir;
    private javax.swing.JButton buttonLimpar;
    private javax.swing.JButton buttonMenu;
    private javax.swing.JButton buttonPesquisar;
    private javax.swing.JButton buttonSalvar;
    private javax.swing.JTextField campoBairroCli;
    private javax.swing.JTextField campoCepCliente;
    private javax.swing.JTextField campoCidadeCli;
    private javax.swing.JTextField campoCodigoCliente;
    private javax.swing.JTextField campoComplCli;
    private javax.swing.JTextField campoDataCadastroCli;
    private javax.swing.JTextField campoEmailCli;
    private javax.swing.JTextField campoEndCli;
    private javax.swing.JTextField campoNomeCliente;
    private javax.swing.JTextField campoNumeroCli;
    private javax.swing.JTextArea campoObsCli;
    private javax.swing.JTextField campoRedeCli;
    private javax.swing.JTextField campoTamanhoCli;
    private javax.swing.JTextField campoTelCli;
    private javax.swing.JTextField campoUFCli;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
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
    // End of variables declaration//GEN-END:variables
}
