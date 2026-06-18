package views;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;
import dao.ConfigDAO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import models.Config;

public class Foto extends javax.swing.JFrame {
    
    private  Dimension dimensao_default;
    private Webcam webcam;
    boolean executando = true;
    private String USER;
    ConfigDAO cdao = new ConfigDAO();
    Config c = new Config();
    
    @SuppressWarnings("LeakingThisInConstructor")
    public Foto() {
        initComponents();
        util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
               // --- 1. DEFINIÇÃO DA PALETA LUXO/MODA PREMIUM (java.awt.Color) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color pretoPuro       = new java.awt.Color(0, 0, 0);       // #000000
        java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C
        // --- 2. 🔥 REVESTIMENTO DOS PAINÉIS DE FUNDO (PRESERVA O SEU ALINHAMENTO) ---
        this.getContentPane().setBackground(grafiteProfundo);        
        jLayeredPane1.setBackground(grafiteProfundo);
        jLayeredPane1.setOpaque(true);
        jLayeredPane1.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));        
        jPanel1.setBackground(grafiteProfundo);
        jPanel1.setOpaque(true);
        // --- 3. MOLDURA ELEGANTE PARA O FEED DA WEBCAM (TELA PRETA CENTRAL) ---
        labelTela.setBackground(new java.awt.Color(35, 35, 35));
        labelTela.setOpaque(true);
        labelTela.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
        // --- 4. CAMPO DE TEXTO INFERIOR DARK STYLE ---
        campoTexto.setBackground(grafiteClaro);
        campoTexto.setForeground(brancoPuro);
        campoTexto.setCaretColor(brancoPuro);
        campoTexto.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
        campoTexto.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        // --- 5. VETOR DE BOTÕES UTILITÁRIOS: DESIGN PLANO FLAT STYLE ---
        // Mapeia Iniciar, Parar e Sair no padrão grafite elegante do menu principal
        javax.swing.JButton[] botoesSecundarios = { buttonIniciar, buttonParar, buttonSair };
        for (javax.swing.JButton btn : botoesSecundarios) {
            btn.setBackground(grafiteClaro);
            btn.setForeground(brancoPuro);
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false); // 🔥 Arranca os contornos chanfrados antigos do Windows! [links: 10]
            btn.putClientProperty("JButton.buttonType", "square"); // Força o FlatLaf Plano [links: 10]
        }
        // --- 6. 🔥 BOTÃO PRINCIPAL (CAPTURAR): DESTAQUE MÁXIMO EM DOURADO OURO ---
        buttonCapturar.setBackground(douradoOuro);
        buttonCapturar.setForeground(pretoPuro); // Letras pretas dão contraste total na cor ouro
        buttonCapturar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        buttonCapturar.setFocusPainted(false);
        buttonCapturar.setBorderPainted(false); // 🔥 Remove o BevelBorder nativo do NetBeans [links: 10]
        buttonCapturar.putClientProperty("JButton.buttonType", "square");       
         // 🔥 TOM DE BRONZE ACOBREADO: Destaque sofisticado e visível para o botão de voltar
        java.awt.Color bronzeAcobreado = new java.awt.Color(140, 120, 83); // #8C7853
        java.awt.Color textoBrancoPuro  = new java.awt.Color(255, 255, 255); // #FFFFFF       
        // Ajuste o nome da variável para o botão MENU da tela atual (ex: buttonMenu, buttonMenuRetire)
        buttonSair.setBackground(bronzeAcobreado);
        buttonSair.setForeground(textoBrancoPuro); // Letras brancas dão leitura perfeita no bronze
        buttonSair.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        buttonSair.setFocusPainted(false);
        buttonSair.setBorderPainted(false);
        buttonSair.putClientProperty("JButton.buttonType", "square");
                // --- CONFIGURAÇÃO DA BARRA DE TÍTULO PREMIUM NA TELA DE FOTOS ---
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
        javax.swing.JLabel lblClienteBarra = new javax.swing.JLabel("PORTOBELLA Brechó & Outlet  |  Módulo de Captura de Mídia");
        lblClienteBarra.setForeground(brancoPuroBarra);
        lblClienteBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        barraTituloPremium.add(lblClienteBarra, java.awt.BorderLayout.WEST);
        
        // 3. Lado Direito: Bloco Agrupador (Assinatura + Botão X)
        javax.swing.JPanel painelDireitoBarra = new javax.swing.JPanel();
        painelDireitoBarra.setBackground(grafiteClaroBarra);
        painelDireitoBarra.setOpaque(true);
        painelDireitoBarra.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 0));
        
        // Texto de Desenvolvimento de Grife
        javax.swing.JLabel lblDevBarra = new javax.swing.JLabel("");
        lblDevBarra.setForeground(douradoOuroBarra); 
        lblDevBarra.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        painelDireitoBarra.add(lblDevBarra);
        
        // Botão X: Plano e integrado para fechar a tela (Diferencial: no módulo de fotos ele apenas oculta a janela)
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
        
        // 🔥 Ação de fechamento: Como o botão "Sair" original fecha apenas a janela de fotos voltando pro Estoque,
        // usamos o 'dispose();' em vez de derrubar o sistema inteiro (System.exit).
//        btnFecharJanela.addActionListener(e -> {
//            this.dispose();
//        });
        
