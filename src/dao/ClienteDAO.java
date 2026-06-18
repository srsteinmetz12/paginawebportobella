package dao;

import connection.ConnectionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import models.Cliente;
import util.MensagemSistema;
import static views.TelaCliente.cc;
import views.TelaFinanceiro;

public class ClienteDAO {
    
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    Connection con;
    Connection con2;
    String sql;
    ResultSet rs;
    StringBuilder todosNomes;
    
    public int saveCliente(Cliente c) throws ClassNotFoundException, SQLException {
        String sql = "INSERT INTO cliente(codcli, datacadastro, nomecli, cepcli, cidadecli, ufcli, enderecocli, numerocli, complementocli, bairrocli, tamanhocli, emailcli, telefonecli, redecli, obscli) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";       
        int linhasInseridas = 0;
        try (Connection con = ConnectionDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, c.getCodCli());
            stmt.setDate(2, new java.sql.Date(c.getDataCadastro().getTime()));
            stmt.setString(3, c.getNomeCli());
            stmt.setString(4, c.getCepCli());
            stmt.setString(5, c.getCidadeCli());
            stmt.setString(6, c.getUFCli());
            stmt.setString(7, c.getEnderecoCli());
            stmt.setString(8, c.getNumeroCli());
            stmt.setString(9, c.getComplementoCli());
            stmt.setString(10, c.getBairroCli());
            stmt.setString(11, c.getTamanhoCli());
            stmt.setString(12, c.getEmailCli());
            stmt.setString(13, c.getTelefoneCli());
            stmt.setString(14, c.getRedeCli());
            stmt.setString(15, c.getObsCli());            

            linhasInseridas = stmt.executeUpdate();           
            System.out.println("Acessou o banco de dados! Registros: " + linhasInseridas);
            System.out.println("----------------------------------");

        } catch (SQLException ex) {
            System.out.println("Erro ao inserir dados: " + ex.toString()); 
            System.out.println("Erro ao cadastrar Cliente!");
            System.out.println("----------------------------------");
            throw ex; 
        }
        System.out.println("Conexão encerrada automaticamente!");
        System.out.println("Fim da inclusão!");
        System.out.println("----------------------------------");

