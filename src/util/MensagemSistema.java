package util;

public class MensagemSistema {
    
    // --- PALETA LUXO/MODA PREMIUM COMPARTILHADA ---
    private static final java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
    private static final java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
    private static final java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
    private static final java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
    private static final java.awt.Color cinzaLinhas     = new java.awt.Color(60, 60, 60);    // #3C3C3C
    
    //POPUP 1: CAIXA DE INPUT/DIGITAÇÃO REUTILIZÁVEL (CÓDIGOS, SENHAS, ETC)
    public static String mostrarInputDark(java.awt.Component telaPai, String mensagemPersonalizada) {

        // Instanciação da mini janela flutuante sem barras do Windows
        final javax.swing.JDialog popupDarkItem = new javax.swing.JDialog(javax.swing.SwingUtilities.getWindowAncestor(telaPai), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        popupDarkItem.setUndecorated(true); 
        popupDarkItem.setSize(280, 140);
        
        javax.swing.JPanel painelJanela = new javax.swing.JPanel(null);
        painelJanela.setBackground(grafiteProfundo);
        painelJanela.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));

        // Rótulo da Mensagem Superior (Recebe a variável com o texto do relatório)
        javax.swing.JLabel lblMensagem = new javax.swing.JLabel(mensagemPersonalizada, javax.swing.SwingConstants.CENTER);
        lblMensagem.setForeground(brancoPuro);
        lblMensagem.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblMensagem.setBounds(15, 15, 250, 20);
        painelJanela.add(lblMensagem);

        // Campo de Texto Aberto para Digitação do Código
        final javax.swing.JTextField txtCodigoInput = new javax.swing.JTextField();
        txtCodigoInput.setBackground(grafiteClaro);
        txtCodigoInput.setForeground(brancoPuro);
        txtCodigoInput.setCaretColor(brancoPuro);
        txtCodigoInput.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));
        txtCodigoInput.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        txtCodigoInput.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCodigoInput.setBounds(35, 45, 210, 28);
        painelJanela.add(txtCodigoInput);

        // Botão OK
        javax.swing.JButton btnOk = new javax.swing.JButton("OK");
        btnOk.setBackground(grafiteClaro); btnOk.setForeground(brancoPuro);
        btnOk.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnOk.setFocusPainted(false); btnOk.setBorderPainted(false);
        btnOk.setBounds(35, 88, 100, 28);
        btnOk.putClientProperty("JButton.buttonType", "square");
        painelJanela.add(btnOk);

        // Botão Cancelar
        javax.swing.JButton btnCancelar = new javax.swing.JButton("Cancelar");
        btnCancelar.setBackground(grafiteClaro); btnCancelar.setForeground(brancoPuro);
        btnCancelar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnCancelar.setFocusPainted(false); btnCancelar.setBorderPainted(false);
        btnCancelar.setBounds(145, 88, 100, 28);
        btnCancelar.putClientProperty("JButton.buttonType", "square");
        painelJanela.add(btnCancelar);

        // Controle de Gatilhos (Compatível com Java antigo)
        final boolean[] confirmouAcesso = {false};

        java.awt.event.ActionListener acaoConfirmar = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmouAcesso[0] = true;
                popupDarkItem.dispose();
            }
        };
        btnOk.addActionListener(acaoConfirmar);
        txtCodigoInput.addActionListener(acaoConfirmar);

        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmouAcesso[0] = false;
                popupDarkItem.dispose();
            }
        });

        popupDarkItem.getContentPane().add(painelJanela);
        popupDarkItem.setLocationRelativeTo(null);

        // Foco automático no teclado
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                txtCodigoInput.requestFocusInWindow();
            }
        });
        
        popupDarkItem.setVisible(true); // O código pausa aqui até fechar

        // Retorna o texto digitado se clicou em OK, ou retorna vazio "" se cancelou
        if (confirmouAcesso[0]) {
            return txtCodigoInput.getText().trim();
        }
        return ""; 
    }
    
    // 🔥 POPUP 2: CAIXA DE MENSAGEM INFORMATIVA REUTILIZÁVEL (SUCESSO, AVISOS)
