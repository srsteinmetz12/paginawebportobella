package views;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import util.ConfigLoader;

public class TelaSelecaoEtiquetas extends javax.swing.JDialog {

    private JTable tabela;
    private DefaultTableModel model;
    private final String pastaEtiquetas;
    private final java.awt.Component telaFinanceiraDestino;// Guarda a referência da tela que chamou
    String favicon = ConfigLoader.get("sistema.favicon");
    
    public TelaSelecaoEtiquetas(java.awt.Frame parent, boolean modal, String pastaEtiquetas, java.awt.Component telaFinanceiraDestino) {
        super(parent, modal);
        this.pastaEtiquetas = pastaEtiquetas;
        this.telaFinanceiraDestino = telaFinanceiraDestino;
        initManualComponents();
        carregarArquivosEtiquetas();
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void initManualComponents() {
        setUndecorated(true);
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        
        java.awt.Color grafiteProfundo = new java.awt.Color(28, 28, 28);
        java.awt.Color grafiteClaro    = new java.awt.Color(45, 45, 45);
        java.awt.Color cinzaLinhas     = new java.awt.Color(70, 70, 70);
        java.awt.Color pretoCabecalho  = new java.awt.Color(20, 20, 20);
        java.awt.Color brancoPuro      = java.awt.Color.WHITE;

        getContentPane().setBackground(grafiteProfundo);
        getRootPane().setBorder(BorderFactory.createLineBorder(cinzaLinhas, 1));
        setLayout(new BorderLayout());

        // ─── BARRA DE TÍTULO PREMIUM ACESSÓRIA ───
        JPanel barraTitulo = new JPanel(new BorderLayout());
        barraTitulo.setBackground(pretoCabecalho);
        barraTitulo.setPreferredSize(new java.awt.Dimension(500, 35));
        barraTitulo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, cinzaLinhas));

        JLabel lblTitulo = new JLabel("  Histórico de Etiquetas Geradas (QR Code)");
        lblTitulo.setForeground(brancoPuro);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        barraTitulo.add(lblTitulo, BorderLayout.WEST);
        
        // Painel Esquerdo: Logo + Título
        javax.swing.JPanel painelEsquerdo = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 6));
        painelEsquerdo.setOpaque(false);

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

        JButton btnFechar = new JButton(" X ");
        btnFechar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFechar.setForeground(brancoPuro);
        btnFechar.setBackground(pretoCabecalho);
        btnFechar.setFocusPainted(false);
        btnFechar.setBorderPainted(false);
        btnFechar.addActionListener(e -> dispose());
        barraTitulo.add(btnFechar, BorderLayout.EAST);
        add(barraTitulo, BorderLayout.NORTH);

        // ─── TABELA DE ARQUIVOS INTERNA ───
        tabela = new JTable();
        model = new DefaultTableModel(new Object[]{"ETIQUETAS DO SISTEMA (CLIQUE DUPLO)", "CAMINHO_COMPLETO"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tabela.setModel(model);
        
        // Oculta a coluna do caminho do arquivo em disco, deixando apenas o nome bonito para a operadora
        tabela.getColumnModel().getColumn(1).setMinWidth(0);
        tabela.getColumnModel().getColumn(1).setMaxWidth(0);

        tabela.setBackground(grafiteClaro);
        tabela.setForeground(brancoPuro);
        tabela.setGridColor(cinzaLinhas);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabela.setRowHeight(26);

        javax.swing.table.JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setOpaque(true);
        cabecalho.setBackground(new java.awt.Color(220, 220, 220));
        cabecalho.setForeground(new java.awt.Color(40, 40, 40));
        cabecalho.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBackground(grafiteProfundo);
        scroll.getViewport().setBackground(grafiteProfundo);
        scroll.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(scroll, BorderLayout.CENTER);

        // 🚀 GATILHO AUTOMÁTICO: Captura clique duplo e transfere para a tela financeira
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tabela.getSelectedRow() != -1) {
                    processarSelecaoEtiqueta();
                }
            }
        });
    }
    
    private void carregarArquivosEtiquetas() {
        File diretorio = new File(pastaEtiquetas);
        if (diretorio.exists() && diretorio.isDirectory()) {
            File[] arquivos = diretorio.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
            if (arquivos != null) {
                for (File f : arquivos) {
                    model.addRow(new Object[]{f.getName(), f.getAbsolutePath()});
                }
            }
        }
    }

    private void processarSelecaoEtiqueta() {
        int linhaSelecao = tabela.getSelectedRow();
        String nomeArquivo = model.getValueAt(linhaSelecao, 0).toString(); // Pega o nome (ex: etiqueta_1024_49,90.png)

        try {
            // 1. Limpa as extensões e prefixos do texto do arquivo
            String textoLimpo = nomeArquivo.replace(".png", "").replace("etiqueta_", "");
            String caminhoCompleto = model.getValueAt(linhaSelecao, 1).toString();

            String codigoExtraido = "";
            String valorExtraido = "";

            // 2. Separa os dados usando o caractere split se houver o preço anexado no nome
            if (textoLimpo.contains("_")) {
                String[] partes = textoLimpo.split("_");
                codigoExtraido = partes[0];
                valorExtraido = partes[1].replace(",", ".");
            } else {
                // Caso seja uma etiqueta antiga sem o preço no nome, traz apenas o código
                codigoExtraido = textoLimpo;
            }

            // 3. Injeta simultaneamente os dados nos componentes públicos da TelaFinanceiro
            if (telaFinanceiraDestino instanceof TelaFinanceiro) {
                TelaFinanceiro telaFin = (TelaFinanceiro) telaFinanceiraDestino;

                telaFin.campoCodPeca.setText(codigoExtraido);

                // Só preenche o valor se ele foi encontrado no padrão do nome do arquivo
                if (!valorExtraido.isEmpty()) {
                    telaFin.campoValorVenda.setText(valorExtraido);
                }

                // Foca automaticamente no campo de código para o operador prosseguir
                telaFin.campoCodPeca.requestFocus();
            }
             // ─── 🪵 4. MOTOR DE EXCLUSÃO AUTOMÁTICA DA ETIQUETA EM DISCO ───
            File arquivoEtiqueta = new File(caminhoCompleto);
            if (arquivoEtiqueta.exists()) {
                boolean deletadoComSucesso = arquivoEtiqueta.delete(); // 🔥 Apaga o arquivo físico .png do HD!
                if (deletadoComSucesso) {
                    System.out.println("Arquivo de etiqueta limpo com sucesso do diretório: " + nomeArquivo);
                } else {
                    System.err.println("Aviso: O Java não conseguiu apagar o arquivo fisicamente (pode estar aberto por outro processo).");
                }
            }

            util.MensagemSistema.mostrarAvisoDark(null, "Código e Valor vinculados com sucesso ao Financeiro!");
            dispose(); // Fecha o JDialog

        } catch (Exception ex) {
            System.err.println("Erro ao separar dados da etiqueta: " + ex.getMessage());
        }
    }

    
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
            java.util.logging.Logger.getLogger(TelaSelecaoEtiquetas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaSelecaoEtiquetas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaSelecaoEtiquetas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaSelecaoEtiquetas.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
//                new TelaSelecaoEtiquetas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
