package views;

import java.io.IOException;

public class TelaAjudaIA extends javax.swing.JFrame {

    @SuppressWarnings({"LeakingThisInConstructor", "LeakingThisInConstructor"})
    public TelaAjudaIA() {
        this.setUndecorated(true);
        initComponents();

        // --- 1. SUA PALETA LUXO/MODA PREMIUM (IDENTIDADE SRS OFICIAL) ---
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);    // #1C1C1C
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);    // #2D2D2D
        java.awt.Color douradoOuro     = new java.awt.Color(212, 175, 55);  // #D4AF37
        java.awt.Color brancoPuro      = new java.awt.Color(255, 255, 255); // #FFFFFF
        java.awt.Color cinzaBordasGrid = new java.awt.Color(60, 60, 60);    // Divisórias
        java.awt.Color pretoCabecalho  = new java.awt.Color(20, 20, 20);    // Topo

        // --- 2. PINTURA DE FUNDO DA JANELA ---
        this.getContentPane().setBackground(grafiteProfundo);

        // --- 3. ESTILIZAÇÃO DO TÍTULO DA TELA ---
        jLabel1.setForeground(brancoPuro);
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));

        // --- 4. ÁREA DO HISTÓRICO DO CHAT (JTextArea) ---
        areaChat.setBackground(grafiteClaro);
        areaChat.setForeground(brancoPuro);
        areaChat.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        areaChat.setEditable(false); // Operador não pode apagar o histórico digitando em cima
        areaChat.setLineWrap(true);  // Quebra a linha automaticamente quando o texto chega no fim
        areaChat.setWrapStyleWord(true);
        
        // Customização do ScrollPane do Chat
        jScrollPane1.getViewport().setBackground(grafiteProfundo);
        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(cinzaBordasGrid, 1));

        // Mensagem inicial de boas-vindas da IA
        areaChat.setText("[Assistente SRS]: Olá! Sou o suporte inteligente do Sistema SRS Consultoria TI.\n"
                       + "Como posso te ajudar hoje?\n\n");

        // --- 5. CAMPO DE DIGITAÇÃO DA PERGUNTA (JTextField) ---
        campoPergunta.setBackground(grafiteClaro);
        campoPergunta.setForeground(brancoPuro);
        campoPergunta.setCaretColor(brancoPuro);
        campoPergunta.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        campoPergunta.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(cinzaBordasGrid, 1),
            javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        // --- 6. BOTÃO ENVIAR (Estilo Creme original ou Dourado) ---
        btnEnviar.setBackground(new java.awt.Color(245, 235, 215)); // Creme Luxo
        btnEnviar.setForeground(new java.awt.Color(30, 30, 30));
        btnEnviar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnEnviar.setFocusPainted(false);
        btnEnviar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEnviar.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12));

        // --- 7. BARRA DE TÍTULOS SUPERIOR PREMIUM COM LOGO E BOTÃO FECHAR X ---
        // (Aproveita a mesma lógica utilitária do favicon que criamos)
        try {
            util.GerenciadorLogoFavicon.aplicarFaviconGlobal(this);
        } catch (Exception ex) {System.err.println("Erro: "+ex);}
        
         // --- 🔴 BOTÃO FECHAR ARREDONDADO COMPACTO (ALINHAMENTO DIRETO NA BARRA) ---
        javax.swing.JButton btnFecharChat = new javax.swing.JButton("X") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g); 
            }
        };
        
        // Alinhamentos estritos para o "X" ficar bem no meio do círculo
        btnFecharChat.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFecharChat.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFecharChat.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        btnFecharChat.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11)); 
        btnFecharChat.setForeground(brancoPuro);
        btnFecharChat.setBackground(pretoCabecalho); // Some na barra preta
        btnFecharChat.setPreferredSize(new java.awt.Dimension(24, 24)); // Esférico perfeito
        btnFecharChat.setFocusPainted(false);
        btnFecharChat.setBorderPainted(false);
        btnFecharChat.setContentAreaFilled(false); 
        btnFecharChat.setOpaque(false);           
        btnFecharChat.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Acende em vermelho quando o mouse entra e volta ao preto quando sai
        btnFecharChat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent evt) { btnFecharChat.setBackground(java.awt.Color.RED); }
            @Override public void mouseExited(java.awt.event.MouseEvent evt) { btnFecharChat.setBackground(pretoCabecalho); }
        });
        
         // Fecha apenas o chat liberando a memória RAM sem derrubar o ERP
        btnFecharChat.addActionListener(e -> dispose());

        // 🔥 O SEGREDO DO ENCAIXE: Adiciona o botão diretamente na extremidade DIREITA (EAST) da barra
