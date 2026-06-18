package views;

import dao.ConfigDAO;
import dao.ProdutoDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Config;
import util.BackupSistema;
import util.MensagemSistema;
import util.RegistraLogs;

public class TelaMenu extends javax.swing.JFrame {
        
    public String nomeDoCliente;
    Config c = new Config();
    ConfigDAO cdao = new ConfigDAO();
    ProdutoDAO pdao = new ProdutoDAO();
    TelaBarraProgresso tbp = new TelaBarraProgresso();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    TelaDashboard td = new TelaDashboard();
    RegistraLogs rl = new RegistraLogs();
    BackupSistema bs = new BackupSistema();
    int anoAtual = java.time.Year.now().getValue();
    String senhaRelatorio = "Admin123";

    
    @SuppressWarnings("LeakingThisInConstructor")
    public TelaMenu() {
        initComponents();
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
        // --- 1. DEFINIÇÃO DA PALETA CORPORATIVA UNIFICADA ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255);
        java.awt.Color vermelhoSair    = new java.awt.Color(120, 35, 35);
        // --- 2. APLICAÇÃO NO ACABAMENTO DA TELA ---      
        jPanel1.setBackground(grafiteProfundo);
        jPanel1.setOpaque(true); // Garante que o Java renderize a cor opaca        
        // Fontes e Cores dos Títulos (Substitua pelos nomes reais das suas Labels)
        jLabel1.setForeground(brancoPuro);
        jLabel1.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 36)); // Mantém a imponência da logo       
        jLabel3.setForeground(douradoOuro);
        jLabel3.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));       
        jLabel2.setForeground(brancoPuro);
        // --- 3. VETOR DE BOTÕES: MODIFICA TODOS DE UMA SÓ VEZ ---
        // Adicione dentro do array o nome da variável de todos os seus botões do menu!
        javax.swing.JButton[] botoesMenu = {
            buttonTirarFotos, buttonEstoque, buttonFornecedores, buttonFinanceiro, 
            buttonClientes, buttonSacola, buttonUsuarios, buttonEntregas, buttonRelatorios, buttonTrocas
        };
        for (javax.swing.JButton btn : botoesMenu) {
            btn.setBackground(grafiteClaro);
            btn.setForeground(brancoPuro);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            // 🚀 AS DUAS LINHAS DE TRAVA QUE MATAM A BORDA BRANCA:
            btn.setFocusPainted(false);       // Remove o contorno branco de clique
            btn.setBorderPainted(false);      // Desativa a borda nativa tridimensional do Swing
            btn.setFocusPainted(false); // Remove aquela borda quadrada feia de clique
            btn.putClientProperty("JButton.buttonType", "square"); // Força o FlatLaf a deixá-lo plano
        }
        // Estilização isolada do Botão Sair
        buttonSair.setBackground(vermelhoSair);
        buttonSair.setForeground(brancoPuro);
        buttonSair.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        buttonSair.putClientProperty("JButton.buttonType", "square");       
                // --- CONFIGURAÇÃO DA BARRA DE TÍTULO PREMIUM NO MENU PRINCIPAL ---
        java.awt.Color grafiteClaroBarra = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuroBarra  = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuroBarra   = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color vermelhoTopSair      = new java.awt.Color(160, 40, 40);   // Hover vermelho para o X
        // 1. Cria o container da barra física
        javax.swing.JPanel barraTituloPremium = new javax.swing.JPanel();
        barraTituloPremium.setBackground(grafiteClaroBarra);
        barraTituloPremium.setOpaque(true);
        barraTituloPremium.setLayout(new java.awt.BorderLayout(15, 0));
        barraTituloPremium.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0));        
        // 2. Lado Esquerdo: Nome do Cliente e do Sistema
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Menu de Gestão");
        lblClienteBarra.setForeground(brancoPuroBarra);
        lblClienteBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        barraTituloPremium.add(lblClienteBarra, java.awt.BorderLayout.WEST);      
        // 3. Lado Direito: Bloco Agrupador (Assinatura + Botão X)
        javax.swing.JPanel painelDireitoBarra = new javax.swing.JPanel();
        painelDireitoBarra.setBackground(grafiteClaroBarra);
        painelDireitoBarra.setOpaque(true);
        painelDireitoBarra.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 0));       
        // Texto de Desenvolvimento de Grife [links: 10]
        javax.swing.JLabel lblDevBarra = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDevBarra.setForeground(douradoOuroBarra); 
        lblDevBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        painelDireitoBarra.add(lblDevBarra);       
        // Botão X: Plano e integrado para fechar todo o sistema
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
                btnFecharJanela.setBackground(vermelhoTopSair);
                btnFecharJanela.setForeground(brancoPuroBarra);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnFecharJanela.setBackground(grafiteClaroBarra);
                btnFecharJanela.setForeground(brancoPuroBarra);
            }
        });       
        // Ação de fechamento total e seguro da JVM
        btnFecharJanela.addActionListener(e -> {
            System.exit(0);
        });       
        painelDireitoBarra.add(btnFecharJanela);
        barraTituloPremium.add(painelDireitoBarra, java.awt.BorderLayout.EAST);       
        // 4. Posiciona e estica a barra no topo exato do Menu
        barraTituloPremium.setBounds(0, 0, this.getWidth(), 30);
        // 5. Motor de Movimentação utilizando a referência de janelaAtual
        final int[] coordX = {0};
        final int[] coordY = {0};
        final javax.swing.JFrame janelaAtual = this; // Aponta diretamente para o JFrame do Menu       
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
        this.getContentPane().setBackground(grafiteProfundo);
        this.setTitle(util.ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI LTDA");
        
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Iniciando ciclo automático de sincronização...");

            // Instancia o monitor de latência da rede Cloud
            java.util.Timer monitorAtraso = new java.util.Timer();

            try {
                // Abre a barra de progresso na Thread visual correta do Swing
                java.awt.EventQueue.invokeLater(() -> {
                    tbp.ativarAlertaAtraso(false); // Reseta a cor para o padrão laranja
                    tbp.setVisible(true);
                    tbp.setLocationRelativeTo(null);
                });

                // Agenda o alerta de lentidão amarelo para daqui a 60 segundos
                monitorAtraso.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        tbp.ativarAlertaAtraso(true); // Muda a barra para amarelo
                    }
                }, 60000);

                // EXECUTA A SINCRONIZAÇÃO EM SEGUNDO PLANO
                tbp.sincronizarComBarra(tbp.getJProgressBar());

                // ====================================================================================
                // INJEÇÃO DA AUTOMAÇÃO COMERCIAL: Monitora o Mercado Pago e atualiza estoque na Aiven
                // ====================================================================================
                try {
                    dao.ProdutoDAO produtoDao = new dao.ProdutoDAO();
                    produtoDao.processarVendasMercadoPago();
                } catch (ClassNotFoundException e) {
                    System.err.println("Erro ao rodar processamento automatizado Mercado Pago: " + e.getMessage());
                }
                // ====================================================================================

                // ATUALIZA O DASHBOARD COM OS NOVOS DADOS LOCAIS RECENTES (Já com as baixas online inclusas)
                td.atualizarDashboard();

                // Sucesso: Cancela o alerta de atraso antes que ele mude a barra para amarelo
                monitorAtraso.cancel();

                // Fecha a janela da barra de progresso suavemente
                java.awt.EventQueue.invokeLater(() -> tbp.setVisible(false));
                System.out.println("Ciclo de sincronização concluído com sucesso.");

            } catch (Exception ex) {
                System.err.println("Mensagem de Alerta de Sincronia: " + ex.getMessage());

                // Grava a falha física no seu arquivo .log externo
                rl.registrarLog("Erro crítico no ciclo automático de sincronia: " + ex.getMessage());

                // Contingência: Cancela o timer e oculta a barra para não travar a tela do operador
                monitorAtraso.cancel();
                java.awt.EventQueue.invokeLater(() -> tbp.setVisible(false));
            }
        }, 300, 3600, TimeUnit.SECONDS);

        // --- 🤖 ATALHO GLOBAL F1 PARA CHAMAR O ASSISTENTE DE AJUDA IA ---
        this.getRootPane().registerKeyboardAction(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Instancia e exibe a sua tela de ajuda customizada
                views.TelaAjudaIA telaAjuda = new views.TelaAjudaIA();
                telaAjuda.setVisible(true);
                telaAjuda.setLocationRelativeTo(null); // Centraliza no meio do monitor
            }
        }, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator3 = new javax.swing.JSeparator();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        buttonFornecedores = new javax.swing.JButton();
        buttonEstoque = new javax.swing.JButton();
        buttonRelatorios = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        buttonSair = new javax.swing.JButton();
        buttonClientes = new javax.swing.JButton();
        buttonFinanceiro = new javax.swing.JButton();
        buttonUsuarios = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        buttonTrocas = new javax.swing.JButton();
        buttonSacola = new javax.swing.JButton();
        buttonTirarFotos = new javax.swing.JButton();
        buttonEntregas = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setUndecorated(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(245, 247, 250));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setFont(new java.awt.Font("Times New Roman", 0, 11)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 50)); // NOI18N
        jLabel1.setText("PORTOBELLA");

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel2.setText("- Sistema de Gestão -");

        buttonFornecedores.setBackground(new java.awt.Color(49, 130, 206));
        buttonFornecedores.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonFornecedores.setForeground(new java.awt.Color(255, 255, 255));
        buttonFornecedores.setText("FORNECEDORES");
        buttonFornecedores.setBorder(null);
        buttonFornecedores.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonFornecedores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFornecedoresActionPerformed(evt);
            }
        });

        buttonEstoque.setBackground(new java.awt.Color(49, 130, 206));
        buttonEstoque.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonEstoque.setForeground(new java.awt.Color(255, 255, 255));
        buttonEstoque.setText("ESTOQUE");
        buttonEstoque.setBorder(null);
        buttonEstoque.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonEstoque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEstoqueActionPerformed(evt);
            }
        });

        buttonRelatorios.setBackground(new java.awt.Color(49, 130, 206));
        buttonRelatorios.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonRelatorios.setForeground(new java.awt.Color(255, 255, 255));
        buttonRelatorios.setText("RELATÓRIOS");
        buttonRelatorios.setBorder(null);
        buttonRelatorios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonRelatorios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRelatoriosActionPerformed(evt);
            }
        });

        buttonSair.setBackground(new java.awt.Color(49, 130, 206));
        buttonSair.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonSair.setForeground(new java.awt.Color(255, 255, 255));
        buttonSair.setText("SAIR");
        buttonSair.setBorder(null);
        buttonSair.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSairActionPerformed(evt);
            }
        });

        buttonClientes.setBackground(new java.awt.Color(49, 130, 206));
        buttonClientes.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonClientes.setForeground(new java.awt.Color(255, 255, 255));
        buttonClientes.setText("CLIENTES");
        buttonClientes.setBorder(null);
        buttonClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClientesActionPerformed(evt);
            }
        });

        buttonFinanceiro.setBackground(new java.awt.Color(49, 130, 206));
        buttonFinanceiro.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonFinanceiro.setForeground(new java.awt.Color(255, 255, 255));
        buttonFinanceiro.setText("FINANCEIRO");
        buttonFinanceiro.setBorder(null);
        buttonFinanceiro.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonFinanceiro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFinanceiroActionPerformed(evt);
            }
        });

        buttonUsuarios.setBackground(new java.awt.Color(49, 130, 206));
        buttonUsuarios.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonUsuarios.setForeground(new java.awt.Color(255, 255, 255));
        buttonUsuarios.setText("USUÁRIOS");
        buttonUsuarios.setBorder(null);
        buttonUsuarios.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonUsuariosActionPerformed(evt);
            }
        });

        buttonTrocas.setBackground(new java.awt.Color(49, 130, 206));
        buttonTrocas.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonTrocas.setForeground(new java.awt.Color(255, 255, 255));
        buttonTrocas.setText("TROCAS");
        buttonTrocas.setBorder(null);
        buttonTrocas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonTrocas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTrocasActionPerformed(evt);
            }
        });

        buttonSacola.setBackground(new java.awt.Color(49, 130, 206));
        buttonSacola.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonSacola.setForeground(new java.awt.Color(255, 255, 255));
        buttonSacola.setText("SACOLAS");
        buttonSacola.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonSacola.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSacolaActionPerformed(evt);
            }
        });

        buttonTirarFotos.setBackground(new java.awt.Color(49, 130, 206));
        buttonTirarFotos.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonTirarFotos.setForeground(new java.awt.Color(255, 255, 255));
        buttonTirarFotos.setText("TIRAR FOTOS");
        buttonTirarFotos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonTirarFotos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTirarFotosActionPerformed(evt);
            }
        });

        buttonEntregas.setBackground(new java.awt.Color(49, 130, 206));
        buttonEntregas.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        buttonEntregas.setForeground(new java.awt.Color(255, 255, 255));
        buttonEntregas.setText("ENTREGAS");
        buttonEntregas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonEntregas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEntregasActionPerformed(evt);
            }
        });

        jLabel3.setText("BRECHÓ & OUTLET");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(154, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSeparator1)
                            .addComponent(jSeparator2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(buttonRelatorios, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(buttonUsuarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(buttonClientes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(buttonFornecedores, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                                        .addComponent(buttonTirarFotos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 198, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(buttonFinanceiro, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(buttonEstoque, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(buttonSacola, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(buttonEntregas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(buttonTrocas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(9, 9, 9))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(243, 243, 243)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 508, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(179, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(228, 228, 228))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(422, 422, 422))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 444, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(205, 205, 205))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(371, 371, 371)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(279, 279, 279)
                        .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(392, 392, 392)
                        .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(420, 420, 420)
                        .addComponent(buttonSair, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonTirarFotos, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonEstoque, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonFinanceiro, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSacola, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonEntregas, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonRelatorios, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonTrocas, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator5, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                .addGap(26, 26, 26)
                .addComponent(buttonSair, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonEntregasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEntregasActionPerformed
        new TelaEntregas().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonEntregasActionPerformed

    private void buttonTirarFotosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTirarFotosActionPerformed
        new Foto().setVisible(true);
        this.setLocationRelativeTo(null);
    }//GEN-LAST:event_buttonTirarFotosActionPerformed

    private void buttonSacolaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSacolaActionPerformed
        try {
            new TelaSacola().setVisible(true);
        } catch (ClassNotFoundException | InterruptedException ex) {
            Logger.getLogger(TelaMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonSacolaActionPerformed

    private void buttonTrocasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTrocasActionPerformed
        new TelaTrocas().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonTrocasActionPerformed

    private void buttonUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonUsuariosActionPerformed
        new TelaUsuario().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonUsuariosActionPerformed

    private void buttonFinanceiroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFinanceiroActionPerformed
        new TelaFinanceiro().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonFinanceiroActionPerformed

    private void buttonClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClientesActionPerformed
        new TelaCliente().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonClientesActionPerformed

    private void buttonSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSairActionPerformed
        // 1. Alerta de confirmação para o operador do caixa
        boolean resposta = MensagemSistema.mostrarDecisaoDark(this, 
                "Deseja realmente sair do sistema?");

        if (resposta == true) {
            System.out.println("Encerrando o sistema...");
            System.out.println("----------------------------------------");
            
//            bs.executarBackupGeralCloud();
            try {
                // 2. Para o relógio da sincronização em segundo plano imediatamente
                scheduler.shutdownNow(); 
                System.out.println("Agendador automático finalizado.");
            } catch (Exception e) {
                System.err.println("Erro ao parar agendador: " + e.getMessage());
            }

            // 3. Fecha todas as conexões e encerra o processo do Java de vez
            System.exit(0); 
        }
    }//GEN-LAST:event_buttonSairActionPerformed

    private void buttonRelatoriosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRelatoriosActionPerformed
//               // --- 1. DEFINIÇÃO DA PALETA LUXO/MODA PREMIUM (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C

        // --- 2. INSTANCIAÇÃO DA MINI JANELA RESTRITA (JDialog) ---
        javax.swing.JDialog popupDark = new javax.swing.JDialog(this, true);
        popupDark.setUndecorated(true); // 🔥 O SEGREDO: Arranca a barra branca do Windows com o X de vez!
        popupDark.setSize(280, 140);
        
        // --- 3. CONSTRUÇÃO VISUAL DO CONTEÚNER INTERNO ---
        javax.swing.JPanel painelJanela = new javax.swing.JPanel(null);
        painelJanela.setBackground(grafiteProfundo);
        painelJanela.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); // Borda fina elegante

        // Rótulo da Mensagem Superior
        javax.swing.JLabel lblMensagem = new javax.swing.JLabel("ENTRE COM A SENHA!", javax.swing.SwingConstants.CENTER);
        lblMensagem.setForeground(brancoPuro);
        lblMensagem.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblMensagem.setBounds(15, 15, 250, 20);
        painelJanela.add(lblMensagem);

        // Campo Oculto para Digitação da Senha
        javax.swing.JPasswordField txtSenhaInput = new javax.swing.JPasswordField();
        txtSenhaInput.setBackground(grafiteClaro);
        txtSenhaInput.setForeground(brancoPuro);
        txtSenhaInput.setCaretColor(brancoPuro);
        txtSenhaInput.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
        txtSenhaInput.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        txtSenhaInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtSenhaInput.setBounds(35, 45, 210, 28);
        painelJanela.add(txtSenhaInput);

        // Botão de Confirmação OK
        javax.swing.JButton btnOk = new javax.swing.JButton("OK");
        btnOk.setBackground(grafiteClaro);
        btnOk.setForeground(brancoPuro);
        btnOk.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnOk.setFocusPainted(false);
        btnOk.setBorderPainted(false);
        btnOk.setBounds(35, 88, 100, 28);
        btnOk.putClientProperty("JButton.buttonType", "square");
        painelJanela.add(btnOk);

        // Botão de Cancelamento / Escape
        javax.swing.JButton btnCancelar = new javax.swing.JButton("Cancelar");
        btnCancelar.setBackground(grafiteClaro);
        btnCancelar.setForeground(brancoPuro);
        btnCancelar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setBounds(145, 88, 100, 28);
        btnCancelar.putClientProperty("JButton.buttonType", "square");
        painelJanela.add(btnCancelar);

        // --- 4. ENGINE DE GATILHOS DA ROTINA CONTÁBIL ---
        // Cria uma referência interna transitória para extrair o resultado com segurança
                final boolean[] confirmouAcesso = {false};

        // // Ação do Botão OK
        java.awt.event.ActionListener acaoConfirmar = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmouAcesso[0] = true;
                popupDark.dispose(); // Fecha o mini bloco liberando a Thread de execução
            }
        };
        btnOk.addActionListener(acaoConfirmar);
        
        // // Permite disparar o OK batendo direto na tecla ENTER dentro do campo de texto
        txtSenhaInput.addActionListener(acaoConfirmar);

        // // Ação do Botão Cancelar
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmouAcesso[0] = false;
                popupDark.dispose();
            }
        });


        // Adiciona a malha ao frame do popup, centraliza e foca as coordenadas
        popupDark.getContentPane().add(painelJanela);
        popupDark.setLocationRelativeTo(this); // Centraliza exatamente no meio do Menu Principal!

        // Força a abertura síncrona jogando o foco do teclado direto no input
        java.awt.EventQueue.invokeLater(() -> txtSenhaInput.requestFocusInWindow());
        popupDark.setVisible(true); // O código pausa aqui até que o popup feche

        // --- 5. VALIDAÇÃO RETILÍNEA APÓS O FECHAMENTO ---
        if (confirmouAcesso[0]) {
            String senhaDigitada = new String(txtSenhaInput.getPassword());
            
            // 🚀 SUA VALIDAÇÃO ORIGINAL DO SISTEMA DE GESTÃO PROSSEGUE AQUI:
            if (senhaDigitada.equals("Admin123")) {
                System.out.println("Acesso validado com sucesso! Abrindo módulo restrito...");
                new TelaRelatorios().setVisible(true);
                this.setLocationRelativeTo(null);
                dispose();
            } else {
                MensagemSistema.mostrarAvisoDark(this, "Senha Incorreta!");
            }
        }
    }//GEN-LAST:event_buttonRelatoriosActionPerformed

    private void buttonEstoqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEstoqueActionPerformed
        new TelaEstoque().setVisible(true);
        this.setLocationRelativeTo(null);
        try {
            util.PagamentoServer.iniciar();
            pdao.gerarSiteEstoque();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaMenu.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(TelaMenu.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(TelaMenu.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TelaMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
        dispose();
    }//GEN-LAST:event_buttonEstoqueActionPerformed

    private void buttonFornecedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFornecedoresActionPerformed
        new TelaFornecedor().setVisible(true);
        this.setLocationRelativeTo(null);
        dispose();
    }//GEN-LAST:event_buttonFornecedoresActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // Antes de fechar a janela e encerrar o Java, garante a cópia de segurança na pasta
        try {
            util.BackupSistema backup = new util.BackupSistema();
            backup.executarBackupGeralCloud();
        } catch (Exception e) {
            System.err.println("Falha no encerramento de segurança: " + e.getMessage());
        }
    }//GEN-LAST:event_formWindowClosing

    public void carregaNomeCliente(){
        try {
            cdao.lerNomeCliente(c);
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Erro: "+ex.getMessage());
        }
    }
    
     public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            // Ativa o tema claro moderno (Flat Light). Para modo escuro, use 'FlatDarkLaf'
            com.formdev.flatlaf.FlatLightLaf.setup();
            
            javax.swing.UIManager.put("Button.arc", 8);
            javax.swing.UIManager.put("Component.arc", 8);
            javax.swing.UIManager.put("TextComponent.arc", 8);
            
            javax.swing.UIManager.put("Table.rowHeight", 26);
            javax.swing.UIManager.put("Table.showHorizontalLines", true);
            javax.swing.UIManager.put("Table.showVerticalLines", true);
        } catch( Exception ex ) {
            System.err.println("Falha ao inicializar tema moderno FlatLaf");
            System.err.println("Erro: "+ex);
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaMenu().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonClientes;
    private javax.swing.JButton buttonEntregas;
    private javax.swing.JButton buttonEstoque;
    private javax.swing.JButton buttonFinanceiro;
    private javax.swing.JButton buttonFornecedores;
    private javax.swing.JButton buttonRelatorios;
    private javax.swing.JButton buttonSacola;
    private javax.swing.JButton buttonSair;
    private javax.swing.JButton buttonTirarFotos;
    private javax.swing.JButton buttonTrocas;
    private javax.swing.JButton buttonUsuarios;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    // End of variables declaration//GEN-END:variables

}
