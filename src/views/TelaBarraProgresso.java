package views;

import connection.ConnectionDB;
import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;
import util.ConfigLoader;
import util.RegistraLogs;

public class TelaBarraProgresso extends javax.swing.JFrame {
    RegistraLogs rl = new RegistraLogs();

    public TelaBarraProgresso() {
        initComponents();
        String favicon = ConfigLoader.get("sistema.favicon");
        try {
            java.net.URL urlIcone = getClass().getResource(favicon);
            if (urlIcone != null) {
                this.setIconImage(new ImageIcon(urlIcone).getImage());
                System.out.println("Ícone da TelaMenu carregado com sucesso!");
            } else {
                System.err.println("Aviso: O arquivo /images/favicon.png não foi localizado dentro do JAR.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone: " + e.getMessage());
        }
    }
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barraProgresso = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        barraProgresso.setFont(new java.awt.Font("Times New Roman", 1, 11)); // NOI18N
        barraProgresso.setStringPainted(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barraProgresso, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(barraProgresso, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(TelaBarraProgresso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaBarraProgresso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaBarraProgresso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaBarraProgresso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TelaBarraProgresso().setVisible(true);
            }
        });
    }
    
    /////////// METODOS DA CLASSE /////////////
    public JProgressBar getJProgressBar() {
        return barraProgresso; // Use o nome real da sua barra aqui
    }
    
    public void setProgresso(int valor) {
        barraProgresso.setValue(valor);
    }
    
    public void fechaBarra(){
        dispose();
    }
    public void sincronizarComBarra(JProgressBar barra) {
    String sqlCount = "SELECT COUNT(*) FROM vendas WHERE datavenda >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
    String sqlSelect = "SELECT * FROM vendas WHERE datavenda >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
    String sqlInsert = "INSERT INTO vendas (id, datavenda, origemvenda, tipopag, valorvenda, codpecas, nomecli, obsvendas, entrega, status) VALUES (?,?,?,?,?,?,?,?,?,?)"
                     + "ON DUPLICATE KEY UPDATE valorvenda=VALUES(valorvenda), id=VALUES(id)";

        try (Connection conCloud = ConnectionDB.getConnectionCloud();
             Connection conLocal = ConnectionDB.getConnection()) {

            Statement st = conCloud.createStatement();
            ResultSet rsCount = st.executeQuery(sqlCount);
            rsCount.next();
            int total = rsCount.getInt(1);

            if (total == 0) return;

            PreparedStatement stmtCloud = conCloud.prepareStatement(sqlSelect);
            ResultSet rsCloud = stmtCloud.executeQuery();
            PreparedStatement stmtLocal = conLocal.prepareStatement(sqlInsert);

            int processados = 0;
            while (rsCloud.next()) {
                stmtLocal.setInt(1, rsCloud.getInt("id"));
                stmtLocal.setDate(2, rsCloud.getDate("datavenda"));
                stmtLocal.setString(3, rsCloud.getString("origemvenda"));
                stmtLocal.setString(4, rsCloud.getString("tipopag"));
                stmtLocal.setDouble(5, rsCloud.getDouble("valorvenda"));
                stmtLocal.setInt(6, rsCloud.getInt("codpecas"));
                stmtLocal.setString(7, rsCloud.getString("nomecli"));
                stmtLocal.setString(8, rsCloud.getString("obsvendas"));
                stmtLocal.setString(9, rsCloud.getString("entrega"));
                stmtLocal.setString(10, rsCloud.getString("status"));
                stmtLocal.addBatch();               
                processados++;
                // Atualiza a barra de progresso na Thread da Interface
                int progresso = (int) (((double) processados / total) * 100);
                javax.swing.SwingUtilities.invokeLater(() -> barra.setValue(progresso));

                if (processados % 50 == 0) stmtLocal.executeBatch();
            }
            stmtLocal.executeBatch();

        } catch (Exception ex) {
            System.err.println("Erro na automação: " + ex.getMessage());
        }
        System.out.println("Fim da sincronização automática...");
        System.out.println("-------------------------------------");
    }
    
    public void ativarAlertaAtraso(boolean ativar) {
        java.awt.EventQueue.invokeLater(() -> {
            if (ativar) {
                barraProgresso.setForeground(Color.YELLOW);
                barraProgresso.setString("Sincronização Lenta - Aguarde...");
            } else {
                // Volta para a cor original (ex: laranja da sua foto)
                barraProgresso.setForeground(new Color(255, 153, 0)); 
                barraProgresso.setStringPainted(true);
            }
        });
    }
    
    public void gerarRelatorioSincronizacao(List<String> itensSincronizados) {
        String dataArquivo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        String nomeArquivo = "Relatorio_Sincronia_" + dataArquivo + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
            writer.println("=== RELATÓRIO DE SINCRONIZAÇÃO - BRECHÓ PORTOBELLA ===");
            writer.println("Data/Hora: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println("Total de itens processados: " + itensSincronizados.size());
            writer.println("-----------------------------------------------------");

            itensSincronizados.forEach((item) -> {
                writer.println("- " + item);
            });

            writer.println("-----------------------------------------------------");
            writer.println("Sincronização concluída com sucesso.");
        } catch (IOException ex) {
            rl.registrarLog("Erro ao gerar relatório: " + ex.getMessage());
        }
    }
    
    


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar barraProgresso;
    // End of variables declaration//GEN-END:variables
}
