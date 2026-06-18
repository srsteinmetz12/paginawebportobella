package views;

import javax.swing.ButtonGroup;
import com.itextpdf.text.DocumentException;
import dao.RelatorioDAO;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Fornecedor;
import models.Produto;
import models.Vendas;
import util.MensagemSistema;

public class TelaRelatorios extends javax.swing.JFrame {

    public static String relatorio;
    public static String nomeRelatorio;
    public static String dtInicial;
    public static String dtFinal;
    public static String dtIni;
    public static String dtFim;
    public static String descricao;
    public static String codigo;
    public static String marca;
    public static String tamanho;
    public static String fornecedor;
    RelatorioDAO rdao = new RelatorioDAO();
    Produto p = new Produto();
    Fornecedor f = new Fornecedor();
    ButtonGroup bg = new ButtonGroup();
    Vendas v = new Vendas();
    int anoAtual = java.time.Year.now().getValue();
    MensagemSistema box = new MensagemSistema();
     
    @SuppressWarnings("LeakingThisInConstructor")
    public TelaRelatorios() {
        this.setUndecorated(true);
        initComponents();
       util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
                // --- 1. PALETA LUXO/MODA PREMIUM CONTÍNUA (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C

        // --- 2. 🔥 DESTRUIÇÃO DO LILÁS: Força a pintura do container real da tela [links: 10]
        this.getContentPane().setBackground(grafiteProfundo);       
        // Pinta de forma defensiva qualquer painel que possa estar em background
        try { jPanel3.setBackground(grafiteProfundo); jPanel3.setOpaque(true); } catch (Exception e) {}
        try { painelCentral.setBackground(grafiteProfundo); painelCentral.setOpaque(true); } catch (Exception e) {}
        // --- 3. ⚖️ HIERARQUIA DE FONTES OPERACIONAIS CORRIGIDA ---
        // Título Indicador de Tela Superior (O "RELATÓRIOS" pequeno do topo)
        jLabel8.setText("RELATÓRIOS");
        jLabel8.setForeground(brancoPuro);
        jLabel8.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        // 🔥 CORREÇÃO CIRÚRGICA: Ajusta o "RELATÓRIOS" de baixo para o tamanho correto de rótulo para não cobrir as datas!
        jLabel12.setText("DATA INICIAL"); // Ajusta o texto que estava gigante e sobreposto
        jLabel12.setForeground(brancoPuro);
        jLabel12.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        // Rótulo da Data Final [links: 10]
        jLabel13.setForeground(brancoPuro);
        jLabel13.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        // Limpa os demais rótulos genéricos da tela de forma segura [links: 10]
        javax.swing.JLabel[] labelsCamposRelatorio = {
            jLabel1, jLabel2, jLabel3, jLabel4, jLabel5, jLabel6, jLabel7, labelCampoDescritivo
        };
        for (javax.swing.JLabel lbl : labelsCamposRelatorio) {
            try {
                lbl.setForeground(brancoPuro);
                lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            } catch (Exception ex
                    ) {System.err.println("Erro: "+ex);}
        }
        // Componentes Circulares de Seleção (RadioButtons)
        javax.swing.JRadioButton[] todosRadios = {
            jRadioButton1, jRadioButton2, jRadioButton3, jRadioButton4, jRadioButton5, jRadioButton6, jRadioButton7
        };
        for (javax.swing.JRadioButton rad : todosRadios) {
            try {
                rad.setForeground(douradoOuro);
                rad.setOpaque(false);
                rad.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
            } catch (Exception ex) {System.err.println("Erro: "+ex);}
        }
        // --- 4. JCOMBOBOX EM MODO DARK COMPLETO (ESCOLHA UM RELATÓRIO) ---
        try {
            jComboRelatorios.setBackground(grafiteClaro);
            jComboRelatorios.setForeground(brancoPuro);
            jComboRelatorios.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            // Força a renderização do menu suspenso interno de opções
            ((javax.swing.JLabel)jComboRelatorios.getRenderer()).setBackground(grafiteClaro);
            ((javax.swing.JLabel)jComboRelatorios.getRenderer()).setForeground(brancoPuro);
        } catch (Exception ex) {System.err.println("Erro: "+ex);}
        // --- 5. 🔥 ESTILIZAÇÃO DOS CALENDÁRIOS (CAMPO DATE INICIAL / FINAL) ---
        try {
            com.toedter.calendar.JDateChooser[] calendariosReais = { campoDateInicial, campoDateFinal };
            for (com.toedter.calendar.JDateChooser dc : calendariosReais) {
                dc.setBackground(grafiteProfundo);
                dc.setOpaque(false);               
                // Força a pintura do input de texto interno do seletor
                dc.getDateEditor().getUiComponent().setBackground(grafiteClaro);
                dc.getDateEditor().getUiComponent().setForeground(brancoPuro);
                dc.getDateEditor().getUiComponent().setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));               
                // Remove os gradientes tridimensionais antigos do quadrado do ícone do calendário
                for (java.awt.Component comp : dc.getComponents()) {
                    if (comp instanceof javax.swing.JButton) {
                        javax.swing.JButton btnCal = (javax.swing.JButton) comp;
                        btnCal.setBackground(grafiteClaro);
                        btnCal.setBorderPainted(false);
                        btnCal.setFocusPainted(false);
                    }
                }
            }
        } catch (Exception e) {}
        // --- 6. ESTILIZAÇÃO DOS SEPARADORES DE LINHA (JSeparator) ---
        javax.swing.JSeparator[] todosSeparadores = {
            jSeparator1, jSeparator2, jSeparator3, jSeparator4, jSeparator5
        };
        for (javax.swing.JSeparator sep : todosSeparadores) {
            try {
                sep.setForeground(cinzaLinhas);
                sep.setBackground(cinzaLinhas);
            } catch (Exception e) {}
        }
        // --- 7. VETOR DE BOTÕES UTILITÁRIOS: DESIGN PLANO FLAT STYLE ---
        javax.swing.JButton[] botoesBaseRelatorios = { buttonMenu, buttonLimpar };
        for (javax.swing.JButton btn : botoesBaseRelatorios) {
            try {
                btn.setBackground(grafiteClaro);
                btn.setForeground(brancoPuro);
                btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false); // Remove as molduras tridimensionais do Windows antigo! [links: 10]
                btn.putClientProperty("JButton.buttonType", "square"); // Força o FlatLaf Plano [links: 10]
            } catch (Exception e) {}
        }
        // --- 8. BOTÃO DE EXECUÇÃO (INICIAR): DESTAQUE MÁXIMO EM DOURADO OURO ---
        try {
            buttonIniciar.setBackground(douradoOuro);
            buttonIniciar.setForeground(new java.awt.Color(0, 0, 0)); // Letras pretas dão leitura máxima no dourado
            buttonIniciar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            buttonIniciar.setFocusPainted(false);
            buttonIniciar.setBorderPainted(false);
            buttonIniciar.putClientProperty("JButton.buttonType", "square");           
            // Trava de segurança operacional: pressionar ENTER na tela dispara o relatório imediatamente
            this.getRootPane().setDefaultButton(buttonIniciar);
        } catch (Exception e) {}
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
                // --- CONFIGURAÇÃO DA BARRA DE TÍTULO PREMIUM NA TELA DE RELATÓRIOS ---
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
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Módulo de Relatórios Gerenciais");
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
        // 4. Posiciona e estica a barra no topo exato da tela de relatórios
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
        // 6. Inserção Inteligente no topo do painel principal (Tratamento para jPanel3 que existe na sua tela)
        try {
            painelCentral.add(barraTituloPremium);
            painelCentral.revalidate();
            painelCentral.repaint();
        } catch(Exception e) {
            try {
                painelCentral.add(barraTituloPremium);
                painelCentral.revalidate();
                painelCentral.repaint();
            } catch(Exception ex) {
                this.getContentPane().add(barraTituloPremium);
                this.getContentPane().revalidate();
                this.getContentPane().repaint();
            }
        }
        // --- 🚀 SOLUÇÃO DE ALTA PERFORMANCE: AGUARDA A RENDERIZAÇÃO PARA PINTAR ---
        // Força a pintura a acontecer em uma fração de segundo após a inicialização, quebrando o bloqueio do NetBeans
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
                java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
                java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);                
                com.toedter.calendar.JDateChooser[] calendariosReais = { campoDateInicial, campoDateFinal };                
                for (com.toedter.calendar.JDateChooser dc : calendariosReais) {
                    // Tira a blindagem nativa do componente
                    dc.setOpaque(false);
                    dc.setBackground(new java.awt.Color(28, 28, 28));                   
                    // Acessa o editor e destrói o cinza fosco antigo do Windows
                    javax.swing.JTextField txtInterno = (javax.swing.JTextField) dc.getDateEditor().getUiComponent();
                    txtInterno.setOpaque(true);
                    txtInterno.setBackground(grafiteClaro); // Crava o cinza escuro plano
                    txtInterno.setForeground(brancoPuro);   // Cravado em branco forte
                    txtInterno.setCaretColor(brancoPuro);
                    txtInterno.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));                    
                    // Varre e limpa o botãozinho tridimensional do ícone do calendário
                    for (java.awt.Component comp : dc.getComponents()) {
                        if (comp instanceof javax.swing.JButton) {
                            javax.swing.JButton btnCal = (javax.swing.JButton) comp;
                            btnCal.setBackground(grafiteClaro);
                            btnCal.setBorderPainted(false);
                            btnCal.setFocusPainted(false);
                            btnCal.setContentAreaFilled(true);
                        }
                    }                   
                    // Força a atualização física imediata dos pixels no monitor
                    dc.revalidate();
                    dc.repaint();
                }
            }
        });
        this.setLocationRelativeTo(null);
