package dao;

import connection.ConnectionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.swing.JOptionPane;
import models.Usuario;
import util.MensagemSistema;
import static views.TelaAcessoLogin.user;
import views.TelaFornecedor;

public class UsuarioDAO {
    PreparedStatement stmt = null;
    PreparedStatement stmt2 = null;
    Connection con;
    Connection con2;
    String sql;
    ResultSet rs;
    
    public void saveUsuario(Usuario u) throws ClassNotFoundException, SQLException{
        con = ConnectionDB.getConnection();
        sql = "INSERT INTO usuario(id, usuario, password) VALUES (?, ?, ?)";       
        try {
            stmt = con.prepareStatement(sql);//tabela          
            stmt.setInt(1, u.getId());              
            stmt.setString(2, u.getUsuario());
            stmt.setString(3, u.getPassword());           
            stmt.execute();
            MensagemSistema.mostrarAvisoDark(null, "Usuário cadastrado com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Usuário Salvo com sucesso!");
            System.out.println("----------------------------------");
        }catch (SQLException ex){
            MensagemSistema.mostrarAvisoDark(null, "Erro ao salvar Usuário: "+ex);
            System.out.println("Erro ao inserir Usuário: " +ex.toString());
            System.out.println("----------------------------------");
            System.out.println("Erro ao salvar o Usuário!");
            System.out.println("----------------------------------");
        }finally{
            con.close();
            System.out.println("Conexão de banco encerrada!");
            System.out.println("----------------------------------");
        }
    } 
    
    public void saveUsuarioCloud(Usuario u) throws ClassNotFoundException, SQLException{
        con2 = ConnectionDB.getConnectionCloud();
        sql = "INSERT INTO usuario(id, usuario, password) VALUES (?, ?, ?)";       
        try {
            stmt2 = con2.prepareStatement(sql);//tabela          
            stmt2.setInt(1, u.getId());              
            stmt2.setString(2, u.getUsuario());
            stmt2.setString(3, u.getPassword());           
            stmt2.execute();
            MensagemSistema.mostrarAvisoDark(null, "Usuário cadastrado com sucesso!");
            System.out.println("----------------------------------");
            System.out.println("Usuário Salvo com sucesso!");
            System.out.println("----------------------------------");
        }catch (SQLException ex){
            MensagemSistema.mostrarAvisoDark
        (null, "Erro ao salvar Usuário: "+ex);
            System.out.println("Erro ao inserir Usuário: " +ex.toString());
            System.out.println("----------------------------------");
            System.out.println("Erro ao salvar o Usuário!");
            System.out.println("----------------------------------");
        }finally{
            con2.close();
            System.out.println("Conexão de banco encerrada!");
            System.out.println("----------------------------------");
        }
    } 
    
    public  void selectIdUsuario(Usuario u) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "SELECT MAX(id) FROM usuario";       
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                u.setId(rs.getInt(1));                              
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("-------------------------------------");
            }else{
                System.out.println("Id não cadastrado!");
                System.out.println("-------------------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("-------------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("-------------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("-------------------------------------");
            }
        }
    }
    
    public  void selectIdUsuarioCloud(Usuario u) throws ClassNotFoundException, SQLException{
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT MAX(id) FROM usuario";       
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery(sql);
            if(rs.next()){
                u.setId(rs.getInt(1));                              
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("-------------------------------------");
            }else{
                System.out.println("Id não cadastrado!");
                System.out.println("-------------------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("-------------------------------------");
        }finally{
            try {
                con2.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("-------------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("-------------------------------------");
            }
        }
    }
    
    public  void selectUsuario(Usuario u) throws ClassNotFoundException{
        con = ConnectionDB.getConnection();
        sql = "SELECT * FROM usuario WHERE usuario='"+user+"'";
        System.out.println(sql);       
        try {
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                u.setId(rs.getInt(1));
                u.setUsuario(rs.getString(2));
                u.setPassword(rs.getString(3));                                          
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("-------------------------------------");
            }else{
                System.out.println("Usuario não cadastrado!");
                System.out.println("-------------------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("-------------------------------------");
        }finally{
            try {
                con.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("-------------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("-------------------------------------");
            }
        }
    }
    
    public  void selectUsuarioCloud(Usuario u) throws ClassNotFoundException, SQLException{
        con2 = ConnectionDB.getConnectionCloud();
        sql = "SELECT * FROM usuario WHERE usuario='"+user+"'";
        System.out.println(sql);       
        try {
            stmt2 = con2.prepareStatement(sql);
            rs = stmt2.executeQuery(sql);
            if(rs.next()){
                u.setId(rs.getInt(1));
                u.setUsuario(rs.getString(2));
                u.setPassword(rs.getString(3));                                          
                System.out.println("Acessou o banco de dados com sucesso!");
                System.out.println("-------------------------------------");
            }else{
                System.out.println("Usuario não cadastrado!");
                System.out.println("-------------------------------------");
            }
        } catch(SQLException ex){
                System.out.println("Erro: "+ ex.getMessage());
                System.out.println("-------------------------------------");
        }finally{
            try {
                con2.close();
                System.out.println("Fim da pesquisa!");
                System.out.println("Conexão encerrada!");
                System.out.println("-------------------------------------");
            } catch (SQLException ex) {
                Logger.getLogger(TelaFornecedor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Conexão erro: "+ ex.getMessage());
                System.out.println("-------------------------------------");
            }
        }
    }
}
