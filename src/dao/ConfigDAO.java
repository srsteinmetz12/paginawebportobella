
package dao;

import connection.ConnectionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import models.Config;

public class ConfigDAO {
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    String sql;
    Connection con;
    Connection con2;
    ResultSet rs;
    
    public void gravarConfig(Config c) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection(); 
        sql = "INSERT INTO config(nomebanco, senhabanco, nomecli, usuario, id) VALUES (?, ?, ?, ?, ?)";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            stmt.setString(1, c.getNomeBanco());
            stmt.setString(2, c.getSenhaBanco());
            stmt.setString(3, c.getNomeCliente());
            stmt.setString(4, c.getUsuarioSistema());
            stmt.setInt(5, c.getId());           
            stmt.execute();
            System.out.println("Acessou o banco de dados!");
            JOptionPane.showMessageDialog(null, "Registro salvo com sucesso!");
            System.out.println("----------------------------------");
        }catch(SQLException ex){
            System.out.println("Erro ao inserir dados: " +ex.toString()); 
            System.out.println("Erro ao cadastrar config!");
            System.out.println("----------------------------------");           
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da inclusão!");
            System.out.println("----------------------------------");
        }
    }
    public void lerConfig(Config c) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM config";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                c.setNomeBanco(rs.getString(1));
                c.setSenhaBanco(rs.getString(2));
                c.setNomeCliente(rs.getString(3));
                c.setUsuarioSistema(rs.getString(4));           
            }
            stmt.execute();
            System.out.println("Acessou o banco de dados!");
            JOptionPane.showMessageDialog(null, "Leitura realizada com sucesso!");
            System.out.println("----------------------------------");           
        }catch(SQLException ex){
            System.out.println("Erro ao ler dados: " +ex.toString()); 
            System.out.println("Erro ao ler tabela config!");
            System.out.println("----------------------------------");           
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da leitura!");
            System.out.println("----------------------------------");
        }
    }
    public void lerId(Config c) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT MAX(id) FROM config";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                c.setId(rs.getInt(1)); 
            }
            stmt.execute();
            System.out.println("Acessou o banco de dados!");
            System.out.println("----------------------------------");            
        }catch(SQLException ex){
            System.out.println("Erro ao ler dados: " +ex.toString()); 
            System.out.println("Erro ao ler id config!");
            System.out.println("----------------------------------");           
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da leitura!");
            System.out.println("----------------------------------");
        }
    }
    public void lerNomeCliente(Config c) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT nomecli FROM config WHERE (SELECT MAX(id) ORDER BY id ASC)";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                c.setNomeCliente(rs.getString(1)); 
            }
            stmt.execute();
            System.out.println("Acessou o banco de dados!");
            System.out.println("----------------------------------");           
        }catch(SQLException ex){
            System.out.println("Erro ao ler dados: " +ex.toString()); 
            System.out.println("Erro ao ler id config!");
            System.out.println("----------------------------------");           
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da leitura!");
            System.out.println("----------------------------------");
        }
    }
    public void lerNomeUser(Config c) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "SELECT usuario FROM config WHERE (SELECT MAX(id) ORDER BY id ASC)";
        System.out.println(sql);
        try {
            stmt = con.prepareStatement(sql);//tabela
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                c.setUsuarioSistema(rs.getString(1));
            }
            stmt.execute();
            System.out.println("Acessou o banco de dados!");
            System.out.println("----------------------------------");           
        }catch(SQLException ex){
            System.out.println("Erro ao ler dados: " +ex.toString()); 
            System.out.println("Erro ao ler id config!");
            System.out.println("----------------------------------");           
        }finally{
            con.close();
            System.out.println("Conexão encerrada!");
            System.out.println("Fim da leitura!");
            System.out.println("----------------------------------");
        }
    }
}
