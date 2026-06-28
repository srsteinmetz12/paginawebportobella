package views;

import dao.UsuarioDAO;
import geradorlicencacliente.LicencaManager;
import geradorlicencacliente.LicencaVO;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Usuario;
import paginaweb.NotificacaoVendasService;
import util.ConfigLoader;
import util.MensagemSistema;

public class TelaAcessoLogin extends javax.swing.JFrame {
    
    public static String usuario;
    public static String senha;
    public static String user;
    public static String password;
    UsuarioDAO udao = new UsuarioDAO();
    Usuario u = new Usuario();
    TelaMenu telaMenu = new TelaMenu();

    @SuppressWarnings("LeakingThisInConstructor")
    public TelaAcessoLogin() {
        initComponents();
        
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
        
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color pretoPuro       = new java.awt.Color(0, 0, 0);       // #000000
        java.awt.Color cinzaClaro      = new java.awt.Color(204, 204, 204); // #CCCCCC
        java.awt.Color cinzaDiscreto   = new java.awt.Color(102, 102, 102); // #666666
        java.awt.Color vermelhoSair      = new java.awt.Color(160, 40, 40);
        
        this.getContentPane().setBackground(grafiteProfundo);
        
        jLabel1.setForeground(douradoOuro);
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        jPanel1.setBackground(grafiteProfundo);
        jPanel1.setOpaque(true);
        
        campoUsuario.setBackground(grafiteClaro);
        campoUsuario.setForeground(brancoPuro);
        campoUsuario.setCaretColor(brancoPuro); // Cursor piscando em branco
        campoUsuario.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        
        campoSenha.setBackground(grafiteClaro);
        campoSenha.setForeground(brancoPuro);
        campoSenha.setCaretColor(brancoPuro);
        campoSenha.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        
        buttonLogar.setBackground(douradoOuro);
        buttonLogar.setForeground(pretoPuro);
        buttonLogar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        
        buttonLimpar.setBackground(grafiteClaro);
        buttonLimpar.setForeground(cinzaClaro);
        buttonLimpar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        
//        labelCopyright.setForeground(cinzaDiscreto);
//        labelCopyright.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        
        int anoAtual = java.time.Year.now().getValue();
        labelCopyright.setText("Copyright © 2022-" + anoAtual + " SRS Consultoria TI LTDA");
        labelCopyright.setForeground(douradoOuro); // Usa a mesma variável de cor do título!
        labelCopyright.setBackground(grafiteProfundo);
        labelCopyright.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 10));
        labelCopyright.setAlignment(java.awt.Label.CENTER);
        
        this.setLocationRelativeTo(null);
        
        this.setTitle(ConfigLoader.get("sistema.nome_cliente"));    
        this.getRootPane().setDefaultButton(buttonLogar);
        campoUsuario.requestFocus();
        // Variáveis globais para guardar a coordenada do clique do mouse (Declare-as no topo da classe se necessário)
        final int[] pX = {0}; final int[] pY = {0};
        
        // Altere 'seuNovoPanelDoTopo' para o nome do painel criado no Passo 2
        jLabelBarraSuperior.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pX[0] = evt.getX(); pY[0] = evt.getY();
            }
        });
        jLabelBarraSuperior.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                setLocation(evt.getXOnScreen() - pX[0], evt.getYOnScreen() - pY[0]);
            }
        });
    
            // --- CONFIGURAÇÃO EXPLICITA DE CORES NATIVAS DA BARRA ---
        java.awt.Color grafiteClaroBarra = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuroBarra  = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuroBarra   = new java.awt.Color(255, 255, 255); // #FFFFFF

        // 1. Cria o container da barra física
        javax.swing.JPanel barraTituloPremium = new javax.swing.JPanel();
        barraTituloPremium.setBackground(grafiteClaroBarra);
        barraTituloPremium.setOpaque(true);
        barraTituloPremium.setLayout(new java.awt.BorderLayout(15, 0));
        barraTituloPremium.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 15, 0, 0)); // Ajustado padding direito para o X colar na borda
        
        // 2. Lado Esquerdo: Nome do Cliente e do Sistema
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Acesso ao Sistema");
        lblClienteBarra.setForeground(brancoPuroBarra);
        lblClienteBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        barraTituloPremium.add(lblClienteBarra, java.awt.BorderLayout.WEST);
        
        // 3. 🔥 Lado Direito: Bloco Agrupador (Assinatura + Botão X)
        javax.swing.JPanel painelDireitoBarra = new javax.swing.JPanel();
        painelDireitoBarra.setBackground(grafiteClaroBarra);
        painelDireitoBarra.setOpaque(true);
        painelDireitoBarra.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 0));
        
        // Texto de Desenvolvimento
        javax.swing.JLabel lblDevBarra = new javax.swing.JLabel("Desenvolvido por: SRS Consultoria TI");
        lblDevBarra.setForeground(douradoOuroBarra); 
        lblDevBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        painelDireitoBarra.add(lblDevBarra);
        
        // 🔥 BOTÃO X DEFINITIVO: Plano, minimalista e integrado
        javax.swing.JButton btnFecharJanela = new javax.swing.JButton(" X ");
        btnFecharJanela.setBackground(grafiteClaroBarra);
        btnFecharJanela.setForeground(brancoPuroBarra);
        btnFecharJanela.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnFecharJanela.setFocusPainted(false);
        btnFecharJanela.setBorderPainted(false);
        btnFecharJanela.setPreferredSize(new java.awt.Dimension(45, 30)); // Faz o X ocupar toda a altura da barra
        btnFecharJanela.putClientProperty("JButton.buttonType", "square"); // Visual plano FlatLaf
        
        // Efeito Visual: Muda para vermelho quando o mouse passa por cima (Hover)
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
        
        // 🚀 COMANDO DE FECHAMENTO CONTÁBIL: Encerra toda a JVM com segurança
        btnFecharJanela.addActionListener(e -> {
            NotificacaoVendasService.parar();
            System.exit(0);
        });
        
        painelDireitoBarra.add(btnFecharJanela);
        barraTituloPremium.add(painelDireitoBarra, java.awt.BorderLayout.EAST);
        
        // 4. Posiciona e estica a barra no topo exato da tela atual dinamicamente [links: 1, 3]
        barraTituloPremium.setBounds(0, 0, this.getWidth(), 30);

        // 5. MOTOR DE MOVIMENTAÇÃO BLINDADO: Uso de arrays finais para não bugar a Thread
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

        // 6. INSERÇÃO SEGURA: Força a barra a aparecer no topo da tela
        try {
            jPanel1.add(barraTituloPremium);
            jPanel1.revalidate();
            jPanel1.repaint();
        } catch(Exception e) {
            this.getContentPane().add(barraTituloPremium);
            this.getContentPane().revalidate();
            this.getContentPane().repaint();
        }
        try {
            // Executa a leitura do arquivo .lic na raiz do sistema
            LicencaVO licenca = LicencaManager.lerArquivoLicenca("licenca.lic");
            long diasRestantes = licenca.getDiasRestantes();

            // CASO 1: Licença Vencida ou com 0 Dias (Bloqueia tudo)
            if (licenca.estaExpirada() || diasRestantes <= 0) {
                campoUsuario.setEnabled(false);
                campoSenha.setEnabled(false);
                buttonLogar.setEnabled(false);
                buttonLimpar.setEnabled(false);
                
                MensagemSistema.mostrarAvisoDark(this, 
                    "<center><font color='#FF3333'><b>SISTEMA BLOQUEADO</b></font><br>" +
                    "Sua licença expirou. Contate o suporte.</center>" +
                    "<center>(51)98111-7127</center> ");
            } 
            // CASO 2: Aviso Prévio (Falta menos de 15 dias para expirar)
            else if (diasRestantes <= 15) {
                MensagemSistema.mostrarAvisoDark(this, 
                    "<center><font color='#D4AF37'><b>AVISO DE RENOVAÇÃO</b></font><br></center>" +
                    "<center>Sua licença expira em " + diasRestantes + " dias.</center>");
            }
            
            // Se a licença for válida, você pode passar o tipo de armazenamento aqui para a sua Factory:
            // dao.DaoFactory.setModoAtivo(licenca.getTipoArmazenamento());

        } catch (Exception e) {
            // CASO 3: Fraude, Modificação ou Arquivo Ausente (Tratamento de Contingência Crítica)
            campoUsuario.setEnabled(false);
            campoSenha.setEnabled(false);
            buttonLogar.setEnabled(false);
            buttonLimpar.setEnabled(false);
            
            MensagemSistema.mostrarAvisoDark(this, 
                "<center><font color='#FF3333'><b>ERRO DE SEGURANÇA</b></font><br>" +
                "Licença ausente ou corrompida.</center>");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        campoUsuario = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        buttonLogar = new javax.swing.JButton();
        buttonLimpar = new javax.swing.JButton();
        labelCopyright = new java.awt.Label();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabelBarraSuperior = new javax.swing.JLabel();
        jLabelDev = new javax.swing.JLabel();
        campoSenha = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setForeground(new java.awt.Color(153, 153, 153));

        campoUsuario.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        campoUsuario.setToolTipText("Digite seu nome de usuário");
        campoUsuario.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoUsuarioActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 30)); // NOI18N
        jLabel1.setText("ACESSO AO SISTEMA");

        buttonLogar.setBackground(new java.awt.Color(49, 130, 206));
        buttonLogar.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonLogar.setText("LOGAR");
        buttonLogar.setBorder(null);
        buttonLogar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonLogar.setFocusTraversalPolicyProvider(true);
        buttonLogar.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                buttonLogarFocusGained(evt);
            }
        });
        buttonLogar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLogarActionPerformed(evt);
            }
        });
        buttonLogar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                buttonLogarKeyPressed(evt);
            }
        });

        buttonLimpar.setBackground(new java.awt.Color(49, 130, 206));
        buttonLimpar.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonLimpar.setText("LIMPAR");
        buttonLimpar.setBorder(null);
        buttonLimpar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLimparActionPerformed(evt);
            }
        });

        labelCopyright.setAlignment(java.awt.Label.CENTER);
        labelCopyright.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        labelCopyright.setText("Copyright © Since 2022-2026 SRS Consultoria TI LTDA");

        campoSenha.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(136, 136, 136)
                        .addComponent(labelCopyright, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabelBarraSuperior, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelDev, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(212, 212, 212)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(campoUsuario)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(buttonLogar, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                                .addComponent(buttonLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(campoSenha))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabelBarraSuperior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelDev, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE))
                .addGap(35, 35, 35)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(campoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(campoSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(buttonLimpar, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(buttonLogar, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
                .addGap(13, 13, 13)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelCopyright, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLimparActionPerformed
        campoUsuario.setText("");
        campoSenha.setText("");
    }//GEN-LAST:event_buttonLimparActionPerformed

    private void buttonLogarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLogarActionPerformed
        String nomeCliente ="PORTOBELLA Brecho & Outlet";
        System.out.println("Nome do Cliente: "+nomeCliente);
        String driver = "com.mysql.cj.jdbc.Driver";
        System.out.println("URL do Driver: "+driver);
        try {           
            user = campoUsuario.getText().trim();
            String password = new String(campoSenha.getPassword());
            System.out.println("Na tela: "+user+" + "+password);
            udao.selectUsuario(u);
            usuario = u.getUsuario();
            senha = u.getPassword();
            System.out.println("Na base: "+usuario+ "+" +senha);                  
            if(!user.isEmpty() && user.equals(usuario) && !password.isEmpty() && password.equals(senha)){
                System.out.println("User: "+campoUsuario.getText()+" Senha: "+campoSenha.getText());
                if(user.equals(usuario) && password.equals(senha)){
                    // ==========================================
                    // 🔥 INICIA O SERVIDOR DE PAGAMENTOS
                    // ==========================================
                    try {
                        util.PagamentoServer.iniciar();
                        System.out.println("✅ Servidor de pagamentos iniciado com sucesso!");
                    } catch (IOException e) {
                        System.err.println("❌ Erro ao iniciar servidor de pagamentos: " + e.getMessage());
                        MensagemSistema.mostrarAvisoDark(this, "Erro ao iniciar servidor de pagamentos: " + e.getMessage());
                    }
                    
                    // ==========================================
                    // 🔥 INICIA O SERVIÇO DE NOTIFICAÇÃO DE VENDAS
                    // ==========================================
                    try {
                        NotificacaoVendasService.iniciar();
                        System.out.println("✅ Serviço de notificação de vendas iniciado com sucesso!");
                    } catch (Exception e) {
                        System.err.println("❌ Erro ao iniciar serviço de notificações: " + e.getMessage());
                        MensagemSistema.mostrarAvisoDark(this, "Erro ao iniciar serviço de notificações: " + e.getMessage());
                    }
                    
                    telaMenu.setVisible(true);
                    System.out.println("Acessou o sistema com Sucesso!!!");
                    dispose();
                }else{
                    System.out.println("Usuario: "+usuario+" e "+senha+"");
                    MensagemSistema.mostrarAvisoDark(this, "Usuário ou senha não podem ser nulos!");            
                    System.out.println("Usuário ou senha nulos!");
                    campoUsuario.setText("");
                    campoSenha.setText("");
                }
            }else{               
                MensagemSistema.mostrarAvisoDark(this, "Usuário ou senha incorretos. Tente novamente!");
                System.out.println("Usuário ou senha incorretos!");
                campoUsuario.setText("");
                campoSenha.setText("");
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TelaAcessoLogin.class.getName()).log(Level.SEVERE, null, ex);
            MensagemSistema.mostrarAvisoDark(this, "Erro: "+ex);
            System.out.println("Erro: "+ex);
        }
        
    }//GEN-LAST:event_buttonLogarActionPerformed

    private void buttonLogarFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_buttonLogarFocusGained
        buttonLogar.requestFocusInWindow();     
    }//GEN-LAST:event_buttonLogarFocusGained

    private void buttonLogarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buttonLogarKeyPressed
        buttonLogar.doClick();
    }//GEN-LAST:event_buttonLogarKeyPressed

    private void campoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoUsuarioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoUsuarioActionPerformed
    
    
    public static void main(String args[]) {      
        try {
             try {
            // 🔥 Injeta o tema moderno e limpo do FlatLaf Light
            com.formdev.flatlaf.FlatLightLaf.setup();
            } catch (Exception ex) {
                System.err.println("Falha ao inicializar o tema FlatLaf: " + ex);
            }

            java.awt.EventQueue.invokeLater(() -> {
                new TelaAcessoLogin().setVisible(true);
            });
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaAcessoLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            System.out.println("Erro: "+ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
//                new TelaAcessoLogin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonLimpar;
    private javax.swing.JButton buttonLogar;
    private javax.swing.JPasswordField campoSenha;
    private javax.swing.JTextField campoUsuario;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelBarraSuperior;
    private javax.swing.JLabel jLabelDev;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private java.awt.Label labelCopyright;
    // End of variables declaration//GEN-END:variables
}