//        this.setIconImage(new ImageIcon(getClass().getResource("/images/favicon.png")).getImage());
        this.setTitle(util.ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI LTDA");
        jComboRelatorios.setSelectedIndex(0);        
        java.awt.Color grafiteEscuroBarra = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaroCampos = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuroNumeros = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color cinzaLinhasBorda   = new java.awt.Color(60, 60, 60);
        com.toedter.calendar.JDateChooser[] calendariosLoja = { campoDateInicial, campoDateFinal };
        for (com.toedter.calendar.JDateChooser dc : calendariosLoja) {
            dc.setOpaque(false);
            dc.setBackground(grafiteEscuroBarra);
            java.awt.Component editorInterno = dc.getDateEditor().getUiComponent();           
            if (editorInterno instanceof javax.swing.JTextField) {
                final javax.swing.JTextField campoTextoData = (javax.swing.JTextField) editorInterno;               
                // 1. Configuração e pintura inicial do campo
                campoTextoData.setOpaque(true);
                campoTextoData.setBackground(grafiteClaroCampos);  
                campoTextoData.setForeground(douradoOuroNumeros);   
                campoTextoData.setDisabledTextColor(douradoOuroNumeros); // Blinda caso o campo mude para não-editável
                campoTextoData.setCaretColor(douradoOuroNumeros);   
                campoTextoData.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13)); 
                campoTextoData.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhasBorda, 1));              
                // 2. 🚀 O TRUQUE MESTRE: Um vigia em tempo real que intercepta e esmaga o reset da biblioteca!
                campoTextoData.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    @Override
                    public void propertyChange(java.beans.PropertyChangeEvent evt) {
                        // Se o JDateChooser tentar mudar a cor da letra para preto, nós cravamos em Dourado de volta
                        if ("foreground".equals(evt.getPropertyName())) {
                            if (!douradoOuroNumeros.equals(evt.getNewValue())) {
                                campoTextoData.setForeground(douradoOuroNumeros);
                            }
                        }
                        // Se tentar reverter o fundo para a cor antiga, forçamos o Grafite Claro
                        if ("background".equals(evt.getPropertyName())) {
                            if (!grafiteClaroCampos.equals(evt.getNewValue())) {
                                campoTextoData.setBackground(grafiteClaroCampos);
                            }
                        }
                    }
                });
            }
            // Limpa o quadradinho cinza do botão do ícone do calendário
            for (java.awt.Component comp : dc.getComponents()) {
                if (comp instanceof javax.swing.JButton) {
                    javax.swing.JButton btnCal = (javax.swing.JButton) comp;
                    btnCal.setBackground(grafiteClaroCampos);
                    btnCal.setBorderPainted(false);
                    btnCal.setFocusPainted(false);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jRadioButton3 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        painelCentral = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        buttonMenu = new javax.swing.JButton();
        buttonLimpar = new javax.swing.JButton();
        buttonIniciar = new javax.swing.JButton();
        labelCampoDescritivo = new javax.swing.JLabel();
        jComboRelatorios = new javax.swing.JComboBox<>();
        campoDateInicial = new com.toedter.calendar.JDateChooser();
        campoDateFinal = new com.toedter.calendar.JDateChooser();
        jSeparator5 = new javax.swing.JSeparator();

        jButton1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton1.setText("INICIAR");

        jButton3.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton3.setText("LIMPAR");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton2.setText("MENU");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextField2.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        jTextField2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jTextField1.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel5.setText("Data Inicial");

        jLabel6.setBackground(new java.awt.Color(204, 204, 255));
        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel6.setText("Data Final");

        jRadioButton6.setBackground(new java.awt.Color(204, 204, 255));
        jRadioButton6.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton6.setText("Outlet");
        jRadioButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton6ActionPerformed(evt);
            }
        });

        jRadioButton5.setBackground(new java.awt.Color(204, 204, 255));
        jRadioButton5.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton5.setText("Desapego");
        jRadioButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton5ActionPerformed(evt);
            }
        });

        jRadioButton4.setBackground(new java.awt.Color(204, 204, 255));
        jRadioButton4.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton4.setText("Todos");
        jRadioButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel4.setText("Fornecedores");

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 36)); // NOI18N
        jLabel1.setText("Relatórios");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel3.setText("Estoque");

        jRadioButton3.setBackground(new java.awt.Color(204, 204, 255));
        jRadioButton3.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton3.setText("Inventário");
        jRadioButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel2.setText("Financeiro");

        jRadioButton1.setBackground(new java.awt.Color(204, 204, 255));
        jRadioButton1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton1.setText("Faturamento");
        jRadioButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jRadioButton2.setBackground(new java.awt.Color(204, 204, 255));
        jRadioButton2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton2.setText("Vendas no período");
        jRadioButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jRadioButton7.setBackground(new java.awt.Color(204, 204, 255));
        jRadioButton7.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton7.setText("Lucro");
        jRadioButton7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jRadioButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton7ActionPerformed(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(204, 204, 255));

        jLabel7.setText("jLabel7");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(637, Short.MAX_VALUE)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(186, 186, 186))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addGap(204, 204, 204))
        );

        jRadioButtonMenuItem1.setSelected(true);
        jRadioButtonMenuItem1.setText("jRadioButtonMenuItem1");

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        jRadioButtonMenuItem2.setSelected(true);
        jRadioButtonMenuItem2.setText("jRadioButtonMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(204, 204, 255));

        painelCentral.setBackground(new java.awt.Color(204, 204, 255));
        painelCentral.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 30)); // NOI18N
        jLabel8.setText("  RELATÓRIOS");

        jLabel12.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel12.setText("DATA INICIAL");

        jLabel13.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("DATA FINAL");

        buttonMenu.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenu.setText("MENU");
        buttonMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuActionPerformed(evt);
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

        buttonIniciar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonIniciar.setText("INICIAR");
        buttonIniciar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonIniciarActionPerformed(evt);
            }
        });

        labelCampoDescritivo.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N

        jComboRelatorios.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        jComboRelatorios.setMaximumRowCount(29);
        jComboRelatorios.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-----      ESCOLHA UM RELATORIO    -----", "(FINANCEIRO) FATURAMENTO PROJETADO", "(FINANCEIRO) FATURAMENTO SINTETICO", "(FINANCEIRO) FATURAMENTO ANALITICO", "(FINANCEIRO) LUCRO ESTIMADO POR ITEM", "(FINANCEIRO) LUCRO ESTIMADO TOTAL", "(FINANCEIRO) VENDAS NO PERIODO", "(FINANCEIRO) VALOR PAGO", "(FINANCEIRO) DESPESAS NO PERIODO TOTAL", "(FINANCEIRO) RESUMO DESPESAS NO PERIODO", "(FINANCEIRO) GRAFICO FATURAMENTO 30 DIAS", "(FINANCEIRO) FATURAMENTO 12 MESES", "(FINANCEIRO) GRAFICO FATURAMENTO 12 MESES", "(FINANCEIRO) FRETES", "(ESTOQUE) INVENTARIO", "(ESTOQUE) INVENTARIO COMPLETO", "(ESTOQUE) QUANTIDADE", "(ESTOQUE) PECAS", "(ESTOQUE) DESCRIÇÃO", "(ESTOQUE) CODIGO", "(ESTOQUE) MARCA", "(ESTOQUE) TAMANHO", "(ESTOQUE) TIPO ITEM", "(FORNECEDORES) TODOS", "(FORNECEDORES) DESAPEGO", "(FORNECEDORES) OUTLET", " " }));
        jComboRelatorios.setBorder(null);

        campoDateInicial.setForeground(new java.awt.Color(255, 255, 255));
        campoDateInicial.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        campoDateInicial.setPreferredSize(new java.awt.Dimension(87, 29));

        campoDateFinal.setForeground(new java.awt.Color(255, 255, 255));
        campoDateFinal.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N

        javax.swing.GroupLayout painelCentralLayout = new javax.swing.GroupLayout(painelCentral);
        painelCentral.setLayout(painelCentralLayout);
        painelCentralLayout.setHorizontalGroup(
            painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelCentralLayout.createSequentialGroup()
                .addGap(161, 161, 161)
                .addGroup(painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelCentralLayout.createSequentialGroup()
                        .addGroup(painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator4)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(painelCentralLayout.createSequentialGroup()
                                .addGroup(painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(painelCentralLayout.createSequentialGroup()
                                        .addGroup(painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel12)
                                            .addComponent(campoDateInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel13)
                                            .addComponent(campoDateFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(71, 71, 71)
                                        .addComponent(labelCampoDescritivo, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 2, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelCentralLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jComboRelatorios, javax.swing.GroupLayout.PREFERRED_SIZE, 700, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(136, 136, 136))
                    .addGroup(painelCentralLayout.createSequentialGroup()
                        .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(134, 134, 134))))
            .addGroup(painelCentralLayout.createSequentialGroup()
                .addGap(381, 381, 381)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        painelCentralLayout.setVerticalGroup(
            painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCentralLayout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jComboRelatorios, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(216, 216, 216)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelCentralLayout.createSequentialGroup()
                        .addGroup(painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelCampoDescritivo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(campoDateFinal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelCentralLayout.createSequentialGroup()
                        .addComponent(campoDateInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(52, 52, 52)
                .addGroup(painelCentralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonMenu)
                    .addComponent(buttonLimpar)
                    .addComponent(buttonIniciar))
                .addGap(76, 76, 76))
        );

        labelCampoDescritivo.getAccessibleContext().setAccessibleParent(labelCampoDescritivo);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelCentral, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelCentral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed

    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton7ActionPerformed
             
    }//GEN-LAST:event_jRadioButton7ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
          
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
                
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
             
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton5ActionPerformed

    }//GEN-LAST:event_jRadioButton5ActionPerformed

    private void jRadioButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton6ActionPerformed

    }//GEN-LAST:event_jRadioButton6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

    }//GEN-LAST:event_jButton3ActionPerformed

    private void buttonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuActionPerformed
        new TelaMenu().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonMenuActionPerformed

    private void buttonLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimparActionPerformed
        labelCampoDescritivo.setText("");       
    }//GEN-LAST:event_buttonLimparActionPerformed

    private void buttonIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonIniciarActionPerformed
        nomeRelatorio = null;
//        ajusteData();
        if(!(jComboRelatorios.getSelectedItem().equals("-----      ESCOLHA UM RELATORIO    -----"))){           
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) FATURAMENTO PROJETADO")){ 
                campoDateInicial.setEnabled(true);
                campoDateFinal.setEnabled(true);               
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelFaturamentoProjetadoCloud(p);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }  
            }
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) FATURAMENTO SINTETICO")){
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");                   
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelFaturamentoSinteticoCloud(v);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }  
            }
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) FATURAMENTO ANALITICO")){               
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelFaturamentoAnaliticoCloud(v);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }  
            }
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) LUCRO ESTIMADO POR ITEM")){
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelVendaPeriodoPorItemCloud(p);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }  
            }
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) LUCRO ESTIMADO TOTAL")){               
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelLucroEstimadoTotalCloud(p);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }  
            }
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) VENDAS NO PERIODO")){               
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelVendaPeriodoTotalCloud(p);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }  
            }
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) VALOR PAGO")){               
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                } else {
                    ajusteData();                  
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelValorPagoCloud(p);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }  
            }
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) DESPESAS NO PERIODO TOTAL")){                 
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println("");
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelDespesasTotalCloud(v);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }  
            }
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) RESUMO DESPESAS NO PERIODO")){               
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println("");
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelDespesasResumoCloud(v);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("----------------------------");
                    }
                }  
            }           
            if(jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) FRETES")){                
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println("");
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelFretesCloud(v);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("----------------------------");
                    }
                }  
            }
            if (jComboRelatorios.getSelectedItem().equals("(ESTOQUE) INVENTARIO")) {              
                boolean dataInicialVazia = (dtInicial == null || dtInicial.trim().isEmpty() || dtInicial.contains("_"));
                boolean dataFinalVazia = (dtFinal == null || dtFinal.trim().isEmpty() || dtFinal.contains("_"));
                if (dataInicialVazia && dataFinalVazia) { 
                    campoDateInicial.setEnabled(false);
                    campoDateFinal.setEnabled(false);
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println("Iniciando geração: " + nomeRelatorio);           
                    try {
                        rdao.gerarRelInventarioCloud(p);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");                       
                    } catch (ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: " + ex);
                        System.out.println("-----------------------");
                    } catch (DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: " + ex);
                        System.out.println("-----------------------");
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: " + ex);
                        System.out.println("-----------------------");
                    } finally {
                        campoDateInicial.setEnabled(true);
                        campoDateFinal.setEnabled(true);
                    }
                } else {
                    if (MensagemSistema.mostrarDecisaoDark(this, "O Relatório de INVENTÁRIO não utiliza filtros de período.<br>Deseja limpar os campos de data e prosseguir?")) {
                        campoDateInicial.setDate(null);
                        campoDateFinal.setDate(null);
                        System.out.println("Campos limpos. Prosseguindo...");
                        try {
                        rdao.gerarRelInventarioCloud(p);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");                       
                        } catch (ClassNotFoundException | SQLException ex) {
                            Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                            System.out.println("Erro: " + ex);
                            System.out.println("-----------------------");
                        } catch (DocumentException ex) {
                            Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                            System.out.println("Erro: " + ex);
                            System.out.println("-----------------------");
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                            System.out.println("Erro: " + ex);
                            System.out.println("-----------------------");
                        } finally {
                            campoDateInicial.setEnabled(true);
                            campoDateFinal.setEnabled(true);
                        }
                    } else {
                        System.out.println("Operação cancelada pelo usuário.");
                    }
                }
            }
            if (jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) GRAFICO FATURAMENTO 30 DIAS")) {              
                boolean dataInicialVazia = (dtInicial == null || dtInicial.trim().isEmpty() || dtInicial.contains("_"));
                boolean dataFinalVazia = (dtFinal == null || dtFinal.trim().isEmpty() || dtFinal.contains("_"));
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                if (dataInicialVazia && dataFinalVazia) { 
                    campoDateInicial.setEnabled(false);
                    campoDateFinal.setEnabled(false);                    
                    System.out.println("Iniciando geração: " + nomeRelatorio);           
                    try {
                        rdao.gerarRelFaturamentoDiario30DiasCloud();
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");                       
                    } catch (ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: " + ex);
                        System.out.println("-----------------------");
                    } finally {
                        campoDateInicial.setEnabled(true);
                        campoDateFinal.setEnabled(true);
                    }
                } else {
                    if (MensagemSistema.mostrarDecisaoDark(this,"O Relatório de GRAFICO FATURAMENTO 30 DIAS não utiliza filtros de período.<br>Deseja limpar os campos de data e prosseguir?")) {
                        campoDateInicial.setDate(null);
                        campoDateFinal.setDate(null);
                        System.out.println("Campos limpos. Prosseguindo...");
                        try {
                            rdao.gerarRelFaturamentoDiario30DiasCloud();
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                            System.out.println("Erro: " + ex);
                            System.out.println("-----------------------");
                        } catch (SQLException ex) {
                            Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                            System.out.println("Erro: " + ex);
                            System.out.println("-----------------------");
                        }
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } else {
                        System.out.println("Operação cancelada pelo usuário.");
                    }
                }
            }
            if (jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) FATURAMENTO 12 MESES")) {              
                boolean dataInicialVazia = (dtInicial == null || dtInicial.trim().isEmpty() || dtInicial.contains("_"));
                boolean dataFinalVazia = (dtFinal == null || dtFinal.trim().isEmpty() || dtFinal.contains("_"));
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                if (dataInicialVazia && dataFinalVazia) { 
                    campoDateInicial.setEnabled(false);
                    campoDateFinal.setEnabled(false);
//                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println("Iniciando geração: " + nomeRelatorio);           
                    try {
                        rdao.gerarRelFaturamento12MesesCloud();
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");                       
                    } catch (ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: " + ex);
                        System.out.println("-----------------------");
                    } finally {
                        campoDateInicial.setEnabled(true);
                        campoDateFinal.setEnabled(true);
                    }
                } else {
                    if (MensagemSistema.mostrarDecisaoDark(this, "O Relatório de FATURAMENTO 12 MESES não utiliza filtros de período.<br>Deseja limpar os campos de data e prosseguir?")) {
                        campoDateInicial.setDate(null);
                        campoDateFinal.setDate(null);
                        System.out.println("Campos limpos. Prosseguindo...");
                        try {
                            rdao.gerarRelFaturamento12MesesCloud();
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                            System.out.println("Erro: " + ex);
                        } catch (SQLException ex) {
                            Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                            System.out.println("Erro: " + ex);
                        }
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
    
                    } else {
                        System.out.println("Operação cancelada pelo usuário.");
                    }
                }
            }
            
            if (jComboRelatorios.getSelectedItem().equals("(FINANCEIRO) GRAFICO FATURAMENTO 12 MESES")) {              
                boolean dataInicialVazia = (dtInicial == null || dtInicial.trim().isEmpty() || dtInicial.contains("_"));
                boolean dataFinalVazia = (dtFinal == null || dtFinal.trim().isEmpty() || dtFinal.contains("_"));
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                if (dataInicialVazia && dataFinalVazia) { 
                    campoDateInicial.setEnabled(false);
                    campoDateFinal.setEnabled(false);
                    System.out.println("Iniciando geração: " + nomeRelatorio);           
                    try {
                        rdao.gerarRelGraficoFaturamento12MesesCloud();
                    } catch (ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: " + ex);
                        System.out.println("-----------------------");
                    }
                    MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    campoDateInicial.setEnabled(true);
                    campoDateFinal.setEnabled(true);
                    
                } else {
                    if (MensagemSistema.mostrarDecisaoDark(this, "O Relatório de GRAFICO FATURAMENTO 12 MESES não utiliza filtros de período.<br>Deseja limpar os campos de data e prosseguir?")) {
                        campoDateInicial.setDate(null);
                        campoDateFinal.setDate(null);
                        System.out.println("Campos limpos. Prosseguindo...");
                        try {
                            rdao.gerarRelGraficoFaturamento12MesesCloud();
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                            System.out.println("Erro: " + ex);
                        } catch (SQLException ex) {
                            Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                            System.out.println("Erro: " + ex);
                        }
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
    
                    } else {
                        System.out.println("Operação cancelada pelo usuário.");
                    }
                }
            }
            
            if (jComboRelatorios.getSelectedItem().equals("(ESTOQUE) INVENTARIO COMPLETO")) {              
                boolean dataInicialVazia = (dtInicial == null || dtInicial.trim().isEmpty() || dtInicial.contains("_"));
                boolean dataFinalVazia = (dtFinal == null || dtFinal.trim().isEmpty() || dtFinal.contains("_"));
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                if (dataInicialVazia && dataFinalVazia) { 
                    campoDateInicial.setEnabled(false);
                    campoDateFinal.setEnabled(false);                    
                    System.out.println("Iniciando geração: " + nomeRelatorio);           
                    try {
                        rdao.gerarRelInventarioCompletoCloud();
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");                       
                    } catch (ClassNotFoundException | SQLException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: " + ex);
                        System.out.println("-----------------------");
                    } catch (DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: " + ex);
                        System.out.println("-----------------------");
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: " + ex);
                        System.out.println("-----------------------");
                    } finally {
                        campoDateInicial.setEnabled(true);
                        campoDateFinal.setEnabled(true);
                    }
                } else {
                    if (MensagemSistema.mostrarDecisaoDark(this, "O Relatório de (ESTOQUE) INVENTARIO COMPLETO não utiliza filtros de período.<br>Deseja limpar os campos de data e prosseguir?")) {
                        campoDateInicial.setDate(null);
                        campoDateFinal.setDate(null);
                        System.out.println("Campos limpos. Prosseguindo...");
    
                    } else {
                        System.out.println("Operação cancelada pelo usuário.");
                    }
                }
            }
                    
            if(jComboRelatorios.getSelectedItem().equals("(ESTOQUE) QUANTIDADE")){
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelPeriodoQuantCloud(p);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }
            }
            if(jComboRelatorios.getSelectedItem().equals("(FORNECEDORES) TODOS")){
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                System.out.println(nomeRelatorio);
                try {
                    rdao.gerarRelFornecedorTodosCloud(f);
                    MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                    Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                    MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                    System.out.println("Erro: "+ex);
                    System.out.println("-----------------------");
                }
                campoDateInicial.setEnabled(true);
                campoDateFinal.setEnabled(true);
            }
            if(jComboRelatorios.getSelectedItem().equals("(FORNECEDORES) DESAPEGO")){
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                System.out.println(nomeRelatorio);
                try {
                    rdao.gerarRelFornecedorDesapegoCloud(f);
                    MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                    Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                    MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                    System.out.println("Erro: "+ex);
                    System.out.println("-----------------------");
                }
                campoDateInicial.setEnabled(true);
                campoDateFinal.setEnabled(true);
            }
            if(jComboRelatorios.getSelectedItem().equals("(FORNECEDORES) OUTLET")){
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                System.out.println(nomeRelatorio);
                try {
                    rdao.gerarRelFornecedorOutletCloud(f);
                    MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                    Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                    MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                    System.out.println("Erro: "+ex);
                    System.out.println("-----------------------");
                }
                campoDateInicial.setEnabled(true);
                campoDateFinal.setEnabled(true);
            }
            if(jComboRelatorios.getSelectedItem().equals("(ESTOQUE) PECAS")){               
                if(campoDateInicial.getDate() == null && campoDateFinal.getDate() == null) {
                    MensagemSistema.mostrarAvisoDark(this, "Preencher Campos Data Inicial e Data Final!");
                }else{
                    ajusteData();
                    nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                    System.out.println(nomeRelatorio);
                    try {
                        rdao.gerarRelPeriodoPecasCloud(p);
                        MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                    } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                        Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                        MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                        System.out.println("Erro: "+ex);
                        System.out.println("-----------------------");
                    }
                }
            }
            if(jComboRelatorios.getSelectedItem().equals("(ESTOQUE) DESCRIÇÃO")){
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                System.out.println(nomeRelatorio);
                descricao = MensagemSistema.mostrarInputDark(this, "Entre com a DESCRIÇÃO do item");
                labelCampoDescritivo.setText(descricao);
                try {                  
                    rdao.gerarRelDescricaoCloud(p);
                    MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                    Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                    MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                    System.out.println("Erro: "+ex);
                    System.out.println("-----------------------");
                }
                campoDateInicial.setEnabled(true);
                campoDateFinal.setEnabled(true);
                labelCampoDescritivo.setText("");
            }
            if(jComboRelatorios.getSelectedItem().equals("(ESTOQUE) CODIGO")){
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                System.out.println(nomeRelatorio);
                try {
                    codigo = MensagemSistema.mostrarInputDark(this, "Entre com CÓDIGO do item");
                    labelCampoDescritivo.setText(codigo);
                    rdao.gerarRelCodigoCloud(p);
                    MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                    Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                    MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                    System.out.println("Erro: "+ex);
                    System.out.println("-----------------------");
                }
                campoDateInicial.setEnabled(true);
                campoDateFinal.setEnabled(true);
                labelCampoDescritivo.setText("");
            }
            if(jComboRelatorios.getSelectedItem().equals("(ESTOQUE) MARCA")){
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                System.out.println(nomeRelatorio);
                try {
                    marca = MensagemSistema.mostrarInputDark(this, "Entre com a MARCA do item:");
                    labelCampoDescritivo.setText(marca);
                    rdao.gerarRelMarcaCloud(p);
                    MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                    Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                    MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                    System.out.println("Erro: "+ex);
                    System.out.println("-----------------------");
                }
                campoDateInicial.setEnabled(true);
                campoDateFinal.setEnabled(true);
                labelCampoDescritivo.setText("");
            }
            if(jComboRelatorios.getSelectedItem().equals("(ESTOQUE) TAMANHO")){
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                System.out.println(nomeRelatorio);
                try {
                    tamanho = MensagemSistema.mostrarInputDark(this, "Entre com o TAMANHO do item:");
                    labelCampoDescritivo.setText(tamanho);
                    rdao.gerarRelTamanhoCloud(p);
                    MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                    Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                    MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                    System.out.println("Erro: "+ex);
                    System.out.println("-----------------------");
                }
                campoDateInicial.setEnabled(true);
                campoDateFinal.setEnabled(true);
                labelCampoDescritivo.setText("");
            }
            if(jComboRelatorios.getSelectedItem().equals("(ESTOQUE) TIPO ITEM")){
                nomeRelatorio = jComboRelatorios.getSelectedItem().toString();
                System.out.println(nomeRelatorio);
                try {
                    rdao.gerarRelTipoItemCloud(p);
                    MensagemSistema.mostrarAvisoDark(this, "Relatório "+nomeRelatorio+" criado com sucesso!");
                } catch (ClassNotFoundException | FileNotFoundException | SQLException | DocumentException ex) {
                    Logger.getLogger(TelaRelatorios.class.getName()).log(Level.SEVERE, null, ex);
                    MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
                    System.out.println("Erro: "+ex);
                    System.out.println("-----------------------");
                }               
            }
            campoDateInicial.setEnabled(true);
            campoDateFinal.setEnabled(true);
            jComboRelatorios.setSelectedIndex(0);
            campoDateInicial.setDate(null);
            campoDateFinal.setDate(null);
        }else{
            MensagemSistema.mostrarAvisoDark(this, "Escolher um tipo de Relatório");
        }
    }//GEN-LAST:event_buttonIniciarActionPerformed

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
                
                new TelaRelatorios().setVisible(true);
            }
        });
    }  
    /////// Metodos ///////         
    public void ajusteData(){
        String di = campoDateInicial.getDate().toString();       
        System.out.println(di);
        String dia = di.substring(8,10);
        System.out.println(dia);
        String mes = di.substring(4,7);
        if(mes.equals("Jan")){
            mes = "01";
        }
        if(mes.equals("Feb")){
            mes = "02";
        }
        if(mes.equals("Mar")){
            mes = "03";
        }
        if(mes.equals("Apr")){
            mes = "04";
        }
        if(mes.equals("May")){
            mes = "05";
        }
        if(mes.equals("Jun")){
            mes = "06";
        }
        if(mes.equals("Jul")){
            mes = "07";
        }
        if(mes.equals("Aug")){
            mes = "08";
        }
        if(mes.equals("Sep")){
            mes = "09";
        }
        if(mes.equals("Oct")){
            mes = "10";
        }
        if(mes.equals("Nov")){
            mes = "11";
        }
        if(mes.equals("Dec")){
            mes = "12";
        }
        System.out.println(mes);
        if(di.length() == 29 ){
            String ano = di.substring(24,29);
            System.out.println(ano);
            dtInicial = dia +"-"+ mes +"-"+ ano;
            dtIni = ano +"-"+ mes +"-"+ dia;
            System.out.println("Data Ajustada: "+dia +"-"+ mes +"-"+ ano); 
        }else{
           String ano = di.substring(24,28);
           System.out.println(ano);
           dtInicial = dia +"-"+ mes +"-"+ ano;
           dtIni = ano +"-"+ mes +"-"+ dia;
           System.out.println("Data Ajustada: "+dia +"-"+ mes +"-"+ ano); 
        }    
        String df = campoDateFinal.getDate().toString();
        System.out.println(df);
        String diaf = df.substring(8,10);
        String mesf = df.substring(4,7);
        if(mesf.equals("Jan")){
            mesf = "01";
        }
        if(mesf.equals("Feb")){
            mesf = "02";
        }
        if(mesf.equals("Mar")){
            mesf = "03";
        }
        if(mesf.equals("Apr")){
            mesf = "04";
        }
        if(mesf.equals("May")){
            mesf = "05";
        }
        if(mesf.equals("Jun")){
            mesf = "06";
        }
        if(mesf.equals("Jul")){
            mesf = "07";
        }
        if(mesf.equals("Aug")){
            mesf = "08";
        }
        if(mesf.equals("Sep")){
            mesf = "09";
        }
        if(mesf.equals("Oct")){
            mesf = "10";
        }
        if(mesf.equals("Nov")){
            mesf = "11";
        }
        if(mesf.equals("Dec")){
            mesf = "12";
        }
        if(df.length() == 29 ){
            String anof = df.substring(24,29);
            dtFinal = diaf +"-"+ mesf +"-"+ anof;
            System.out.println(diaf +"-"+ mesf +"-"+ anof);
            dtFim = anof +"-"+ mesf +"-"+ diaf;
        }else{
           String anof = df.substring(24,28);
           dtFinal = diaf +"-"+ mesf +"-"+ anof;
           System.out.println(diaf +"-"+ mesf +"-"+ anof);
           dtFim = anof +"-"+ mesf +"-"+ diaf;
        }       
    }
    