//    public static void mostrarAvisoDark(java.awt.Component telaPai, String textoMensagem) {
//
//        // Instanciação do popup sem a barra branca superior do sistema operacional
//        final javax.swing.JDialog popupMensagem = new javax.swing.JDialog(javax.swing.SwingUtilities.getWindowAncestor(telaPai), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
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
//        popupMensagem.setLocationRelativeTo(null); // Centraliza no meio exato da sua Tela de Relatórios
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
    
        // 🔥 POPUP 2: CAIXA DE MENSAGEM INFORMATIVA REUTILIZÁVEL (SUCESSO, AVISOS)
    public static void mostrarAvisoDark(java.awt.Component telaPai, String textoMensagem) {

        // 1. Extração segura da janela ancestral (Evita o NullPointerException de vez)
        java.awt.Window ancestral = null;
        if (telaPai != null) {
            ancestral = javax.swing.SwingUtilities.getWindowAncestor(telaPai);
        }

        // 2. Instanciação do popup usando o ancestral seguro
        final javax.swing.JDialog popupMensagem = new javax.swing.JDialog(ancestral, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        popupMensagem.setUndecorated(true); 
        
        // CORREÇÃO 1: Aumentamos a altura da janela de 120 para 170 para dar espaço ao relatório
        popupMensagem.setSize(340, 170); 
        
        javax.swing.JPanel painelJanela = new javax.swing.JPanel(null);
        painelJanela.setBackground(grafiteProfundo);
        painelJanela.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));

        // Rótulo de Texto da Mensagem (Alinhado à esquerda dentro do espaço para ficar elegante)
        javax.swing.JLabel lblMensagem = new javax.swing.JLabel("<html>" + textoMensagem + "</html>", javax.swing.SwingConstants.CENTER);
        lblMensagem.setForeground(brancoPuro);
        lblMensagem.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        
        // CORREÇÃO 2: Aumentamos a altura útil do texto de 45 para 90 para caber todas as quebras <br>
        lblMensagem.setBounds(20, 15, 300, 90);
        painelJanela.add(lblMensagem);

        // Botão Único de Confirmação OK
        javax.swing.JButton btnOk = new javax.swing.JButton("OK");
        btnOk.setBackground(grafiteClaro); 
        btnOk.setForeground(brancoPuro);
        btnOk.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnOk.setFocusPainted(false); 
        btnOk.setBorderPainted(false);
        
        // CORREÇÃO 3: Descemos o botão verticalmente de 75 para 125 para não encavalar no texto
        btnOk.setBounds(120, 125, 100, 28); 
        btnOk.putClientProperty("JButton.buttonType", "square");
        painelJanela.add(btnOk);

        // Gatilho de Fechamento por clique compatível com Java antigo
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMensagem.dispose(); // Fecha a janela e destrava a Thread do sistema
            }
        });

        popupMensagem.getContentPane().add(painelJanela);
        popupMensagem.setLocationRelativeTo(ancestral); // Centraliza na tela de origem

        // Trava o foco diretamente no botão OK para o operador apenas dar ENTER para fechar
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                btnOk.requestFocusInWindow();
            }
        });
        
        popupMensagem.setVisible(true); // Abre de forma síncrona na tela
    }


    
    // 🔥 POPUP 3: CAIXA DE DECISÃO REUTILIZÁVEL (SIM / NÃO)
    public static boolean mostrarDecisaoDark(java.awt.Component telaPai, String textoMensagem) {

        final javax.swing.JDialog popupDecisao = new javax.swing.JDialog(javax.swing.SwingUtilities.getWindowAncestor(telaPai), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        popupDecisao.setUndecorated(true); 
        popupDecisao.setSize(380, 140);
        
        javax.swing.JPanel painelJanela = new javax.swing.JPanel(null);
        painelJanela.setBackground(grafiteProfundo);
        painelJanela.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaLinhas, 1));

        // Ícone de Interrogação sutil em Dourado (HTML)
        javax.swing.JLabel lblIcone = new javax.swing.JLabel("<html><font color='#D4AF37' size='6'><b>?</b></font></html>", javax.swing.SwingConstants.CENTER);
        lblIcone.setBounds(15, 20, 40, 45);
        painelJanela.add(lblIcone);

        // Texto da Mensagem (Suporta as duas linhas do aviso de filtro)
        javax.swing.JLabel lblMensagem = new javax.swing.JLabel("<html>" + textoMensagem + "</html>");
        lblMensagem.setForeground(brancoPuro);
        lblMensagem.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblMensagem.setBounds(65, 15, 300, 50);
        painelJanela.add(lblMensagem);

        // Botão SIM
        final javax.swing.JButton btnSim = new javax.swing.JButton("Sim");
        btnSim.setBackground(grafiteClaro); 
        btnSim.setForeground(brancoPuro);
        btnSim.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnSim.setFocusPainted(false); 
        btnSim.setBorderPainted(false);
        btnSim.setBounds(85, 90, 100, 28);
        btnSim.putClientProperty("JButton.buttonType", "square");
        painelJanela.add(btnSim);

        // Botão NÃO
        javax.swing.JButton btnNao = new javax.swing.JButton("Não");
        btnNao.setBackground(grafiteClaro); 
        btnNao.setForeground(brancoPuro);
        btnNao.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnNao.setFocusPainted(false); 
        btnNao.setBorderPainted(false);
        btnNao.setBounds(195, 90, 100, 28);
        btnNao.putClientProperty("JButton.buttonType", "square");
        painelJanela.add(btnNao);

        // Vetor de controle para o escopo anônimo (Compatível com Java antigo)
        final boolean[] respostaSim = {false};

        // Ação do Botão SIM
        btnSim.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                respostaSim[0] = true;
                popupDecisao.dispose();
            }
        });

        // Ação do Botão NÃO
        btnNao.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                respostaSim[0] = false;
                popupDecisao.dispose();
            }
        });

        popupDecisao.getContentPane().add(painelJanela);
        popupDecisao.setLocationRelativeTo(null); // Centraliza no meio exato da tela de relatórios

        // Força o foco inicial no botão SIM para manter a velocidade do teclado (Enter aceita)
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                btnSim.requestFocusInWindow();
            }
        });
        
        popupDecisao.setVisible(true); // Bloqueia a execução até o operador escolher

        return respostaSim[0]; // Retorna true se escolheu "Sim", e false se escolheu "Não"
    }
}
