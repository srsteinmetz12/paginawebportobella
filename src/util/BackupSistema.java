package util;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;

public class BackupSistema {
    public void executarBackupGeralCloud() {
        System.out.println("Iniciando rotina de contingência e backup em Java Puro (Aiven Cloud)...");
        System.out.println("----------------------------------------------------------------------");

        // 1. Definições de conexão da infraestrutura Aiven Cloud
        String usuario = ConfigLoader.get("db.cloud.user"); 
        String senha = ConfigLoader.get("db.cloud.password");  
        String banco = ConfigLoader.get("db.cloud.database");         
        String host = "://aivencloud.com"; 
        String porta = "12610"; 

        try {
            // Descobre o caminho físico absoluto de onde o .jar está rodando
            String caminhoJar = BackupSistema.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File arquivoJar = new File(caminhoJar);
            File pastaDist = arquivoJar.getParentFile(); 

            // Criação da pasta de armazenamento de backups dentro da dist
            File pastaBackups = new File(pastaDist, "backups_sistema");
            if (!pastaBackups.exists()) {
                pastaBackups.mkdir();
            }

            // Definição do nome do arquivo com timestamp por segundo
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomeArquivo = "Backup_Portobella_" + timestamp + ".sql";
            File arquivoDestino = new File(pastaBackups, nomeArquivo);

            // 🔥 URL JDBC TOTALMENTE LIMPA E PADRONIZADA (Sem parâmetros na String para evitar o erro de Parse)
            String urlConexaoSintaxeEstrita = "jdbc:mysql://" + host + ":" + porta + "/" + banco;

            // Propriedades isoladas passadas diretamente para a API nativa do Driver
            java.util.Properties propriedadesConexao = new java.util.Properties();
            propriedadesConexao.setProperty("user", usuario);
            propriedadesConexao.setProperty("password", senha);
            propriedadesConexao.setProperty("useSSL", "true");
            propriedadesConexao.setProperty("sslMode", "REQUIRED"); // Requisito obrigatório da nuvem Aiven
            propriedadesConexao.setProperty("allowPublicKeyRetrieval", "true");
            propriedadesConexao.setProperty("serverTimezone", "UTC");

            System.out.println("Conectando de forma segura ao endpoint da Aiven com sintaxe estrita...");

            // Força o carregamento explícito da classe do Driver 9.6.0 antes de chamar o DriverManager
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Abertura do fluxo de gravação e conexão direta com a base Cloud
            try (Connection con = java.sql.DriverManager.getConnection(urlConexaoSintaxeEstrita, propriedadesConexao);
                 java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(arquivoDestino))) {

                writer.println("-- ========================================================");
                writer.println("-- BACKUP AUTOMÁTICO - BRECHÓ PORTOBELLA");
                writer.println("-- GERADO EM: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                writer.println("-- ========================================================\n");
                writer.println("SET FOREIGN_KEY_CHECKS = 0;\n");

                // Coleta os metadados reais da estrutura das tabelas na nuvem
                DatabaseMetaData dbMetaData = con.getMetaData();

                // Filtra as tabelas da base de dados ativa
                try (java.sql.ResultSet tabelas = dbMetaData.getTables(banco, null, "%", new String[]{"TABLE"})) {
                    while (tabelas.next()) {
                        String nomeTabela = tabelas.getString("TABLE_NAME");
                        System.out.println("Exportando dados da tabela: " + nomeTabela);

                        writer.println("-- Dados da tabela: " + nomeTabela);
                        writer.println("TRUNCATE TABLE `" + nomeTabela + "`;"); 

                        String sqlSelect = "SELECT * FROM " + nomeTabela;

                        try (java.sql.Statement stmt = con.createStatement();
                             java.sql.ResultSet rs = stmt.executeQuery(sqlSelect)) {

                            ResultSetMetaData rsMetaData = rs.getMetaData();
                            int numColunas = rsMetaData.getColumnCount();

                            while (rs.next()) {
                                StringBuilder sbInsert = new StringBuilder();
                                sbInsert.append("INSERT INTO `").append(nomeTabela).append("` VALUES (");

                                for (int i = 1; i <= numColunas; i++) {
                                    Object valor = rs.getObject(i);

                                    if (valor == null) {
                                        sbInsert.append("NULL");
                                    } else if (valor instanceof Number) {
                                        sbInsert.append(valor);
                                    } else if (valor instanceof java.sql.Date || valor instanceof java.sql.Timestamp) {
                                        sbInsert.append("'").append(valor.toString()).append("'");
                                    } else {
                                        // Limpa aspas simples internas do texto para não quebrar a sintaxe do MySQL
                                        String textoLimpo = valor.toString().replace("'", "''");
                                        sbInsert.append("'").append(textoLimpo).append("'");
                                    }

                                    if (i < numColunas) {
                                        sbInsert.append(", ");
                                    }
                                }
                                sbInsert.append(");");
                                writer.println(sbInsert.toString());
                            }
                        }
                        writer.println("\n");
                    }
                }

                writer.println("SET FOREIGN_KEY_CHECKS = 1;");
                System.out.println("Backup consolidado com sucesso em: " + arquivoDestino.getAbsolutePath());
                System.out.println("--------------------------------------------");

                JOptionPane.showMessageDialog(null, 
                    "Cópia de segurança (.sql) gerada com sucesso!\nSalvo na pasta: backups_sistema", 
                    "Backup Concluído", JOptionPane.INFORMATION_MESSAGE);

                // Abre a pasta automaticamente no Windows para conferência visual
                java.awt.Desktop.getDesktop().open(pastaBackups);
            }

        } catch (HeadlessException | IOException | ClassNotFoundException | URISyntaxException | SQLException ex) {
            System.err.println("Erro crítico ao processar backup via código: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Falha operacional ao processar backup: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