//        painelDireitoBarra.add(btnFecharJanela);
        barraTituloPremium.add(painelDireitoBarra, java.awt.BorderLayout.EAST);
        
        // 4. Posiciona e estica a barra no topo exato da tela de captura
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

        // 6. Inserção inteligente: Garante o acoplamento no jLayeredPane1 ou jPanel1
        try {
            jLayeredPane1.add(barraTituloPremium);
            jLayeredPane1.revalidate();
            jLayeredPane1.repaint();
        } catch(Exception e) {
            try {
                jPanel1.add(barraTituloPremium);
                jPanel1.revalidate();
                jPanel1.repaint();
            } catch(Exception ex) {
                this.getContentPane().add(barraTituloPremium);
                this.getContentPane().revalidate();
                this.getContentPane().repaint();
            }
        }
        this.getRootPane().setDefaultButton(buttonCapturar);
        this.setLocationRelativeTo(null);
        this.setTitle("PortoBella Brecho & Outlet");
        inicializa();
        iniciaVideo();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLayeredPane1 = new javax.swing.JLayeredPane();
        buttonIniciar = new javax.swing.JButton();
        buttonParar = new javax.swing.JButton();
        buttonCapturar = new javax.swing.JButton();
        labelTela = new javax.swing.JLabel();
        buttonSair = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        campoTexto = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(204, 204, 255));
        setUndecorated(true);

        jLayeredPane1.setBackground(new java.awt.Color(204, 204, 255));
        jLayeredPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        buttonIniciar.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonIniciar.setText("Iniciar");
        buttonIniciar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        buttonIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonIniciarActionPerformed(evt);
            }
        });

        buttonParar.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonParar.setText("Parar");
        buttonParar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        buttonParar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPararActionPerformed(evt);
            }
        });

        buttonCapturar.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonCapturar.setText("Capturar");
        buttonCapturar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        buttonCapturar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCapturarActionPerformed(evt);
            }
        });

        labelTela.setBackground(new java.awt.Color(51, 51, 51));
        labelTela.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        labelTela.setForeground(new java.awt.Color(51, 255, 0));
        labelTela.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelTela.setText("                                              ");
        labelTela.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        labelTela.setOpaque(true);

        buttonSair.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        buttonSair.setText("Sair");
        buttonSair.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        buttonSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSairActionPerformed(evt);
            }
        });

        jLayeredPane1.setLayer(buttonIniciar, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(buttonParar, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(buttonCapturar, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(labelTela, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(buttonSair, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(buttonIniciar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonParar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonCapturar, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                    .addComponent(buttonSair, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(labelTela, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap(37, Short.MAX_VALUE)
                .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jLayeredPane1Layout.createSequentialGroup()
                        .addComponent(buttonIniciar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buttonParar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonCapturar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buttonSair, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(labelTela, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        campoTexto.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        campoTexto.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(campoTexto)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(campoTexto, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLayeredPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonPararActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPararActionPerformed
        new Thread(){
            @Override
            public void run(){
                executando = false;
                labelTela.setText("Parando...");
                webcam.close();
                labelTela.setText("");
            }
        }.start();
    }//GEN-LAST:event_buttonPararActionPerformed

    private void buttonCapturarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCapturarActionPerformed
        cdao = new ConfigDAO();
        c = new Config();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(webcam.getImage(), "png", baos);
            byte[] bytes = baos.toByteArray();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            BufferedImage imagem = ImageIO.read(bais);
            
            int nova_largura = 500, nova_altura = 500; // escolha do tamanho do pix da imagem 
            BufferedImage novaImagem = new BufferedImage(nova_largura, nova_altura, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = novaImagem.createGraphics();
            g.drawImage(imagem, 0, 0, nova_largura, nova_altura, null);                     
            try {
                cdao.lerNomeUser(c);
                USER = c.usuarioSistema;
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(Foto.class.getName()).log(Level.SEVERE, null, ex);
            }               
            ImageIO.write(novaImagem, "png", new File("C:\\Users\\"+USER+"\\Documents\\Itens\\"+campoTexto.getText()+".png"));
            campoTexto.setText("");
            JOptionPane.showMessageDialog(this, "Foto Capturada com sucesso!");           
        } catch (IOException e) {
            System.out.println("Erro: "+e.getMessage());
        }
    }//GEN-LAST:event_buttonCapturarActionPerformed

    private void buttonIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonIniciarActionPerformed
        new Thread(){
            @Override
            public void run(){
                executando = true;
                labelTela.setText("Iniciando...");
                webcam.open();
                labelTela.setText("");
                iniciaVideo();
            }
        }.start();
    }//GEN-LAST:event_buttonIniciarActionPerformed

    private void buttonSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSairActionPerformed
        new Thread(){
            @Override
            public void run(){
                executando = false;
                webcam.close();
            }
        }.start();
        dispose();
    }//GEN-LAST:event_buttonSairActionPerformed

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
            java.util.logging.Logger.getLogger(Foto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Foto().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCapturar;
    private javax.swing.JButton buttonIniciar;
    private javax.swing.JButton buttonParar;
    private javax.swing.JButton buttonSair;
    private javax.swing.JTextField campoTexto;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelTela;
    // End of variables declaration//GEN-END:variables
    
    private void inicializa(){
        try {
            dimensao_default = WebcamResolution.VGA.getSize();
            webcam = Webcam.getDefault();
            webcam.setViewSize(dimensao_default);
            
            for(Dimension dimension : webcam.getViewSizes()){
                System.out.println("Largura: "+dimension.getWidth()+ " Altura: "+dimension.getHeight());
            }
            
        } catch (WebcamException ex) {
//            System.out.println("Erro: "+ex);
        }
    }
    
    public void iniciaVideo(){
        
        new  Thread(){
            @Override
            public void run(){
                while (true) { 
                    try {
                        Image imagem = webcam.getImage();
                        ImageIcon icon = new ImageIcon(imagem);
                        icon.setImage(icon.getImage().getScaledInstance(labelTela.getWidth(), labelTela.getHeight(), 100));
                        labelTela.setIcon(icon);
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }                    
                }
            }
        }.start();        
    }    
}