//        barraTitulo.add(btnFecharChat, java.awt.BorderLayout.EAST);

        
        // Evento de clique: fecha apenas esta janela sem encerrar o sistema principal
        btnFecharChat.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) { 
                dispose(); 
            }
        });
        
        // Adiciona ao painel direito da sua barra superior
//        painelDireito.add(btnFecharChat);
        
                // --- ⌨️ ATALHO DA TECLA ESC PARA FECHAR O CHAT RAPIDAMENTE ---
        this.getRootPane().registerKeyboardAction(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        }, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        // --- 🖱️ EVENTO DE ARRASTE DA TELA SEM BORDAS (MOUSE LISTENERS) ---
        try {
            // Variável para guardar o ponto exato do clique inicial do mouse
            final java.awt.Point pontoCliqueInicial = new java.awt.Point();

            // Captura o momento em que o operador aperta o botão do mouse na tela
            this.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    pontoCliqueInicial.x = e.getX();
                    pontoCliqueInicial.y = e.getY();
                }
            });

            // Calcula a movimentação e move o JFrame conforme o mouse é arrastado
            this.addMouseMotionListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseDragged(java.awt.event.MouseEvent e) {
                    java.awt.Point localizacaoAtualFrame = getLocation();
                    int novoX = localizacaoAtualFrame.x + e.getX() - pontoCliqueInicial.x;
                    int novoY = localizacaoAtualFrame.y + e.getY() - pontoCliqueInicial.y;
                    setLocation(novoX, novoY);
                }
            });
            System.out.println("Sucesso: Movimentação da TelaAjudaIA ativada!");
        } catch (Exception ex) {
            System.err.println("Erro ao configurar arraste de tela: " + ex.getMessage());
        }


        
        campoPergunta.addActionListener(e -> btnEnviarActionPerformed(null));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        areaChat = new javax.swing.JTextArea();
        campoPergunta = new javax.swing.JTextField();
        btnEnviar = new javax.swing.JButton();
        buttonX = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("     ASSISTENTE DE SUPORTE IA");

        areaChat.setColumns(20);
        areaChat.setRows(5);
        jScrollPane1.setViewportView(areaChat);

        campoPergunta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoPerguntaActionPerformed(evt);
            }
        });

        btnEnviar.setText("ENVIAR");
        btnEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnviarActionPerformed(evt);
            }
        });

        buttonX.setBackground(new java.awt.Color(153, 153, 153));
        buttonX.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        buttonX.setForeground(new java.awt.Color(153, 153, 153));
        buttonX.setText("X");
        buttonX.setBorder(null);
        buttonX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonXActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(campoPergunta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnEnviar))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonX, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(buttonX, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(campoPergunta, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void campoPerguntaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoPerguntaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_campoPerguntaActionPerformed

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
       String pergunta = campoPergunta.getText().trim();
    
        if (pergunta.isEmpty()) {
            return; // Ignora se o operador clicar sem digitar nada
        }

        // 1. Adiciona a pergunta do operador na área do chat
        areaChat.append("[Operador]: " + pergunta + "\n\n");
        campoPergunta.setText(""); // Limpa o campo de texto imediatamente

        // 2. Exibe o indicador de carregamento temporário
        areaChat.append("[Assistente SRS]: Buscando no manual técnico, aguarde...\n");
        areaChat.setCaretPosition(areaChat.getDocument().getLength()); // Rola o scroll para o final

        // 3. Executa a chamada da IA em segundo plano (Thread) para a interface do Java não travar/congelar
        new Thread(() -> {
            try {
                // Dispara a requisição para a inteligência artificial local
                String respostaRealIA = perguntarOllamaLocal(pergunta); 

                String textoAtual = areaChat.getText();
                String textoSemAguarde = textoAtual.substring(0, textoAtual.lastIndexOf("[Assistente SRS]: Buscando no manual"));

                areaChat.setText(textoSemAguarde);
                areaChat.append("[Assistente SRS]: " + respostaRealIA + "\n\n");
                areaChat.setCaretPosition(areaChat.getDocument().getLength()); 

            } catch (Exception ex) {
                areaChat.append("[Assistente SRS]: Falha ao processar resposta da IA local.\n\n");
            }
        }).start();
    }//GEN-LAST:event_btnEnviarActionPerformed

    private void buttonXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonXActionPerformed
        dispose();
    }//GEN-LAST:event_buttonXActionPerformed
    
    public String perguntarOllamaLocal(String perguntaUsuario) {
        // Rota nativa estável do Ollama
        String endpoint = "http://localhost:11434/api/chat"; 

        String contextoManual = "Voce e o assistente de suporte oficial da SRS Consultoria. "
                + "Use estritamente as regras do manual do Sistema de Brechó para responder. "
                + "Regras: O modulo Sacolas trabalha com status EM_SEPARACAO. A busca e por nomecli e lista em lote. "
                + "O modulo Entregas trabalha com status DISPONIVEL e faz o rateio automatico do frete total "
                + "dividido pelo numero de pecas pendentes do cliente, jogando a dízima de centavos na primeira linha. "
                + "O Relatorio Analitico exibe o Faturamento Bruto de Pecas, soma Fretes e subtrai Despesas (-) gerando a Receita Liquida Real. "
                + "O Relatorio Sintetico, Grafico de 30 dias e Anual excluem fretes e despesas usando NOT IN ('despesa', 'frete'). "
                + "O Modo Offline salva local com sincronizado = 0 e sobe no botao Sincronizar Caixa. "
                + "Se a pergunta nao puder ser respondida com esse manual, diga para contatar o suporte da SRS.";

        try {
            java.net.URL url = new java.net.URL(endpoint);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Limpa aspas e barras para o JSON local não quebrar
            perguntaUsuario = perguntaUsuario.replace("\\", "\\\\").replace("\"", "\\\"");

            // Monta o Payload forçando o stream como false para receber tudo de uma vez
            String jsonPayload = "{"
                + "\"model\": \"llama3:8b\"," 
                + "\"stream\": false,"       
                + "\"options\": { \"temperature\": 0.2 },"
                + "\"messages\": ["
                + "  {\"role\": \"system\", \"content\": \"" + contextoManual + "\"},"
                + "  {\"role\": \"user\", \"content\": \"" + perguntaUsuario + "\"}"
                + "]"
                + "}";

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder respostaCompleta = new StringBuilder();
                String linha;
                while ((linha = br.readLine()) != null) {
                    respostaCompleta.append(linha);
                }
                br.close();

                String jsonStr = respostaCompleta.toString();

                // 🔥 SOLUÇÃO COMPLETA: Expressão Regular (Regex) para capturar o conteúdo de "content"
                // Ela ignora se o JSON veio com ou sem espaços entre os dois pontos
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"content\"\\s*:\\s*\"(.*?)\"\\s*[,}]");
                java.util.regex.Matcher matcher = pattern.matcher(jsonStr);

                if (matcher.find()) {
                    String respostaLimpa = matcher.group(1);

                    // Limpa os escapes padrões que o JSON aplica em textos longos
                    return respostaLimpa.replace("\\n", "\n")
                                         .replace("\\\"", "\"")
                                         .replace("\\\\", "\\")
                                         .replace("\\t", "    ");
                }

                // Fallback secundário: caso o padrão do Regex falhe por quebra de linha interna
                if (jsonStr.contains("\"content\":\"")) {
                    int idx = jsonStr.indexOf("\"content\":\"") + 11;
                    return jsonStr.substring(idx, jsonStr.indexOf("\"", idx)).replace("\\n", "\n").replace("\\\"", "\"");
                }

                return "Erro: O Ollama respondeu, mas a estrutura do texto não pôde ser lida.";
            } else {
                return "Erro no Ollama Local (Código HTTP: " + responseCode + "). Certifique-se de que o modelo 'llama3:8b' está ativo.";
            }

        } catch (IOException ex) {
            return "Falha ao conectar ao Ollama local. Erro: " + ex.getMessage();
        }
    }



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
            java.util.logging.Logger.getLogger(TelaAjudaIA.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaAjudaIA.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaAjudaIA.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaAjudaIA.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaAjudaIA().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaChat;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JButton buttonX;
    private javax.swing.JTextField campoPergunta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
