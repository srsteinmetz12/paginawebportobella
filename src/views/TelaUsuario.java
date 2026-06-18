package views;

import dao.UsuarioDAO;
import java.awt.HeadlessException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Usuario;
import util.MensagemSistema;

public class TelaUsuario extends javax.swing.JFrame {
    UsuarioDAO udao = new UsuarioDAO();
    Usuario u = new Usuario();
    int anoAtual = java.time.Year.now().getValue();

    @SuppressWarnings("LeakingThisInConstructor")
    public TelaUsuario() {
        this.setUndecorated(true);
        initComponents();
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
                // --- 1. PALETA LUXO/MODA PREMIUM CONTÍNUA (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C

        // --- 2. REVESTIMENTO DO PAINEL DE FUNDO (PRESERVA O SEU ALINHAMENTO) ---
        // Se a sua tela possuir um jPanel1 cobrindo o fundo, a linha abaixo blinda a renderização:
        try {
            jPanel1.setBackground(grafiteProfundo);
            jPanel1.setOpaque(true);
        } catch(Exception ex) {System.err.println("Erro: "+ex);}
        this.getContentPane().setBackground(grafiteProfundo);

        // --- 3. ⚖️ HIERARQUIA DE FONTES OPERACIONAIS CONTRASTANTES ---
        // Nível 1: Título Indicador da Tela (CADASTRO DE USUÁRIOS)
        jLabel1.setForeground(brancoPuro);
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));

        // Nível 2: Nomes Comuns de Campos (USUÁRIO, SENHA, REPETE SENHA)
        javax.swing.JLabel[] labelsCamposUsuarios = { jLabel2, jLabel3, jLabel4 };
        for (javax.swing.JLabel lbl : labelsCamposUsuarios) {
            try {
                lbl.setForeground(brancoPuro);
                lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            } catch(Exception ex) {System.err.println("Erro: "+ex);}
        }

        // --- 4. CAMPOS DE ENTRADA DARK SLIM (JTextFields e JPasswordFields) ---
        // Mapeado tanto para campos de texto simples quanto para campos ocultos de senha
        javax.swing.JTextField[] todosCamposCredenciais = { campoUsuario, campoSenha, campoRepetirSenha };
        for (javax.swing.JTextField txt : todosCamposCredenciais) {
            try {
                txt.setBackground(grafiteClaro);
                txt.setForeground(brancoPuro);
                txt.setCaretColor(brancoPuro);
                txt.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1)); // Linha fina reta
                txt.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
                txt.setHorizontalAlignment(javax.swing.JTextField.CENTER); // Mantém a digitação centralizada estilosa
            } catch(Exception ex) {System.err.println("Erro: "+ex);}
        }

        // --- 5. ESTILIZAÇÃO DOS SEPARADORES DE LINHA (JSeparator) ---
        javax.swing.JSeparator[] todosSeparadores = { jSeparator1, jSeparator2 };
        for (javax.swing.JSeparator sep : todosSeparadores) {
            try {
                sep.setForeground(cinzaLinhas);
                sep.setBackground(cinzaLinhas);
            } catch(Exception ex) {System.err.println("Erro: "+ex);}
        }

        // --- 6. VETOR DE BOTÕES SECUNDÁRIOS BASE: DESIGN PLANO FLAT STYLE ---
        javax.swing.JButton[] botoesBaseUsuarios = { buttonMenu, buttonAlterarSenha};
        for (javax.swing.JButton btn : botoesBaseUsuarios) {
            try {
                btn.setBackground(grafiteClaro);
                btn.setForeground(brancoPuro);
                btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false); // Remove o contorno tridimensional cinza arredondado antigo! [1]
                btn.putClientProperty("JButton.buttonType", "square"); // Força o FlatLaf Plano [1]
            } catch(Exception ex) {System.err.println("Erro: "+ex);}
        }

        // --- 7. 🔥 BOTÃO PRINCIPAL DE AÇÃO (CADASTRAR): DESTAQUE EM DOURADO OURO ---
        try {
            buttonCadastrar.setBackground(douradoOuro);
            buttonCadastrar.setForeground(new java.awt.Color(0, 0, 0)); // Texto em preto para contraste total [1]
            buttonCadastrar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            buttonCadastrar.setFocusPainted(false);
            buttonCadastrar.setBorderPainted(false);
            buttonCadastrar.putClientProperty("JButton.buttonType", "square");
            
            // Ativa o gatilho automático: apertar ENTER nessa tela executa a ação de cadastrar [1]
            this.getRootPane().setDefaultButton(buttonCadastrar);
        } catch(Exception ex) {System.err.println("Erro: "+ex);}
                // 🔥 COR EXCLUSIVA PARA O BOTÃO MENU (Tom Prata Envelhecido/Metálico)
        java.awt.Color bronzeAcobreado = new java.awt.Color(140, 120, 83); // #8C7853
        java.awt.Color textoBrancoPuro  = new java.awt.Color(255, 255, 255); // // #E6E6E6
        
        // Altere 'buttonMenu' para o nome exato da sua variável na tela atual (ex: buttonMenuRetire, buttonMenuVendas)
        buttonMenu.setBackground(bronzeAcobreado);
        buttonMenu.setForeground(textoBrancoPuro); // Letras levemente mais claras para excelente contraste
        buttonMenu.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        buttonMenu.setFocusPainted(false);
        buttonMenu.setBorderPainted(false);
        buttonMenu.putClientProperty("JButton.buttonType", "square");
                // --- CONFIGURAÇÃO DA BARRA DE TÍTULO PREMIUM NA TELA DE USUÁRIOS ---
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
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Controle de Acesso de Usuários");
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
        btnFecharJanela.addActionListener(e -> {
            this.dispose();
        });       
        painelDireitoBarra.add(btnFecharJanela);
        barraTituloPremium.add(painelDireitoBarra, java.awt.BorderLayout.EAST);
        barraTituloPremium.setBounds(0, 0, this.getWidth(), 30);

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
        this.setTitle(util.ConfigLoader.get("sistema.nome_cliente") + " | © 2022-" + anoAtual + " SRS Consultoria TI LTDA");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPasswordField1 = new javax.swing.JPasswordField();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        campoUsuario = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        buttonMenu = new javax.swing.JButton();
        buttonCadastrar = new javax.swing.JButton();
        buttonAlterarSenha = new javax.swing.JButton();
        campoSenha = new javax.swing.JPasswordField();
        campoRepetirSenha = new javax.swing.JPasswordField();
        jSeparator4 = new javax.swing.JSeparator();

        jPasswordField1.setText("jPasswordField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 30)); // NOI18N
        jLabel1.setText("       CADASTRO DE USUÁRIOS");

        campoUsuario.setFont(new java.awt.Font("Times New Roman", 0, 20)); // NOI18N
        campoUsuario.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoUsuario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel2.setText("USUÁRIO");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel3.setText("SENHA");

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel4.setText("REPETE SENHA");

        buttonMenu.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonMenu.setText("MENU");
        buttonMenu.setBorder(null);
        buttonMenu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenuActionPerformed(evt);
            }
        });

        buttonCadastrar.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonCadastrar.setText("CADASTRAR");
        buttonCadastrar.setBorder(null);
        buttonCadastrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        buttonCadastrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCadastrarActionPerformed(evt);
            }
        });

        buttonAlterarSenha.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        buttonAlterarSenha.setText("ALTERAR SENHA");
        buttonAlterarSenha.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        buttonAlterarSenha.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        campoSenha.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoSenha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoSenhaActionPerformed(evt);
            }
        });

        campoRepetirSenha.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        campoRepetirSenha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoRepetirSenhaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 656, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(366, 366, 366)
                                .addComponent(buttonAlterarSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(307, 307, 307)
                        .addComponent(buttonCadastrar, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(179, 179, 179)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(campoSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(campoRepetirSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(campoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(163, 163, 163)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 445, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(77, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 377, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(140, 140, 140))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(jLabel1)
                .addGap(11, 11, 11)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoRepetirSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(29, 29, 29)
                .addComponent(buttonCadastrar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonAlterarSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(57, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenuActionPerformed
        TelaMenu tm = new TelaMenu();
        tm.setVisible(true);
        dispose();
    }//GEN-LAST:event_buttonMenuActionPerformed

    private void buttonCadastrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCadastrarActionPerformed
        try {
            if((!campoUsuario.getText().isEmpty() || !campoSenha.getText().isEmpty() || !campoRepetirSenha.getText().isEmpty())){
                int x = 0;
                try {
                    udao.selectIdUsuarioCloud(u);       
                    int codigo = u.getId();
                    System.out.println(codigo);
                    if(codigo != 0){
                        x = codigo;
                        int idUsuario = (x + 1);
                        u.setId(idUsuario);
                    }else{
                        u.setId(1);
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TelaCliente.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro: "+ex);
                } catch (SQLException ex) {
                    Logger.getLogger(TelaUsuario.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro: "+ex);
                }
                u.setUsuario(campoUsuario.getText());
                System.out.println(campoUsuario.getText());
                u.setPassword(Arrays.toString(campoSenha.getPassword()));
                System.out.println(campoSenha.getText());
                System.out.println(campoRepetirSenha.getText());
                try {
                    if(campoSenha.getText().equals(campoRepetirSenha.getText())){
                        udao.saveUsuario(u);
                    }else{
                        MensagemSistema.mostrarAvisoDark(this, "Senha inválida, tente novamente!");
                    }
                } catch (ClassNotFoundException | SQLException ex) {
                    Logger.getLogger(TelaUsuario.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Erro: "+ex);
                }
            }else{
                MensagemSistema.mostrarAvisoDark(this, "Usuário ou senha inválidos, tente novamente!");
                System.out.println("Usuário ou senha inválidos!");
            }
        } catch (HeadlessException e) {
            MensagemSistema.mostrarAvisoDark(this, "Erro: "+e);
            System.out.println("Erro: "+e);
        }       
        campoUsuario.setText("");
        campoSenha.setText("");
        campoRepetirSenha.setText("");
    }//GEN-LAST:event_buttonCadastrarActionPerformed

    private void campoRepetirSenhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoRepetirSenhaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoRepetirSenhaActionPerformed

    private void campoSenhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoSenhaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoSenhaActionPerformed

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
                new TelaUsuario().setVisible(true);
                
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAlterarSenha;
    private javax.swing.JButton buttonCadastrar;
    private javax.swing.JButton buttonMenu;
    private javax.swing.JPasswordField campoRepetirSenha;
    private javax.swing.JPasswordField campoSenha;
    private javax.swing.JTextField campoUsuario;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    // End of variables declaration//GEN-END:variables
}
