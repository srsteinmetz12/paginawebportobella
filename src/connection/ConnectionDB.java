package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.ConfigLoader;

public class ConnectionDB {
    
    public static Connection getConnection() throws ClassNotFoundException {
        try {
            String DRIVER = "com.mysql.cj.jdbc.Driver";

            // 1. Captura as variáveis via ConfigLoader
            String URL_LOCAL = ConfigLoader.get("db.url");
            String NOME_BANCO_LOCAL = ConfigLoader.get("db.nome_banco");
            String USER = ConfigLoader.get("db.user");
            String PASSWORD = ConfigLoader.get("db.password");

            // 2. FALLBACK OPERACIONAL (Se o .jar não ler o arquivo, assume o padrão local)
            if (URL_LOCAL == null || URL_LOCAL.trim().isEmpty()) {
                URL_LOCAL = "jdbc:mysql://localhost:3306/"; // Força o protocolo JDBC obrigatório
                System.out.println("Aviso: URL do banco não localizada no ConfigLoader. Usando padrão Local.");
            } else {
                // Garante que a URL vinda das configurações comece com o protocolo do driver
                if (!URL_LOCAL.startsWith("jdbc:mysql://")) {
                    URL_LOCAL = "jdbc:mysql://" + URL_LOCAL;
                }
                // Garante uma barra no final da URL para colar com o nome do banco
                if (!URL_LOCAL.endsWith("/")) {
                    URL_LOCAL = URL_LOCAL + "/";
                }
            }

            if (NOME_BANCO_LOCAL == null || NOME_BANCO_LOCAL.trim().isEmpty()) {
                NOME_BANCO_LOCAL = "dbbrecho"; // Seu banco padrão
            }
            if (USER == null) {
                USER = "root"; // Seu usuário padrão
            }
            if (PASSWORD == null) {
                PASSWORD = "S@N805281#"; // Sua senha padrão do MySQL local
            }

            // 3. Montagem limpa da String de Conexão com parâmetros de fuso horário
            String URL = URL_LOCAL + NOME_BANCO_LOCAL + "?useTimezone=true&serverTimezone=UTC";
            System.out.println("Tentando conectar em: " + URL);

            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASSWORD);                       

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Erro de infraestrutura de banco: " + ex);
            throw new RuntimeException("Erro de Conexão: ", ex);        
        }       
    }
    
    public static Connection getConnectionCloud() throws ClassNotFoundException, SQLException{
        try{
            String DRIVER = "com.mysql.cj.jdbc.Driver";
            if (DRIVER == null) {
                DRIVER = "com.mysql.cj.jdbc.Driver"; 
            }else{
                DRIVER = "com.mysql.cj.jdbc.Driver";
            }
            String CLOUD_URL = ConfigLoader.get("db.cloud_url");
            String CLOUD_USER = ConfigLoader.get("db.cloud_user");
            String CLOUD_PASSWORD = ConfigLoader.get("db.cloud_password");
            Class.forName(DRIVER);
            return DriverManager.getConnection(CLOUD_URL, CLOUD_USER, CLOUD_PASSWORD);
        }catch(ClassNotFoundException ex){
            System.err.println("Erro: "+ex);
            throw new RuntimeException("Erro de Conexão: ", ex);
        }      
    }
    
    public void closeConnection(Connection con, Connection con2){
        try {
            if(con != null){
                con.close();
            }
            if(con2 != null){
                con2.close();
            }           
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionDB.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
    
    public void closeConnection(Connection con , Connection con2, PreparedStatement stmt) throws SQLException{
        closeConnection(con, con2);
        if(stmt != null){
            stmt.close();
        }    
    }
    
    public void closeConnection(Connection con , Connection con2, PreparedStatement stmt, ResultSet rs) throws SQLException{
        closeConnection(con, con2, stmt);
        try {
            if(rs != null){
                rs.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionDB.class.getName()).log(Level.SEVERE, null, ex);
        }             
    }
    
    public void main(String[] args, Connection con, Connection con2) throws ClassNotFoundException, SQLException{
        con = getConnection();
        con2 = getConnectionCloud();
    }
    
    private double buscarPrecoProduto(String produtoId) throws ClassNotFoundException, SQLException {
        double preco = 0.0;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = ConnectionDB.getConnectionCloud(); // Seu método existente
            String sql = "SELECT precosug FROM estoque WHERE codpeca = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, produtoId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                preco = rs.getDouble("precosug");
                System.out.println("💰 Preço encontrado para " + produtoId + ": R$ " + preco);
            } else {
                System.out.println("⚠️ Produto não encontrado: " + produtoId);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar preço: " + e.getMessage());
        } finally {
            fecharRecursos(rs, stmt, con);
        }

        return preco;
    }

    /**
     * Busca o nome do produto no banco Aiven
     */
    private String buscarNomeProduto(String produtoId) throws SQLException, ClassNotFoundException {
        String nome = "Produto PORTOBELLA";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            String sql = "SELECT itemdesc FROM estoque WHERE codpeca = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, produtoId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                nome = rs.getString("itemdesc");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar nome: " + e.getMessage());
        } finally {
            fecharRecursos(rs, stmt, con);
        }

        return nome;
    }

/**
 * MÉTODO PARA MARCAR PRODUTO COMO VENDIDO (chamado pelo webhook)
 */
    public void marcarProdutoComoVendido(String produtoId) throws ClassNotFoundException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = ConnectionDB.getConnectionCloud();
            String sql = "UPDATE estoque SET status = 'VENDIDO', data_venda = CURRENT_TIMESTAMP WHERE codpeca = ? AND status = 'DISPONIVEL'";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, produtoId);

            int atualizados = stmt.executeUpdate();
            if (atualizados > 0) {
                System.out.println("✅ Produto " + produtoId + " marcado como VENDIDO!");
            } else {
                System.out.println("⚠️ Produto " + produtoId + " não encontrado ou já vendido.");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao marcar produto como vendido: " + e.getMessage());
        } finally {
            fecharRecursos(null, stmt, con);
        }
    }

    /**
     * Método auxiliar para fechar recursos JDBC
     */
    private void fecharRecursos(ResultSet rs, Statement stmt, Connection con) throws SQLException {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        if (stmt != null) stmt.close();
        try { if (con != null) con.close(); } catch (SQLException e) {}
    }
}