//        // 🔥 MÉTODO REUTILIZÁVEL POPUP DARK: Pode ser chamado por qualquer botão variando apenas a mensagem
//    private String chamarInputDarkItem(String mensagemPersonalizada) {
//        // Paleta Luxo/Moda Premium
//        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
//        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
//        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
//        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C
//
//        // Instanciação da mini janela flutuante sem barras do Windows
//        final javax.swing.JDialog popupDarkItem = new javax.swing.JDialog(this, true);
//        popupDarkItem.setUndecorated(true); 
//        popupDarkItem.setSize(280, 140);
//        
//        javax.swing.JPanel painelJanela = new javax.swing.JPanel(null);
//        painelJanela.setBackground(grafiteProfundo);
//        painelJanela.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
//
//        // Rótulo da Mensagem Superior (Recebe a variável com o texto do relatório)
//        javax.swing.JLabel lblMensagem = new javax.swing.JLabel(mensagemPersonalizada, javax.swing.SwingConstants.CENTER);
//        lblMensagem.setForeground(brancoPuro);
//        lblMensagem.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
//        lblMensagem.setBounds(15, 15, 250, 20);
//        painelJanela.add(lblMensagem);
//
//        // Campo de Texto Aberto para Digitação do Código
//        final javax.swing.JTextField txtCodigoInput = new javax.swing.JTextField();
//        txtCodigoInput.setBackground(grafiteClaro);
//        txtCodigoInput.setForeground(brancoPuro);
//        txtCodigoInput.setCaretColor(brancoPuro);
//        txtCodigoInput.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
//        txtCodigoInput.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
//        txtCodigoInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
//        txtCodigoInput.setBounds(35, 45, 210, 28);
//        painelJanela.add(txtCodigoInput);
//
//        // Botão OK
//        javax.swing.JButton btnOk = new javax.swing.JButton("OK");
//        btnOk.setBackground(grafiteClaro); btnOk.setForeground(brancoPuro);
//        btnOk.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
//        btnOk.setFocusPainted(false); btnOk.setBorderPainted(false);
//        btnOk.setBounds(35, 88, 100, 28);
//        btnOk.putClientProperty("JButton.buttonType", "square");
//        painelJanela.add(btnOk);
//
//        // Botão Cancelar
//        javax.swing.JButton btnCancelar = new javax.swing.JButton("Cancelar");
//        btnCancelar.setBackground(grafiteClaro); btnCancelar.setForeground(brancoPuro);
//        btnCancelar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
//        btnCancelar.setFocusPainted(false); btnCancelar.setBorderPainted(false);
//        btnCancelar.setBounds(145, 88, 100, 28);
//        btnCancelar.putClientProperty("JButton.buttonType", "square");
//        painelJanela.add(btnCancelar);
//
//        // Controle de Gatilhos (Compatível com Java antigo)
//        final boolean[] confirmouAcesso = {false};
//
//        java.awt.event.ActionListener acaoConfirmar = new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                confirmouAcesso[0] = true;
//                popupDarkItem.dispose();
//            }
//        };
//        btnOk.addActionListener(acaoConfirmar);
//        txtCodigoInput.addActionListener(acaoConfirmar);
//
//        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                confirmouAcesso[0] = false;
//                popupDarkItem.dispose();
//            }
//        });
//
//        popupDarkItem.getContentPane().add(painelJanela);
//        popupDarkItem.setLocationRelativeTo(this);
//
//        // Foco automático no teclado
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                txtCodigoInput.requestFocusInWindow();
//            }
//        });
//        
//        popupDarkItem.setVisible(true); // O código pausa aqui até fechar
//
//        // Retorna o texto digitado se clicou em OK, ou retorna vazio "" se cancelou
//        if (confirmouAcesso[0]) {
//            return txtCodigoInput.getText().trim();
//        }
//        return ""; 
//    }
//    
//        // 🔥 MÉTODO REUTILIZÁVEL PARA MENSAGENS INFORMATIVAS DARK COM VERSÃO CLÁSSICA DO JAVA
//    private void mostrarMensagemDark(String textoMensagem) {
//        // Paleta Luxo/Moda Premium
//        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
//        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
//        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
//        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C
//
//        // Instanciação do popup sem a barra branca superior do sistema operacional
//        final javax.swing.JDialog popupMensagem = new javax.swing.JDialog(this, true);
//        popupMensagem.setUndecorated(true); 
//        popupMensagem.setSize(340, 120); // Um pouco mais largo para acomodar frases contábeis maiores
//        
//        javax.swing.JPanel painelJanela = new javax.swing.JPanel(null);
//        painelJanela.setBackground(grafiteProfundo);
//        painelJanela.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
//
//        // Rótulo de Texto da Mensagem (Suporta quebra automática se a frase for longa)
//        javax.swing.JLabel lblMensagem = new javax.swing.JLabel("<html><center>" + textoMensagem + "</center></html>", javax.swing.SwingConstants.CENTER);
//        lblMensagem.setForeground(brancoPuro);
//        lblMensagem.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
//        lblMensagem.setBounds(15, 15, 310, 45);
//        painelJanela.add(lblMensagem);
//
//        // Botão Único de Confirmação OK
//        javax.swing.JButton btnOk = new javax.swing.JButton("OK");
//        btnOk.setBackground(grafiteClaro); 
//        btnOk.setForeground(brancoPuro);
//        btnOk.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
//        btnOk.setFocusPainted(false); 
//        btnOk.setBorderPainted(false);
//        btnOk.setBounds(120, 75, 100, 28); // Centralizado milimetricamente na horizontal
//        btnOk.putClientProperty("JButton.buttonType", "square");
//        painelJanela.add(btnOk);
//
//        // Gatilho de Fechamento por clique compatível com Java antigo
//        btnOk.addActionListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                popupMensagem.dispose(); // Fecha a janela e destrava a Thread do sistema
//            }
//        });
//
//        popupMensagem.getContentPane().add(painelJanela);
//        popupMensagem.setLocationRelativeTo(this); // Centraliza no meio exato da sua Tela de Relatórios
//
//        // Trava o foco diretamente no botão OK para o operador apenas dar ENTER para fechar
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                btnOk.requestFocusInWindow();
//            }
//        });
//        
//        popupMensagem.setVisible(true); // Abre de forma síncrona na tela
//    }
//    
//        // 🔥 MÉTODO REUTILIZÁVEL PARA POPUP DE DECISÃO (SIM / NÃO) 100% DARK
//    private boolean mostrarDecisaoDark(String textoMensagem) {
//        // Paleta Luxo/Moda Premium
//        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
//        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
//        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
//        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
//        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C
//
//        // Instanciação do JDialog sem a barra branca superior do Windows
//        final javax.swing.JDialog popupDecisao = new javax.swing.JDialog(this, true);
//        popupDecisao.setUndecorated(true); 
//        popupDecisao.setSize(380, 140); // Largura ideal para caber a frase inteira do aviso de filtro
//        
//        javax.swing.JPanel painelJanela = new javax.swing.JPanel(null);
//        painelJanela.setBackground(grafiteProfundo);
//        painelJanela.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
//
//        // Ícone de Interrogação sutil em Dourado (HTML)
//        javax.swing.JLabel lblIcone = new javax.swing.JLabel("<html><font color='#D4AF37' size='6'><b>?</b></font></html>", javax.swing.SwingConstants.CENTER);
//        lblIcone.setBounds(15, 20, 40, 45);
//        painelJanela.add(lblIcone);
//
//        // Texto da Mensagem (Suporta as duas linhas do aviso de filtro)
//        javax.swing.JLabel lblMensagem = new javax.swing.JLabel("<html>" + textoMensagem + "</html>");
//        lblMensagem.setForeground(brancoPuro);
//        lblMensagem.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
//        lblMensagem.setBounds(65, 15, 300, 50);
//        painelJanela.add(lblMensagem);
//
//        // Botão SIM
//        final javax.swing.JButton btnSim = new javax.swing.JButton("Sim");
//        btnSim.setBackground(grafiteClaro); 
//        btnSim.setForeground(brancoPuro);
//        btnSim.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
//        btnSim.setFocusPainted(false); 
//        btnSim.setBorderPainted(false);
//        btnSim.setBounds(85, 90, 100, 28);
//        btnSim.putClientProperty("JButton.buttonType", "square");
//        painelJanela.add(btnSim);
//
//        // Botão NÃO
//        javax.swing.JButton btnNao = new javax.swing.JButton("Não");
//        btnNao.setBackground(grafiteClaro); 
//        btnNao.setForeground(brancoPuro);
//        btnNao.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
//        btnNao.setFocusPainted(false); 
//        btnNao.setBorderPainted(false);
//        btnNao.setBounds(195, 90, 100, 28);
//        btnNao.putClientProperty("JButton.buttonType", "square");
//        painelJanela.add(btnNao);
//
//        // Vetor de controle para o escopo anônimo (Compatível com Java antigo)
//        final boolean[] respostaSim = {false};
//
//        // Ação do Botão SIM
//        btnSim.addActionListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                respostaSim[0] = true;
//                popupDecisao.dispose();
//            }
//        });
//
//        // Ação do Botão NÃO
//        btnNao.addActionListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                respostaSim[0] = false;
//                popupDecisao.dispose();
//            }
//        });
//
//        popupDecisao.getContentPane().add(painelJanela);
//        popupDecisao.setLocationRelativeTo(this); // Centraliza no meio exato da tela de relatórios
//
//        // Força o foco inicial no botão SIM para manter a velocidade do teclado (Enter aceita)
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                btnSim.requestFocusInWindow();
//            }
//        });
//        
//        popupDecisao.setVisible(true); // Bloqueia a execução até o operador escolher
//
//        return respostaSim[0]; // Retorna true se escolheu "Sim", e false se escolheu "Não"
//    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonIniciar;
    private javax.swing.JButton buttonLimpar;
    private javax.swing.JButton buttonMenu;
    private com.toedter.calendar.JDateChooser campoDateFinal;
    private com.toedter.calendar.JDateChooser campoDateInicial;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JComboBox<String> jComboRelatorios;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JLabel labelCampoDescritivo;
    private javax.swing.JPanel painelCentral;
    // End of variables declaration//GEN-END:variables
}