        return linhasInseridas;
    }
  
    public int saveClienteCloud(Cliente c) throws ClassNotFoundException, SQLException {
        String sql = "INSERT INTO cliente(codcli, datacadastro, nomecli, cepcli, cidadecli, ufcli, enderecocli, numerocli, complementocli, bairrocli, tamanhocli, emailcli, telefonecli, redecli, obscli) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";       
        System.out.println("Pesquisa: " + sql);        
        int linhasInseridas = 0;
        try (Connection con2 = ConnectionDB.getConnectionCloud();
            PreparedStatement stmt2 = con2.prepareStatement(sql)) {
            stmt2.setString(1, c.getCodCli());
            stmt2.setDate(2, new java.sql.Date(c.getDataCadastro().getTime()));
            stmt2.setString(3, c.getNomeCli());
            stmt2.setString(4, c.getCepCli());
            stmt2.setString(5, c.getCidadeCli());
            stmt2.setString(6, c.getUFCli()); 
            stmt2.setString(7, c.getEnderecoCli());
            stmt2.setString(8, c.getNumeroCli());
            stmt2.setString(9, c.getComplementoCli());
            stmt2.setString(10, c.getBairroCli());
            stmt2.setString(11, c.getTamanhoCli());
            stmt2.setString(12, c.getEmailCli());
            stmt2.setString(13, c.getTelefoneCli());
            stmt2.setString(14, c.getRedeCli());
            stmt2.setString(15, c.getObsCli());            

            linhasInseridas = stmt2.executeUpdate();                      
            System.out.println("Acessou o banco de dados! Registros inseridos: " + linhasInseridas);
            System.out.println("----------------------------------");

        } catch (SQLException ex) {
            System.out.println("Erro ao inserir dados: " + ex.toString()); 
            System.out.println("Erro ao cadastrar Cliente!");
            System.out.println("----------------------------------");
            throw ex;
        }
        System.out.println("Conexão Cloud encerrada automaticamente!");
        System.out.println("Fim da inclusão na Nuvem!");
        System.out.println("----------------------------------");

        return linhasInseridas;
    }
 
    public void updateCliente(Cliente c) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "UPDATE cliente SET codcli=?, datacadastro=?, nomecli=?, cepcli=?, cidadecli=?, ufcli=?, enderecocli=?, numerocli=?, complementocli=?, bairrocli=?, tamanhocli=?, emailcli=?, telefonecli=?, redecli=?, obscli=? WHERE codcli=?";
        System.out.println("Pesquisa: " + sql);       
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;

            stmt.setString(1, c.getCodCli());
            stmt.setDate(2, new java.sql.Date(c.getDataCadastro().getTime()));
            stmt.setString(3, c.getNomeCli());
            stmt.setString(4, c.getCepCli());
            stmt.setString(5, c.getCidadeCli());
            stmt.setString(6, c.getUFCli());
            stmt.setString(7, c.getEnderecoCli());
            stmt.setString(8, c.getNumeroCli());
            stmt.setString(9, c.getComplementoCli());
            stmt.setString(10, c.getBairroCli());
            stmt.setString(11, c.getTamanhoCli());
            stmt.setString(12, c.getEmailCli());
            stmt.setString(13, c.getTelefoneCli());
            stmt.setString(14, c.getRedeCli());
            stmt.setString(15, c.getObsCli());            
            stmt.setString(16, c.getCodCli());
            
            stmt.executeUpdate();           
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");           
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            if (con != null) {
                con.close();
            }
            System.out.println("Conexão encerrada!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Atualização!");
            System.out.println("----------------------------------");
        }
    }
    
    public void updateClienteCloud(Cliente c) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "UPDATE cliente SET codcli=?, datacadastro=?, nomecli=?, cepcli=?, cidadecli=?, ufcli=?, enderecocli=?, numerocli=?, complementocli=?, bairrocli=?, tamanhocli=?, emailcli=?, telefonecli=?, redecli=?, obscli=? WHERE codcli=?";
        System.out.println("Pesquisa: " + sql);       
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2;

            stmt2.setString(1, c.getCodCli());
            stmt2.setDate(2, new java.sql.Date(c.getDataCadastro().getTime()));
            stmt2.setString(3, c.getNomeCli());
            stmt2.setString(4, c.getCepCli());
            stmt2.setString(5, c.getCidadeCli());
            stmt2.setString(6, c.getUFCli());
            stmt2.setString(7, c.getEnderecoCli());
            stmt2.setString(8, c.getNumeroCli());
            stmt2.setString(9, c.getComplementoCli());
            stmt2.setString(10, c.getBairroCli());
            stmt2.setString(11, c.getTamanhoCli());
            stmt2.setString(12, c.getEmailCli());
            stmt2.setString(13, c.getTelefoneCli());
            stmt2.setString(14, c.getRedeCli());
            stmt2.setString(15, c.getObsCli());                       
            stmt2.setString(16, c.getCodCli()); 
            
            int linhasAlteradas = stmt2.executeUpdate();           
            System.out.println("Acessou o banco de dados na Cloud com sucesso! Registros alterados: " + linhasAlteradas);
            System.out.println("----------------------------------");           
        } catch (SQLException ex) {
            System.err.println("Erro ao atualizar cliente na Cloud: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
                System.out.println("Fim da Atualização!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexões de atualização: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void deleteCliente(Cliente c) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "DELETE from cliente WHERE codcli=?";
        System.out.println("Pesquisa: " + sql);
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;
            stmt.setString(1, c.getCodCli());
            stmt.executeUpdate();           
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");                       
        } catch (SQLException ex) {
            System.out.println("Erro ao tentar remover o registro!");
            System.out.println("----------------------------------");
            System.out.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            if (con != null) {
                con.close();
            }
            System.out.println("Conexão encerrada!");
            System.out.println("----------------------------------");
            System.out.println("Fim da exclusão!");
            System.out.println("----------------------------------");
        }
    }
    
    public void deleteClienteCloud(Cliente c) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "DELETE FROM cliente WHERE codcli = ?";
        System.out.println("Pesquisa: " + sql);
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2;

            stmt2.setString(1, c.getCodCli());
            int linhasAfetadas = stmt2.executeUpdate();                       
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("Clientes removidos na Cloud: " + linhasAfetadas);
            System.out.println("----------------------------------");                       
        } catch (SQLException ex) {
            System.err.println("Erro ao tentar remover o registro!");
            System.out.println("----------------------------------");
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
                System.out.println("Fim da exclusão!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexões de exclusão de cliente: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }

    public void selectCliente(Cliente c) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM cliente WHERE codcli = ?"; 
        System.out.println("Pesquisa: " + sql);       
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;
            stmt.setString(1, cc); 
            rs = stmt.executeQuery(); 
            
            if (rs.next()) {
                c.setCodCli(rs.getString(1));
                c.setDataCadastro(rs.getDate(2));
                c.setNomeCli(rs.getString(3));
                c.setCepCli(rs.getString(4));
                c.setCidadeCli(rs.getString(5));
                c.setUFCli(rs.getString(6));
                c.setEnderecoCli(rs.getString(7));
                c.setNumeroCli(rs.getString(8));
                c.setComplementoCli(rs.getString(9));
                c.setBairroCli(rs.getString(10));
                c.setTamanhoCli(rs.getString(11));
                c.setEmailCli(rs.getString(12));
                c.setTelefoneCli(rs.getString(13));
                c.setRedeCli(rs.getString(14));
                c.setObsCli(rs.getString(15));                
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            } else {
                MensagemSistema.mostrarAvisoDark(null, "Cliente Inválido ou NÃO Cadastrado!");
                System.out.println("Cliente não cadastrado!");
                System.out.println("----------------------------------");
            }            
            System.out.println("Acessou o banco de dados com sucesso!");
            System.out.println("----------------------------------");            
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            if (con != null) {
                con.close();
            }
            System.out.println("Conexão encerrada!");
            System.out.println("----------------------------------");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }
    
    public void selectClienteCloud(String s, Cliente c) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM cliente WHERE codcli = ?"; 
        System.out.println("Pesquisa: " + sql);

        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2;           
            stmt2.setString(1, s); 
            rs = stmt2.executeQuery();           
            if (rs.next()) {
                c.setCodCli(rs.getString(1));
                c.setDataCadastro(rs.getDate(2));
                c.setNomeCli(rs.getString(3));
                c.setCepCli(rs.getString(4));
                c.setCidadeCli(rs.getString(5));
                c.setUFCli(rs.getString(6));
                c.setEnderecoCli(rs.getString(7));
                c.setNumeroCli(rs.getString(8));
                c.setComplementoCli(rs.getString(9));
                c.setBairroCli(rs.getString(10));
                c.setTamanhoCli(rs.getString(11));
                c.setEmailCli(rs.getString(12));
                c.setTelefoneCli(rs.getString(13));
                c.setRedeCli(rs.getString(14));
                c.setObsCli(rs.getString(15));                               
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            } else {
                MensagemSistema.mostrarAvisoDark(null, "Cliente Inválido ou NÃO Cadastrado!");
                System.out.println("Cliente não cadastrado!");
                System.out.println("----------------------------------");
            }            
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            try {
                if (rs != null) rs.close();
                if (con2 != null) con2.close();
                System.out.println("Conexão encerrada!");
                System.out.println("----------------------------------");
                System.out.println("Fim da Pesquisa!");
                System.out.println("----------------------------------");
            } catch (SQLException ex) {
                System.err.println("Erro ao fechar conexões de busca: " + ex.getMessage());
                System.out.println("----------------------------------");
            }
        }
    }
    
    public void selectCodCliente(Cliente c) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();       
        sql = "SELECT MAX(codcli) FROM cliente";              
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                c.setCodCli(rs.getString(1));                              
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            } else {
                System.out.println("Cliente não Cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            if (con != null) {
                con.close();
            }
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }
    
    public void selectCodClienteCloud(Cliente c) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();       
        sql = "SELECT MAX(codcli) FROM cliente";               
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2;
            rs = stmt2.executeQuery();
            
            if (rs.next()) {
                c.setCodCli(rs.getString(1));                              
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("----------------------------------");
            } else {
                System.out.println("Cliente não Cadastrado!");
                System.out.println("----------------------------------");
            }
        } catch (SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            if (con2 != null) {
                con2.close();
            }
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }
    
    public void buscaNomeCliente(Cliente c) throws ClassNotFoundException, SQLException {
        con = ConnectionDB.getConnection();
        sql = "SELECT nomecli FROM cliente WHERE nomecli LIKE ? ORDER BY nomecli LIMIT 15";
        System.out.println(sql);       
        try (PreparedStatement localStmt = con.prepareStatement(sql)) {
            this.stmt = localStmt;
            
            stmt.setString(1, TelaFinanceiro.nomeCliente + "%");
            rs = stmt.executeQuery();
            
            todosNomes = new StringBuilder();
            boolean temResultados = false;
            
            while (rs.next()) {
                if (temResultados) {
                    todosNomes.append(";");
                }
                todosNomes.append(rs.getString("nomecli"));
                temResultados = true;
            }           
            if (temResultados) {
                c.setNomeCli(todosNomes.toString());
                System.out.println("Encontrados " + (todosNomes.toString().split(";").length) + " clientes");
            } else {
                c.setNomeCli("");
                System.out.println("Nenhum cliente encontrado!");
            }
        } catch(SQLException ex) {
            System.err.println("Erro: " + ex.getMessage());
            c.setNomeCli("");
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            if (con != null) {
                con.close();
            }
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da Pesquisa!");
            System.out.println("----------------------------------");
        }
    }
    
    public void buscaNomeClienteCloud(Cliente c) throws ClassNotFoundException, SQLException {
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT nomecli FROM cliente ORDER BY nomecli ASC";
        System.out.println("Pesquisa de Nomes na Nuvem: " + sql);        
        
        try (PreparedStatement localStmt2 = con2.prepareStatement(sql)) {
            this.stmt2 = localStmt2;           
            rs = stmt2.executeQuery();
            StringBuilder localTodosNomes = new StringBuilder();
            
            while (rs.next()) {
                String nome = rs.getString("nomecli");
                if (nome != null && !nome.trim().isEmpty()) {
                    localTodosNomes.append(nome.trim()).append(";");
                }
            }
            c.setNomeCli(localTodosNomes.toString());
            
            System.out.println("Nomes de clientes baixados com sucesso da Cloud!");
            System.out.println("----------------------------------");
            
        } catch (SQLException ex) {
            System.err.println("Erro SQL ao buscar nomes de clientes: " + ex.getMessage());
            System.out.println("----------------------------------");
            throw ex;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (con2 != null) try { con2.close(); } catch (SQLException e) {}
            System.out.println("Conexão de busca de clientes encerrada!");
            System.out.println("----------------------------------");
        }
    }

     // SQLs centralizadas como constantes para evitar duplicação de strings
    private static final String INSERT_SQL = 
        "INSERT INTO cliente(codcli, datacadastro, nomecli, cepcli, cidadecli, ufcli, " +
        "enderecocli, numerocli, complementocli, bairrocli, tamanhocli, emailcli, " +
        "telefonecli, redecli, obscli) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL = 
        "UPDATE cliente SET codcli=?, datacadastro=?, nomecli=?, cepcli=?, cidadecli=?, " +
        "ufcli=?, enderecocli=?, numerocli=?, complementocli=?, bairrocli=?, " +
        "tamanhocli=?, emailcli=?, telefonecli=?, redecli=?, obscli=? WHERE codcli=?";
}
